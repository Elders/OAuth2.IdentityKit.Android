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

package com.eldersoss.identitykit

import com.eldersoss.identitykit.network.NetworkError
import com.eldersoss.identitykit.network.NetworkResponse
import com.eldersoss.identitykit.oauth2.OAuth2Error

/**
 * Created by IvanVatov on 9/1/2017.
 */
interface Error {
    fun getMessage(): String
}

internal fun getError(response: NetworkResponse): Error {

    return when (response.getJson()?.optString("error")) {

        "invalid_request" -> OAuth2Error.INVALID_REQUEST
        "invalid_client" -> OAuth2Error.INVALID_CLIENT
        "invalid_grant" -> OAuth2Error.INVALID_GRAND
        "unauthorized_client" -> OAuth2Error.UNAUTHORIZED_CLIENT
        "unsupported_grant_type" -> OAuth2Error.UNSUPPORTED_GRANT_TYPE
        "invalid_scope" -> OAuth2Error.INVALID_SCOPE
        else -> NetworkError(response.getStringData())
    }
}