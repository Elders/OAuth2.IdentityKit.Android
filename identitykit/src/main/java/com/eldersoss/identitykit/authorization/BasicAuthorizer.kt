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

package com.eldersoss.identitykit.authorization

import android.util.Base64
import com.eldersoss.identitykit.network.DEFAULT_CHARSET
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.Error

/**
 * Authorize requests using access token
 * @see <a href="https://tools.ietf.org/html/rfc6750#section-2">Authenticated Requests</a>
 * @property method - BearerAuthorizer.Method : HEADER, BODY or QUERY
 * @property token - Token used for authorization
 * @constructor
 */
class BasicAuthorizer(val userName : String, val password : String) : Authorizer {

    /**
     * Authorize requests
     * @param request - request for authorization
     * @param handler - callback function that return authorized request
     */
    override fun authorize(request: NetworkRequest, handler: (NetworkRequest, Error?) -> Unit){
        request.headers.put("Authorization", "Basic " + encodeString("$userName:$password"))
        handler(request, null)
    }

    private fun encodeString(str : String) : String{
        return Base64.encodeToString(str.toByteArray(charset(DEFAULT_CHARSET)), Base64.NO_WRAP)
    }
}