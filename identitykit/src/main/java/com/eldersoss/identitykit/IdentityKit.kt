/*
 * Copyright (c) 2017. Elders LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eldersoss.identitykit

import com.eldersoss.identitykit.authorization.Authorizer
import com.eldersoss.identitykit.authorization.BearerAuthorizer
import com.eldersoss.identitykit.network.NetworkClient
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.network.NetworkResponse
import com.eldersoss.identitykit.oauth2.Token
import com.eldersoss.identitykit.oauth2.Error
import com.eldersoss.identitykit.oauth2.OAuth2Error
import com.eldersoss.identitykit.oauth2.TokenRefresher
import com.eldersoss.identitykit.oauth2.flows.AuthorizationFlow
import com.eldersoss.identitykit.storage.REFRESH_TOKEN
import com.eldersoss.identitykit.storage.TokenStorage

/**
 * @property flow - Desired Oauth2 flow
 * @property tokenAuthorizationProvider - function which return authorizer
 * @property refresher - token refresher must implement TokenRefresher, DefaultTokenRefresher provided
 * @property storage - Key, Value storage
 * @property client Network client with ability to execute NetworkRequest
 * @constructor - the primary constructor is not used currently
 */
class IdentityKit(val flow: AuthorizationFlow, val tokenAuthorizationProvider: (Token) -> Authorizer, val refresher: TokenRefresher?, val storage: TokenStorage?, val client: NetworkClient) {
    /**
     * Constructor that take BearerAuthorizer.Method
     */
    constructor(flow: AuthorizationFlow, tokenAuthorizationMethod: BearerAuthorizer.Method, refresher: TokenRefresher?, storage: TokenStorage?, client: NetworkClient) :
            this(
                    flow, { token -> BearerAuthorizer(tokenAuthorizationMethod, token) }, refresher, storage, client
            )


    private var token: Token? = null

    private val executor = SerialTaskExecutor()

    /**
     * Authotrize and execute the request with provided network client
     * @param request network request
     * @param callback callback function that return back NetworkResponse,
     * Possible case callback can return NetworkResponse with Error
     */
    @Synchronized
    fun authorizeAndExecute(request: NetworkRequest, callback: (NetworkResponse) -> Unit) {
        authorize(request, { authorizedRequest, error ->
            if (error == null) {
                client.execute(authorizedRequest, { networkResponse ->
                    callback(networkResponse)
                })
            }
        })
    }

    /**
     * Authotrize and return authorized request
     * @param request network request
     * @param callback callback function that return back authorized request,
     * Possible case callback can return unauthorized request and Error
     */
    @Synchronized
    fun authorize(request: NetworkRequest, callback: (NetworkRequest, Error?) -> Unit) {
        val runnable = Runnable {
            authorizeOrRefresh(request, callback)
        }
        executor.execute(runnable)
    }

    /**
     * Just execute network request request
     * @param request network request
     * @param callback callback function that return back NetworkResponse,
     * Possible case callback can return NetworkResponse with Error
     */
    @Synchronized
    fun execute(request: NetworkRequest, callback: (NetworkResponse) -> Unit) {
        client.execute(request, { networkResponse ->
            callback(networkResponse)
        })
    }

    @Synchronized
    fun revokeAuthentication() {
        val runnable = Runnable {
            storage?.let { storage.delete(REFRESH_TOKEN) }
            this.token = null
        }
        executor.execute(runnable)
    }

    @Synchronized
    fun getValidToken(callback: (Token?, Error?) -> Unit) {
        val runnable = Runnable {
            val lock = java.lang.Object()

            val refreshToken = storage?.read(REFRESH_TOKEN)
            if (tokenIsValid()) {
                // We have valid token
                callback(token!!, null)
            }
            // we have refresh token stored
            else if (refreshToken != null && refresher != null) {
                refresher?.refresh(refreshToken, token?.scope, { token, error ->
                    if (token != null) {
                        if (token.refreshToken != null) {
                            storage?.let { storage.write(REFRESH_TOKEN, token.refreshToken) }
                        }
                        this.token = token
                        callback(token!!, null)
                    } else {
                        if (error == OAuth2Error.invalid_grant) {
                            storage?.let { storage.delete(REFRESH_TOKEN) }
                        }
                        this.token = null
                        callback(null, error)
                    }
                    synchronized(lock) {
                        lock.notify()
                    }
                })
                synchronized(lock) {
                    lock.wait()
                }
            } else {
                flow.authenticate({ networkResponse ->
                    token = parseToken(networkResponse)
                    if (token != null) {
                        if (token?.refreshToken != null) {
                            storage?.let { storage.write(REFRESH_TOKEN, token?.refreshToken!!) }
                        }
                        this.token = token
                        callback(token!!, null)
                    } else {
                        this.token = null
                        callback(null, networkResponse.error)
                    }
                    synchronized(lock) {
                        lock.notify()
                    }
                })
                synchronized(lock) {
                    lock.wait()
                }
            }
        }
        executor.execute(runnable)
    }

