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
import com.eldersoss.identitykit.oauth2.OAuth2Error
import com.eldersoss.identitykit.oauth2.TokenRefresher
import com.eldersoss.identitykit.oauth2.flows.AuthorizationFlow
import com.eldersoss.identitykit.oauth2.parseToken
import com.eldersoss.identitykit.storage.REFRESH_TOKEN
import com.eldersoss.identitykit.storage.TokenStorage

/**
 * @property kitConfiguration - configuration
 * @property flow - Desired Oauth2 flow
 * @property tokenAuthorizationProvider - function which return authorizer
 * @property refresher - token refresher must implement TokenRefresher, DefaultTokenRefresher provided
 * @property storage - Key, Value storage
 * @property client Network client with ability to execute NetworkRequest
 * @constructor - the primary constructor is not used currently
 */
class IdentityKit(private val kitConfiguration: KitConfiguration, private val flow: AuthorizationFlow, private val tokenAuthorizationProvider: (Token) -> Authorizer, private val refresher: TokenRefresher?, private val storage: TokenStorage?, private val client: NetworkClient) {
    /**
     * Constructor that take BearerAuthorizer.Method
     */
    constructor(kitConfiguration: KitConfiguration, flow: AuthorizationFlow, tokenAuthorizationMethod: BearerAuthorizer.Method, refresher: TokenRefresher?, storage: TokenStorage?, client: NetworkClient) :
            this(
                    kitConfiguration, flow, { token -> BearerAuthorizer(tokenAuthorizationMethod, token) }, refresher, storage, client
            )

    @Volatile
    private var _token: Token? = null

    private val executor = SerialTaskExecutor()
    private val lock = java.lang.Object()

    /**
     * Authotrize and execute the request with provided network client
     * @param request network request
     * @param callback callback function that return back NetworkResponse,
     * Possible case callback can return NetworkResponse with Error
     */
    @Synchronized
    fun authorizeAndExecute(request: NetworkRequest, callback: (NetworkResponse) -> Unit) {
        authorize(request) { authorizedRequest, error ->
            if (error == null) {
                client.execute(authorizedRequest) { networkResponse ->
                    callback(networkResponse)
                }
            } else {
                val response = NetworkResponse()
                response.error = error
                callback(response)
            }
        }
    }

    /**
     * Authotrize and return authorized request
     * @param request network request
     * @param callback callback function that return back authorized request,
     * Possible case callback can return unauthorized request and Error
     */
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
    fun execute(request: NetworkRequest, callback: (NetworkResponse) -> Unit) {
        client.execute(request) { networkResponse ->
            callback(networkResponse)
        }
    }

    fun revokeAuthentication() {
        val runnable = Runnable {
            storage?.let { storage.delete(REFRESH_TOKEN) }
            this._token = null
        }
        executor.execute(runnable)
    }


    fun getValidToken(callback: (Token?, Error?) -> Unit) {
        val runnable = Runnable {
            val refreshToken = storage?.read(REFRESH_TOKEN)
            validToken()?.let {
                callback(it, null)
                return@Runnable
            }
            // we have refresh token stored
            if (refreshToken != null && refresher != null) {
                refresherRefreshToken(refreshToken, callback)
            } else {
                flowAuthenticate(callback)
            }
            synchronized(lock) {
                lock.wait()
            }
        }
        executor.execute(runnable)
    }

    /**
     * Perform internal logic to authorize the request
     */
    private fun authorizeOrRefresh(request: NetworkRequest, callback: (NetworkRequest, Error?) -> Unit) {
        validToken()?.let {
            tokenAuthorizationProvider(it).authorize(request, callback)
            return
        }
        // we have refresh token stored
        val refreshToken = storage?.read(REFRESH_TOKEN)
        if (refreshToken != null && refresher != null) {
            refresherRefreshToken(refreshToken) { token, error ->
                if (error != null) {
                    callback(request, error)
                } else {
                    tokenAuthorizationProvider(token!!).authorize(request, callback)
                }

            }
        } else {
            flowAuthenticate { token, error ->
                if (error != null) {
                    callback(request, error)
                } else {
                    tokenAuthorizationProvider(token!!).authorize(request, callback)
                }
            }
        }
        synchronized(lock) {
            lock.wait()
        }
    }


    /** Use the given flow to obtain access token */
    private fun flowAuthenticate(callback: (Token?, Error?) -> Unit) {
        flow.authenticate { networkResponse ->

            parseToken(networkResponse) { token, error ->
                if (error is OAuth2Error && kitConfiguration.retryFlowAuthentication) {
                    if (kitConfiguration.onAuthenticationRetryInvokeCallbackWithFailure) {
                        callback(token, error)
                    }
                    flowAuthenticate(callback)
                    return@parseToken
                }
                callback(token, error)

                if (token != null) {
                    this._token = token
                    if (token.refreshToken != null) {
                        storage?.let { storage.write(REFRESH_TOKEN, token.refreshToken) }
                    }
                }
            }
        }
    }

    /** Refresh access token */
    private fun refresherRefreshToken(refreshToken: String, callback: (Token?, Error?) -> Unit) {
        refresher?.refresh(refreshToken, _token?.scope) { token, error ->
            if (token != null) {
                this._token = token
                if (token.refreshToken != null) {
                    storage?.let { storage.write(REFRESH_TOKEN, token.refreshToken) }
                }
                callback(token, null)
            } else {
                if (error is OAuth2Error) {
                    storage?.let { storage.delete(REFRESH_TOKEN) }
                    if (kitConfiguration.authenticateOnFailedRefresh) {
                        if (kitConfiguration.onAuthenticationRetryInvokeCallbackWithFailure) {
                            callback(token, error)
                        }
                        flowAuthenticate(callback)
                        return@refresh
                    }
                }
                callback(null, error)
            }
            synchronized(lock) {
                lock.notify()
            }
        }
    }

    private fun validToken(): Token? {
        if (this._token != null && this._token?.expiresIn != null) {
            if (this._token?.expiresIn!! > System.currentTimeMillis() / 1000) {
                return this._token
            }
        }
        return null
    }
}