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
import com.eldersoss.identitykit.errors.OAuth2InvalidTokenResponseError
import com.eldersoss.identitykit.ext.getOptString
import com.eldersoss.identitykit.ext.parseToken
import com.eldersoss.identitykit.network.*
import com.eldersoss.identitykit.oauth2.Token

/**
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.4">Client Credentials Grant</a>
 * @property tokenEndPoint = Token end point
 * @property scope - Token scopes separate by space
 * @property authorizer - Request authorizer
 * @property networkClient - Network client that implement NetworkClient interface
 * @constructor Create client credentials flow
 */
class ClientCredentialsFlow(
    private val tokenEndPoint: String,
    private val scope: String,
    private val authorizer: Authorizer,
    private val networkClient: NetworkClient
) :
    AuthorizationFlow {
    /**
     * Build and execute request for authentication
     */
    override suspend fun authenticate(): Token {

        val params = Uri.Builder()
            .appendQueryParameter("grant_type", "client_credentials")
            .appendQueryParameter("scope", scope)
            .build().query

        val request = NetworkRequest(
            NetworkRequest.Method.POST,
            NetworkRequest.Priority.IMMEDIATE,
            tokenEndPoint,
            HashMap(),
            params?.toByteArray(DEFAULT_CHARSET)
        )

        authorizer.authorize(request)
        val response = networkClient.execute(request)

        validateResponse(response)

        return response.parseToken()

    }

    private fun validateResponse(networkResponse: NetworkResponse) {

        if (networkResponse.getJson() != null) {

            val jsonObject = networkResponse.getJson()
            val refreshToken = jsonObject?.getOptString("refresh_token")

            if (refreshToken != null) {

                throw OAuth2InvalidTokenResponseError()
            }
        }
    }
}