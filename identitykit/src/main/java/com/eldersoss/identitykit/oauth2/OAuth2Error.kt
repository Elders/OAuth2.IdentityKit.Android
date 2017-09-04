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
            else -> "Unknown error"
        }
    }
}