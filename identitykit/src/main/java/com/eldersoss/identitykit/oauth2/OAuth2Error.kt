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


/**
 * Created by IvanVatov on 8/17/2017.
 */
enum class OAuth2Error(val errorMessage: String?) : Error{
    // OAuth2 errors
    invalid_request("invalid_request"),
    invalid_client("invalid_client"),
    invalid_grant("invalid_grant"),
    unauthorized_client("unauthorized_client"),
    unsupported_grant_type("unsupported_grant_type"),
    invalid_scope("invalid_scope"),
    invalid_token_response("invalid_token_response"),
    unknown(null);

    override fun getMessage(): String{
        return when(errorMessage) {
        // OAuth2 error messages
            "invalid_request" -> "The request is missing a required parameter"
            "invalid_client" -> "Unknown client, no client authentication included, or unsupported authentication method"
            "invalid_grant" -> "The provided authorization grant or refresh token is invalid"
            "unauthorized_client" -> "The authenticated client is not authorized to use this authorization grant type"
            "unsupported_grant_type" -> "The authorization grant type is not supported by the authorization server"
            "invalid_scope" -> "The requested scope is invalid"
            "invalid_token_response" -> "The received access token response is not valid"
            else -> "Unknown error"
        }
    }
}