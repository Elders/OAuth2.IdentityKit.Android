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
import com.eldersoss.identitykit.ext.parseToken
import com.eldersoss.identitykit.network.NetworkClient
import com.eldersoss.identitykit.network.NetworkRequest
import java.nio.charset.Charset

/**
 * Default implementation
 */
class DefaultTokenRefresher(
    private val tokenEndPoint: String,
    private val networkClient: NetworkClient,
    private val authorizer: Authorizer
) : TokenRefresher {

    override suspend fun refresh(refreshToken: String, scope: String?): Token? {
        var builder = Uri.Builder()
            .appendQueryParameter("grant_type", "refresh_token")
            .appendQueryParameter("refresh_token", refreshToken)
        if (scope != null) {
            builder.appendQueryParameter("scope", scope)
        }

        val body = builder.build().query

        val request = NetworkRequest(
            NetworkRequest.Method.POST,
            NetworkRequest.Priority.IMMEDIATE,
            tokenEndPoint,
            HashMap(),
            body?.toByteArray(Charset.defaultCharset())
        )
        authorizer.authorize(request)
        // Execute request
        return networkClient.execute(request).parseToken()
    }
}