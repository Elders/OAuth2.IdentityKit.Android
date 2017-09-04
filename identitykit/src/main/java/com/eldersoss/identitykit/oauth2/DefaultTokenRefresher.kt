package com.eldersoss.identitykit.oauth2

import android.net.Uri
import com.eldersoss.identitykit.authorization.Authorizer
import com.eldersoss.identitykit.network.NetworkClient
import com.eldersoss.identitykit.network.NetworkRequest

/**
 * Created by IvanVatov on 8/30/2017.
 */
class DefaultTokenRefresher(val tokenEndPoint: String, val networkClient: NetworkClient, val authorizer: Authorizer) : TokenRefresher {

    override fun refresh(refreshToken: String, scope: String?, callback: (Token?, Error?) -> Unit) {
        var body = "grant_type=refresh_token&refresh_token=$refreshToken"
        if (scope != null) {
            val uriScope = Uri.encode(scope)
            body += "&scope=$uriScope"
        }

        val request = NetworkRequest("POST", tokenEndPoint, HashMap(), body)
        authorizer.authorize(request, { networkRequest, error ->
            // Execute request
            when { networkRequest != null -> networkClient.execute(networkRequest, { networkResponse ->
                //Parse Token from network response
                if (networkResponse.getJson() != null && networkResponse.statusCode in 200..299) {
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
                } else if (networkResponse.statusCode in 400..499) {
                    callback(null, OAuth2Error.valueOf(networkResponse.getJson()!!.optString("error")))
                } else {
                    callback(null, networkResponse.error)
                }

            })
            }
        })

    }
}