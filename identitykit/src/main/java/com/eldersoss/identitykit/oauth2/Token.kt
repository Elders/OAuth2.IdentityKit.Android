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

import com.eldersoss.identitykit.exceptions.*
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
fun parseToken(networkResponse: NetworkResponse): Token {

    networkResponse.error?.let { error ->

        throw OAuth2Exception(error.getMessage())
    }

    if (networkResponse.statusCode in 400..499) {

        // try found Oauth2Error or return NetworkError
        val error = getError(networkResponse)
        // Try to get the optional error_description from the response.
        val errorDescription = networkResponse.getJson()?.optString("error_description")

        throw when(error) {

            OAuth2Error.INVALID_REQUEST -> OAuth2InvalidRequestException(errorDescription ?: error.getMessage())
            OAuth2Error.INVALID_CLIENT -> OAuth2InvalidClientException(errorDescription ?: error.getMessage())
            OAuth2Error.INVALID_GRAND -> OAuth2InvalidGrand(errorDescription ?: error.getMessage())
            OAuth2Error.UNAUTHORIZED_CLIENT -> OAuth2UnauthorizedClientException(errorDescription ?: error.getMessage())
            OAuth2Error.UNSUPPORTED_GRANT_TYPE -> OAuth2UnsupportedGrantTypeException(errorDescription ?: error.getMessage())
            OAuth2Error.INVALID_SCOPE -> OAuth2InvalidScopeException(errorDescription ?: error.getMessage())
            else -> OAuth2Exception(error.getMessage())
        }

    }

    if (networkResponse.statusCode !in 200..299) {

        throw Throwable(OAuth2Error.INVALID_TOKEN_RESPONSE.getMessage())
    }

    val jsonObject = networkResponse.getJson() ?: throw Throwable(OAuth2Error.INVALID_TOKEN_RESPONSE.getMessage())
    return Token(
        //required fields
        jsonObject.getString("access_token"),
        jsonObject.getString("token_type"),
        (System.currentTimeMillis() / 1000) + jsonObject.getLong("expires_in"),
        //optional fields
        jsonObject.getOptString("refresh_token"),
        jsonObject.getOptString("scope")
    )
}