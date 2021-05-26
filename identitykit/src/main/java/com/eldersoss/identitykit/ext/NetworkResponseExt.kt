package com.eldersoss.identitykit.ext

import com.eldersoss.identitykit.errors.*
import com.eldersoss.identitykit.network.NetworkResponse
import com.eldersoss.identitykit.oauth2.Token

fun NetworkResponse.parseToken(): Token {

    if (this.statusCode in 400..499) {

        // try found OAuth2Error or throw general Error
        throw OAuth2Error.getError(this)
    }

    if (this.statusCode in 200..299) {

        val jsonObject = this.getJson()
            ?: throw OAuth2InvalidTokenResponseError()
        return Token(jsonObject)
    }

    throw Error(this.error?.message)
}