package com.eldersoss.identitykit.errors

enum class OAuth2ErrorType(val message: String) {
    // OAuth2 errors
    INVALID_REQUEST("The request is missing a required parameter"),
    INVALID_CLIENT("Unknown client, no client authentication included, or unsupported authentication method"),
    INVALID_GRAND("The provided authorization grant or refresh token is invalid"),
    UNAUTHORIZED_CLIENT("The authenticated client is not authorized to use this authorization grant type"),
    UNSUPPORTED_GRANT_TYPE("The authorization grant type is not supported by the authorization server"),
    INVALID_SCOPE("The requested scope is invalid"),
    INVALID_TOKEN_RESPONSE("The received access token response is not valid"),
    INVALID_METHOD("The requested method is not valid for this authorization"),
    INVALID_CONTENT_TYPE("Invalid content type for this authorization");
}