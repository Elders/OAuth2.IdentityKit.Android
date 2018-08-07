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

import android.net.Uri
import com.eldersoss.identitykit.network.BODY_CONTENT_TYPE
import com.eldersoss.identitykit.network.DEFAULT_CHARSET
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.oauth2.Token
import com.eldersoss.identitykit.Error

/**
 * Authorize requests using access token
 * @see <a href="https://tools.ietf.org/html/rfc6750#section-2">Authenticated Requests</a>
 * @property method - BearerAuthorizer.Method : HEADER, BODY or QUERY
 * @property token - Token used for authorization
 * @constructor
 */
class BearerAuthorizer(val method: Method, val token: Token) : Authorizer {

    enum class Method {
        HEADER, BODY, QUERY;
    }

    /**
     * Authorize requests
     * @param request - request for authorization
     * @param handler - callback function that return authorized request
      */
    override fun authorize(request: NetworkRequest, handler: (NetworkRequest, Error?) -> Unit) {
        when (method) {
            Method.HEADER -> headerAuthorization(request)
            Method.BODY -> bodyAuthorization(request, handler)
            Method.QUERY -> queryAuthorization(request)
        }
        handler(request, null)
    }

    private fun headerAuthorization(request: NetworkRequest) {
        val accessToken = token.accessToken
        request.headers.put("Authorization", "Bearer $accessToken")
    }

    private fun bodyAuthorization(request: NetworkRequest, handler: (NetworkRequest, Error?) -> Unit) {
        if (request.method != "GET") {
            handler(request, AuthorizationError.INVALID_METHOD)
            return
        }
        if (request.bodyContentType != BODY_CONTENT_TYPE) {
            handler(request, AuthorizationError.INVALID_CONTENT_TYPE)
            return
        }
        val accessToken = token.accessToken
        var authorizedBody = ""
        if (request.body.isNotEmpty()) {
            authorizedBody = request.body.toString(charset(DEFAULT_CHARSET)) + "&"
        }
        authorizedBody += "access_token=$accessToken"
        request.body = authorizedBody.toByteArray(charset(DEFAULT_CHARSET))
    }

    private fun queryAuthorization(request: NetworkRequest) {
        val accessToken = token.accessToken
        var authorizedUrl : String= request.url
        authorizedUrl += if (request.url.contains("/?")){
            "&access_token="
        } else {
            "?access_token="
        }
        authorizedUrl += Uri.encode(accessToken)
        request.url = authorizedUrl
    }
}