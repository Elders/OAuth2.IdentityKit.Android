package com.eldersoss.identitykit.oauth2.flows

import com.eldersoss.identitykit.authorization.Authorizer
import com.eldersoss.identitykit.network.NetworkClient
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.oauth2.Token
import com.eldersoss.identitykit.oauth2.TokenError

/**
 * Created by IvanVatov on 8/17/2017.
 */
interface AuthorizationFlow {
    fun authenticate(callback: (Token?, TokenError?) -> Unit)
}

fun authorizeAndPerform(request: NetworkRequest, authorizer: Authorizer, networkClient: NetworkClient, callback: (Token?, TokenError?) -> Unit) {
    authorizer.authorize(request, { networkRequest: NetworkRequest, s: String? ->
        networkClient.execute(networkRequest, { networkResponse ->
            //Parse Token from network response
            if (networkResponse.getJson() != null && networkResponse.statusCode == 200) {
                var jsonObject = networkResponse.getJson()
                val accessToken = jsonObject?.optString("access_token", null)
                val tokenType = jsonObject?.optString("token_type", null)
                val expiresIn = jsonObject?.optLong("expires_in", 0L)
                val refreshToken = jsonObject?.optString("refresh_token", null)
                val scope = jsonObject?.optString("scope", null)
                if (accessToken != null && tokenType != null && expiresIn != null) {
                    // if response is valid token return it to callback
                    callback(Token(accessToken, tokenType, expiresIn, refreshToken, scope), null)
                }
            }
        })
    })
}