    /**
     * Perform internal logic to authorize the request
     */
    private fun authorizeOrRefresh(request: NetworkRequest, callback: (NetworkRequest, Error?) -> Unit) {
        val lock = java.lang.Object()

        if (tokenIsValid()) {
            tokenAuthorizationProvider(token!!).authorize(request, callback)
            return
        }
        // we have refresh token stored
        val refreshToken = storage?.read(REFRESH_TOKEN)
        if (refreshToken != null && refresher != null) {
            refreshToken(refreshToken, request, { networkRequest, error ->
                if (error != null) {
                    callback(networkRequest, error)
                } else {
                    tokenAuthorizationProvider(token!!).authorize(request, callback)
                }
                synchronized(lock) {
                    lock.notify()
                }
            })
            synchronized(lock) {
                lock.wait()
            }
        } else {
            useCredentials(request, { networkRequest, error ->
                if (error != null) {
                    callback(networkRequest, error)
                } else {
                    tokenAuthorizationProvider(token!!).authorize(request, callback)
                }
                synchronized(lock) {
                    lock.notify()
                }
            })
            synchronized(lock) {
                lock.wait()
            }
        }
    }

    /** Refresh access token and authorize queue */
    private fun refreshToken(refreshToken: String, request: NetworkRequest, callback: (NetworkRequest, Error?) -> Unit) {
        refresher?.refresh(refreshToken, token?.scope, { token, tokenError ->
            if (tokenError != null) {
                if (OAuth2Error.invalid_grant == tokenError) {
                    storage?.let { storage.delete(REFRESH_TOKEN) }
                    useCredentials(request, callback)
                } else {
                    callback(request, tokenError)
                }
            }
            if (token != null) {
                this.token = token
                if (token.refreshToken != null) {
                    storage?.let { storage.write(REFRESH_TOKEN, token.refreshToken) }
                }
                callback(request, null)
            }
        })
    }


    /** Use the given flow to obtain access token */
    private fun useCredentials(request: NetworkRequest, callback: (NetworkRequest, Error?) -> Unit) {
        flow.authenticate({ networkResponse ->
            token = parseToken(networkResponse)
            if (token != null) {
                this.token = token
                if (token?.refreshToken != null) {
                    storage?.let { storage.write(REFRESH_TOKEN, token?.refreshToken!!) }
                }
                callback(request, null)
            } else {
                if (networkResponse.getJson() != null) {
                    val error = OAuth2Error.valueOf(networkResponse.getJson()!!.optString("error"))
                    networkResponse.error = error
                    callback(request, networkResponse.error)
                    if (OAuth2Error.invalid_grant == error) {
                        useCredentials(request, callback)
                    }
                } else {
                    callback(request, networkResponse.error)
                }
            }
        })
    }

    /** Parse Token object from network response this can return null */
    private fun parseToken(networkResponse: NetworkResponse): Token? {
        //Parse Token from network response
        if (networkResponse.getJson() != null) {
            if (networkResponse.statusCode in 200..299) {
                val jsonObject = networkResponse.getJson()
                val accessToken = jsonObject?.optString("access_token", null)
                val tokenType = jsonObject?.optString("token_type", null)
                val expiresIn = (System.currentTimeMillis() / 1000) + jsonObject!!.optLong("expires_in", 0L)
                val refreshToken = jsonObject?.optString("refresh_token", null)
                val scope = jsonObject?.optString("scope", null)
                if (accessToken != null && tokenType != null && expiresIn != null) {
                    // if response is valid token return it to callback
                    return Token(accessToken, tokenType, expiresIn, refreshToken, scope)
                }
            }
        }
        return null
    }

    private fun tokenIsValid(): Boolean {
        if (token != null && token?.expiresIn != null) {
            if (token?.expiresIn!! > System.currentTimeMillis() / 1000) {
                return true
            }
        }
        return false
    }
}