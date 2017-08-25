package com.eldersoss.identitykit.oauth2

import com.eldersoss.identitykit.CredentialsProvider
import com.eldersoss.identitykit.authorization.Authorizer
import com.eldersoss.identitykit.network.IdClient
import com.eldersoss.identitykit.network.IdRequest
import com.eldersoss.identitykit.network.IdResponse
import com.eldersoss.identitykit.oauth2.flows.AuthorizationFlow
import com.eldersoss.identitykit.oauth2.flows.ResourceOwnerFlow
import com.eldersoss.identitykit.storage.TokenStorage

/**
 * Created by IvanVatov on 8/23/2017.
 */
class TokenRefresher(var authorizer: Authorizer?) {

    var credentialsProvider: CredentialsProvider? = null
    var tokenEndPoint: String? = null
    var storage: TokenStorage? = null
    var client: IdClient? = null
    var flow: AuthorizationFlow? = null


    var onTokenValid: (Token, String) -> Unit = { _, _ -> Unit }

    private var token: Token? = null
    private var tokenExpiration = 0L
    private var isRefreshing: Boolean = false

    fun setDependencies(credentialsProvider: CredentialsProvider, tokenEndPoint: String?, storage: TokenStorage?, client: IdClient, flow: AuthorizationFlow?) {
        this.credentialsProvider = credentialsProvider
        this.tokenEndPoint = tokenEndPoint
        this.storage = storage
        this.authorizer = authorizer
        this.client = client
        this.flow = flow
    }

    private fun validateTokenResponse(callback: (Token?, String?) -> Unit, response: IdResponse?, error: String?) {
        if (error == null) {
            if (response?.getJson() != null) {
                val error = response?.getJson()!!.optString("error", null)
                if (error == null) {
                    var jsonObject = response.getJson()
                    val accessToken = jsonObject?.optString("access_token", null)
                    val tokenType = jsonObject?.optString("token_type", null)
                    val expiresIn = jsonObject?.optLong("expires_in", 0L)
                    val refreshToken = jsonObject?.optString("refresh_token", null)
                    val scope = jsonObject?.optString("scope", null)

                    if (accessToken != null && tokenType != null && expiresIn != null){
                        token = Token(accessToken, tokenType, expiresIn, refreshToken, scope)
                        storage?.writeToken(token!!)
                        callback(token, null)
                    } else {
                        callback(null, TokenError("").getMessage())
                    }
                } else {
                    callback(null, TokenError(error).getMessage())
                }
            }
        }
    }

    internal fun getValidToken(callback: (Token?, String?) -> Unit): Token? {
        if (isRefreshing) {
            return null
        }
        if (flow is ResourceOwnerFlow) {
            return if (token?.accessToken?.isNotEmpty() == true) {
                when {
                // case we have valid refresh token saved
                    tokenExpiration > System.currentTimeMillis() -> token
                // case we have expired refresh token
                    else -> {
                        refreshToken(callback)
                        null
                    }
                }
            } else {
                // case we haven`t refresh token
                requestCredentials(callback)
                null
            }
//        } else if (flow is ClientCredentialsFlow) {
//            val request = flow?.authenticate()
//            authorizer?.authorize(request!!)
//            request?.onResponse = { response, error -> }
//            client?.execute(request!!)
//            return null
        } else {
            return null
        }
    }

    private fun requestCredentials(callback: (Token?, String?) -> Unit) {
        credentialsProvider?.provideCredentials { username, password ->
            run {
                (flow as ResourceOwnerFlow).username = username
                (flow as ResourceOwnerFlow).password = password
                val request = flow?.authenticate()
                authorizer?.authorize(request!!)
                request?.onResponse = { response, error -> validateTokenResponse(callback, response, error) }
                client?.execute(request!!)
            }
        }
    }

    private fun refreshToken(callback: (Token?, String?) -> Unit) {
        var request: IdRequest = RefreshTokenRequest(token!!, IdRequest.Method.POST, tokenEndPoint!!, HashMap(), "")
        authorizer?.authorize(request)
        request?.onResponse = { response, error -> validateTokenResponse(callback, response, error) }
        client?.execute(request)
    }
}