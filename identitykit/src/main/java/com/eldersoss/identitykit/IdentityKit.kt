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
import java.util.*

/**
 * Created by IvanVatov on 8/30/2017.
 */
class IdentityKit(val flow: AuthorizationFlow, val tokenAuthorizationProvider: (Token) -> Authorizer, val refresher: TokenRefresher, val storage: TokenStorage?, val client: NetworkClient) {

    constructor(flow: AuthorizationFlow, tokenAuthorizationMethod: BearerAuthorizer.Method, refresher: TokenRefresher, storage: TokenStorage?, client: NetworkClient) :
            this(
                    flow, { token -> BearerAuthorizer(tokenAuthorizationMethod, token) }, refresher, storage, client
            )

    private val queue = ArrayDeque<RequestHandler>()

    private data class RequestHandler(val request: NetworkRequest, val callback: (NetworkRequest, Error?) -> Unit)

    @Volatile
    private var isRefreshing = false

    private var token: Token? = null

    fun authorizeAndExecute(request: NetworkRequest, callback: (NetworkResponse) -> Unit) {
        authorize(request, { authorizedRequest, error ->
            client.execute(authorizedRequest, { networkResponse ->
                callback(networkResponse)
            })
        })
    }

    fun authorize(request: NetworkRequest, callback: (NetworkRequest, Error?) -> Unit) {
        if (token != null) {
            // We have valid token
            if (token?.expiresIn!! > System.currentTimeMillis()) {
                tokenAuthorizationProvider(token!!).authorize(request, callback)
                return
            }
        }
        // we have refresh token stored
        val refreshToken = storage?.read(REFRESH_TOKEN)
        if (refreshToken != null) {
            refreshToken(refreshToken, request, callback)
            return
        }

        // we have nothing - request and use credentials
        useCredentials(request, callback)
    }

    private fun authorizeQueue() {
        if (token != null) {
            synchronized(queue) {
                while (queue.isNotEmpty()) {
                    val requestHandler = queue.poll()
                    tokenAuthorizationProvider(token!!).authorize(requestHandler.request, requestHandler.callback)
                }
            }
        }
    }

    private fun refreshToken(refreshToken: String, request: NetworkRequest, callback: (NetworkRequest, Error?) -> Unit) {
        val requestHandler = RequestHandler(request, callback)
        synchronized(queue) {
            queue.add(requestHandler)
        }
        if (!isRefreshing) {
            if (refreshToken != null) {
                isRefreshing = true
                refresher.refresh(refreshToken!!, token?.scope, { token, tokenError ->
                    run {
                        isRefreshing = false
                        if (tokenError != null) {
                            callback(request, tokenError)
                            return@run
                        }
                        if (token != null) {
                            if (token.refreshToken != null) {
                                storage?.write(REFRESH_TOKEN, token.refreshToken)
                            }
                            authorizeQueue()
                        }
                    }
                })
            }
        }
    }

    private fun useCredentials(request: NetworkRequest, callback: (NetworkRequest, Error?) -> Unit){
        val requestHandler = RequestHandler(request, callback)
        synchronized(queue) {
            queue.add(requestHandler)
        }
        if (!isRefreshing) {
            isRefreshing = true
            flow.authenticate({ networkResponse ->
                run {
                    isRefreshing = false
                    token = parseToken(networkResponse)
                    if (token != null) {
                        if (token?.refreshToken != null) {
                            storage?.write(REFRESH_TOKEN, token?.refreshToken!!)
                        }
                        authorizeQueue()
                    } else {
                        if (networkResponse.statusCode in 400..499) {
                            networkResponse.error = OAuth2Error.valueOf(networkResponse.getJson()!!.optString("error"))
                            callback(request, networkResponse.error)
                        } else {
                            callback(request, networkResponse.error)
                        }
                    }
                }
            })
        }
    }

    private fun parseToken(networkResponse: NetworkResponse): Token? {
        //Parse Token from network response
        if (networkResponse.getJson() != null) {
            if (networkResponse.statusCode in 200..299) {
                var jsonObject = networkResponse.getJson()
                val accessToken = jsonObject?.optString("access_token", null)
                val tokenType = jsonObject?.optString("token_type", null)
                val expiresIn = jsonObject?.optLong("expires_in", 0L)
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
}