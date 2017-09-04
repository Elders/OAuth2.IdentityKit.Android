package com.eldersoss.identitykit.authorization

import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.oauth2.Token
import com.eldersoss.identitykit.oauth2.Error

/**
 * Created by IvanVatov on 8/17/2017.
 */

// https://tools.ietf.org/html/rfc6750#section-2
class BearerAuthorizer(val method: Method, val token: Token) : Authorizer {

    enum class Method {
        HEADER, BODY, QUERY;
    }

    override fun authorize(request: NetworkRequest, handler: (NetworkRequest, Error?) -> Unit) {
        val accessToken = token.accessToken
        when (method) {
            Method.HEADER -> request.headers.put("Authorization", "Bearer $accessToken")
            Method.BODY -> NotImplementedError()
            Method.QUERY -> NotImplementedError()
        }
        handler(request, null)
    }
}