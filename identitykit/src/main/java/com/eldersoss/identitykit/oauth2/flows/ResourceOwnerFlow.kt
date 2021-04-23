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

import com.eldersoss.identitykit.CredentialsProvider
import com.eldersoss.identitykit.Password
import com.eldersoss.identitykit.Username
import com.eldersoss.identitykit.authorization.Authorizer
import com.eldersoss.identitykit.authorization.authorizeAndPerform
import com.eldersoss.identitykit.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.4">Client Credentials Grant</a>
 * @property tokenEndPoint = Token end point
 * @property credentialsProvider - Instance of object that implement CredentialsProvider interface
 * @property scope - Token scopes separate by space
 * @property authorizer - Request authorizer
 * @property networkClient - Network client that implement NetworkClient interface
 * @constructor Create client credentials flow
 */
class ResourceOwnerFlow(val tokenEndPoint: String, val credentialsProvider: CredentialsProvider, val scope: String, val authorizer: Authorizer, val networkClient: NetworkClient) : AuthorizationFlow {
    /**
     * Build and execute request for authentication
     */
    override suspend fun authenticate(): NetworkResponse {

        val credentials = getCredentials()

        val params = ParamsBuilder()
            .add("grant_type", "password")
            .add("username", credentials.first)
            .add("password", credentials.second)
            .add("scope", scope)
            .build()

        val request = NetworkRequest(
            NetworkRequest.Method.POST,
            NetworkRequest.Priority.IMMEDIATE,
            tokenEndPoint,
            HashMap(),
            params.toByteArray(charset(DEFAULT_CHARSET))
        )


        return authorizer.authorizeAndPerform(request, networkClient)

        /*credentialsProvider.provideCredentials { username, password ->

            val params = ParamsBuilder()
                    .add("grant_type", "password")
                    .add("username", username)
                    .add("password", password)
                    .add("scope", scope)
                    .build()

            val request = NetworkRequest(NetworkRequest.Method.POST, NetworkRequest.Priority.IMMEDIATE, tokenEndPoint, HashMap(), params.toByteArray(charset(DEFAULT_CHARSET)))
            authorizer.authorizeAndPerform(request, networkClient)
        }*/
    }

    private suspend fun getCredentials(): Pair<Username, Password> {

        return suspendCoroutine { continuation ->

            try {

                credentialsProvider.provideCredentials { username, password ->

                    continuation.resumeWith(Result.success(Pair(username, password)))
                }

            } catch (e: Throwable) {

                continuation.resumeWithException(e)
            }
        }
    }
}