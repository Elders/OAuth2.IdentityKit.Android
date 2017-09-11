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

package com.eldersoss.identitykit.oauth2.flows

import android.net.Uri
import com.eldersoss.identitykit.authorization.Authorizer
import com.eldersoss.identitykit.network.DEFAULT_CHARSET
import com.eldersoss.identitykit.network.NetworkClient
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.network.NetworkResponse
import com.eldersoss.identitykit.oauth2.OAuth2Error

/**
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.4">Client Credentials Grant</a>
 * @property tokenEndPoint = Token end point
 * @property scope - Token scopes separate by space
 * @property authorizer - Request authorizer
 * @property networkClient - Network client that implement NetworkClient interface
 * @constructor Create client credentials flow
 */
class ClientCredentialsFlow(val tokenEndPoint: String, val scope: String, val authorizer: Authorizer, val networkClient: NetworkClient) : AuthorizationFlow {
    /**
     * Build and execute request for authentication
     * @param callback - callback function with NetworkResponse
     */
    override fun authenticate(callback: (NetworkResponse) -> Unit) {
        val uriScope = Uri.encode(scope)
        val request = NetworkRequest("POST", tokenEndPoint, HashMap(), "grant_type=client_credentials&scope=$uriScope".toByteArray(charset(DEFAULT_CHARSET)))
        authorizeAndPerform(request, authorizer, networkClient,
                // validate response
                { networkResponse -> validateResponse(networkResponse, callback) })
    }

    private fun validateResponse(networkResponse: NetworkResponse, callback: (NetworkResponse) -> Unit){
        if (networkResponse.getJson() != null) {
            val jsonObject = networkResponse.getJson()
            val refreshToken = jsonObject?.optString("refresh_token", null)
            if (refreshToken != null){
                networkResponse.error = OAuth2Error.invalid_token_response
            }
        }
        callback(networkResponse)
    }
}