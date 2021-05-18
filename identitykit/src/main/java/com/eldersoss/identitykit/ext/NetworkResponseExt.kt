package com.eldersoss.identitykit.ext

import com.eldersoss.identitykit.exceptions.*
import com.eldersoss.identitykit.getError
import com.eldersoss.identitykit.network.NetworkResponse
import com.eldersoss.identitykit.oauth2.OAuth2Error
import com.eldersoss.identitykit.oauth2.Token

fun NetworkResponse.parseToken(): Token {

    if (this.statusCode in 400..499) {

        // try found Oauth2Error or return NetworkError
        val error = getError(this)
        // Try to get the optional error_description from the response.
        val errorDescription = this.getJson()?.optString("error_description")

        throw when (error) {

            OAuth2Error.INVALID_REQUEST ->
                OAuth2InvalidRequestException(
                    errorDescription ?: error.getMessage()
                )
            OAuth2Error.INVALID_CLIENT ->
                OAuth2InvalidClientException(
                    errorDescription ?: error.getMessage()
                )
            OAuth2Error.INVALID_GRAND ->
                OAuth2InvalidGrand(errorDescription ?: error.getMessage())
            OAuth2Error.UNAUTHORIZED_CLIENT ->
                OAuth2UnauthorizedClientException(
                    errorDescription ?: error.getMessage()
                )
            OAuth2Error.UNSUPPORTED_GRANT_TYPE ->
                OAuth2UnsupportedGrantTypeException(
                    errorDescription ?: error.getMessage()
                )
            OAuth2Error.INVALID_SCOPE ->
                OAuth2InvalidScopeException(
                    errorDescription ?: error.getMessage()
                )
            else -> OAuth2Exception(error.getMessage())
        }

    }

    if (this.statusCode in 200..299) {

        val jsonObject = this.getJson()
            ?: throw OAuth2Exception(OAuth2Error.INVALID_TOKEN_RESPONSE.getMessage())
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


    throw Throwable(this.error?.getMessage())

}