package com.eldersoss.identitykit

import com.eldersoss.identitykit.authorization.Authorizer
import com.eldersoss.identitykit.authorization.BearerAutorizer
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.oauth2.Token
import com.eldersoss.identitykit.oauth2.TokenError
import com.eldersoss.identitykit.oauth2.TokenRefresher
import com.eldersoss.identitykit.oauth2.flows.AuthorizationFlow
import com.eldersoss.identitykit.storage.TokenStorage
import java.util.*

/**
 * Created by IvanVatov on 8/30/2017.
 */
class IdentityKit(val flow: AuthorizationFlow, val tokenAuthorizationProvider: (Token) -> Authorizer, val refresher: TokenRefresher, val storage: TokenStorage?) {

    constructor(flow: AuthorizationFlow, tokenAuthorizationMethod: BearerAutorizer.Method, refresher: TokenRefresher, storage: TokenStorage?) :
            this(
                    flow, { token -> BearerAutorizer(tokenAuthorizationMethod, token) }, refresher, storage
            )

    private val queue = ArrayDeque<RequestHandler>()
    private data class RequestHandler(val request: NetworkRequest, val callback: (NetworkRequest, TokenError?) -> Unit)
    private var isRefreshing = false

    fun authorize(request: NetworkRequest, callback: (NetworkRequest, TokenError?) -> Unit) {
        val token = storage?.readToken()
        if (token != null) {
            if (token?.expiresIn!! > System.currentTimeMillis()) {
                tokenAuthorizationProvider(token!!).authorize(request, callback)
                return
            } else {
                if (token?.refreshToken != null) {
                    val requestHandler = RequestHandler(request, callback)
                    synchronized(queue) {
                        queue.add(requestHandler)
                    }
                    if (!isRefreshing) {
                        isRefreshing = true
                        refresher.refresh(token?.refreshToken!!, token?.scope, { token, tokenError ->
                            run {
                                isRefreshing = false
                                if (tokenError != null) {
                                    callback(request, tokenError)
                                    return@run
                                }
                                if (token != null) {
                                    storage?.writeToken(token)
                                    authorizeQueue()
                                }
                            }
                        })
                    }
                }
            }
        }
        if (token == null) {
            val requestHandler = RequestHandler(request, callback)
            synchronized(queue) {
                queue.add(requestHandler)
            }
            if (!isRefreshing) {
                isRefreshing = true
                flow.authenticate({ token, tokenError ->
                    run {
                        isRefreshing = false
                        if (tokenError != null) {
                            callback(request, tokenError)
                            return@run
                        }
                        if (token != null) {
                            storage?.writeToken(token)
                            authorizeQueue()
                        }
                    }
                })
            }
        }
    }

    private fun authorizeQueue() {
        val token = storage?.readToken()
        if (token != null) {
            synchronized(queue) {
                while (queue.isNotEmpty()) {
                    val requestHandler = queue.poll()
                    tokenAuthorizationProvider(token).authorize(requestHandler.request, requestHandler.callback)
                }
            }
        }
    }
}