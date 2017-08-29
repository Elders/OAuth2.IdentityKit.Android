package com.eldersoss.identitykit.authorization

import com.eldersoss.identitykit.network.IdRequest
import com.eldersoss.identitykit.oauth2.Token

/**
 * Created by IvanVatov on 8/17/2017.
 */
class BearerAutorizer(val method: Method) : Authorizer {

    private var token: Token? = null

    fun setToken(token: Token){this.token = token}

    enum class Method {
        HEADER, BODY, QUERY;
    }

    override fun authorize(request: IdRequest) {
        val accessToken = token?.accessToken
        if (accessToken != null) {
            when (method) {
                Method.HEADER -> request.headers.put("Authorization", "Bearer $accessToken")
                Method.BODY -> NotImplementedError()
                Method.QUERY -> NotImplementedError()
            }
        }
    }
}