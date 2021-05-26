package com.eldersoss.identitykit.errors

import com.eldersoss.identitykit.network.NetworkResponse

abstract class OAuth2Error : Error() {

    abstract val errorType: OAuth2ErrorType

    override val message: String
        get() = errorType.message

    var errorDescription: String? = null

    companion object {

        internal fun getError(response: NetworkResponse): OAuth2Error? {

            return when (response.getJson()?.optString("error")) {

                "invalid_request" -> OAuth2InvalidRequestError()
                "invalid_client" -> OAuth2InvalidClientError()
                "invalid_grant" -> OAuth2InvalidGrandError()
                "unauthorized_client" -> OAuth2UnauthorizedClientError()
                "unsupported_grant_type" -> OAuth2UnsupportedGrantTypeError()
                "invalid_scope" -> OAuth2InvalidScopeError()
                else -> null
            }?.also {
                // Try to get the optional error_description from the response.
                it.errorDescription = response.getJson()?.optString("error_description")
            }
        }
    }
}