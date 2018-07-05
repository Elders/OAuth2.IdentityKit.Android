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


/**
 * Created by IvanVatov on 8/17/2017.
 */
enum class OAuth2Error : Error {
    // OAuth2 errors
    INVALID_REQUEST,
    INVALID_CLIENT,
    INVALID_GRAND,
    UNAUTHORIZED_CLIENT,
    UNSUPPORTED_GRANT_TYPE,
    INVALID_SCOPE,
    INVALID_TOKEN_RESPONSE;

    override fun getMessage(): String {
        return when (this) {
        // OAuth2 error messages
            INVALID_REQUEST -> "The request is missing a required parameter"
            INVALID_CLIENT -> "Unknown client, no client authentication included, or unsupported authentication method"
            INVALID_GRAND -> "The provided authorization grant or refresh token is invalid"
            UNAUTHORIZED_CLIENT -> "The authenticated client is not authorized to use this authorization grant type"
            UNSUPPORTED_GRANT_TYPE -> "The authorization grant type is not supported by the authorization server"
            INVALID_SCOPE -> "The requested scope is invalid"
            INVALID_TOKEN_RESPONSE -> "The received access token response is not valid"
        }
    }
}