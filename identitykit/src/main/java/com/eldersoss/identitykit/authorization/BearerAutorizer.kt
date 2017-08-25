package com.eldersoss.identitykit.authorization

import com.eldersoss.identitykit.network.IdRequest
import com.eldersoss.identitykit.oauth2.Token

/**
 * Created by IvanVatov on 8/17/2017.
 */
class BearerAutorizer(val method: Method) : Authorizer{

    var token : Token? = null
    get() = null

    enum class Method{
        HEADER, BODY, QUERY;
    }

    override fun authorize(request : IdRequest) {
        when (method) {
            Method.HEADER -> request.headers.put("Authorization", "Bearer $token")
            Method.BODY -> NotImplementedError()
            Method.QUERY -> NotImplementedError()
        }
    }
}