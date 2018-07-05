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

package com.eldersoss.identitykit.oauth2

import android.net.Uri
import com.eldersoss.identitykit.authorization.Authorizer
import com.eldersoss.identitykit.network.NetworkClient
import com.eldersoss.identitykit.network.NetworkRequest
import java.nio.charset.Charset

/**
 * Default implementation
 */
class DefaultTokenRefresher(val tokenEndPoint: String, val networkClient: NetworkClient, val authorizer: Authorizer) : TokenRefresher {

    override fun refresh(refreshToken: String, scope: String?, callback: (Token?, Error?) -> Unit) {
        var body = "grant_type=refresh_token&refresh_token=$refreshToken"
        if (scope != null) {
            val uriScope = Uri.encode(scope)
            body += "&scope=$uriScope"
        }

        val request = NetworkRequest("POST", NetworkRequest.Priority.IMMEDIATE, tokenEndPoint, HashMap(), body.toByteArray(Charset.defaultCharset()))
        authorizer.authorize(request) { networkRequest, _ ->
            // Execute request
            networkClient.execute(networkRequest) { networkResponse ->
                //Parse Token from network response
                if (networkResponse.getJson() != null && networkResponse.statusCode in 200..299) {
                    val jsonObject = networkResponse.getJson()
                    val accessToken = jsonObject?.optString("access_token", null)
                    val tokenType = jsonObject?.optString("token_type", null)
                    val expiresIn = (System.currentTimeMillis() / 1000) + jsonObject!!.optLong("expires_in", 0L)
                    val refrToken = jsonObject?.optString("refresh_token", null)
                    val tokenScope = jsonObject?.optString("scope", null)
                    if (accessToken != null && tokenType != null && expiresIn != null) {
                        // if response is valid token return it to callback
                        callback(Token(accessToken, tokenType, expiresIn, refrToken, tokenScope), null)
                    }
                } else if (networkResponse.statusCode in 400..499) {
                    callback(null, OAuth2Error.get(networkResponse.getJson()?.optString("error")))
                } else {
                    callback(null, networkResponse.error)
                }

            }
        }
    }
}