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

import com.eldersoss.identitykit.Error
import com.eldersoss.identitykit.ext.getOptString
import com.eldersoss.identitykit.getError
import com.eldersoss.identitykit.network.NetworkResponse

/**
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-5.1">Successful Response</a>
 *  @constructor Access token object
 */
data class Token(val accessToken: String,
                 val tokenType: String,
                 val expiresIn: Long,
                 val refreshToken: String?,
                 val scope: String?)

/** Parse Token object from network response, possible error */
fun parseToken(networkResponse: NetworkResponse, callback: (Token?, Error?) -> Unit) {
    if (networkResponse.getJson() != null && networkResponse.statusCode in 200..299) {
        val jsonObject = networkResponse.getJson()
        jsonObject?.let {
            try {
                callback(
                        Token(
                                //required fields
                                it.getString("access_token"),
                                it.getString("token_type"),
                                (System.currentTimeMillis() / 1000) + it.getLong("expires_in"),
                                //optional fields
                                it.getOptString("refresh_token"),
                                it.getOptString("scope")
                        ), null

                )
            } catch (e: Exception) {
                callback(null, OAuth2Error.INVALID_TOKEN_RESPONSE)
            }
        }
    } else if (networkResponse.statusCode in 400..499) {
        // try found Oauth2Error or return NetworkError
        callback(null, getError(networkResponse))
    } else {
        // return error from network client
        callback(null, networkResponse.error)
    }
}