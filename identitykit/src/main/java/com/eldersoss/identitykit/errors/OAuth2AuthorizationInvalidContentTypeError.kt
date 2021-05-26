package com.eldersoss.identitykit.errors

class OAuth2AuthorizationInvalidContentTypeError : OAuth2Error() {

    override val errorType: OAuth2ErrorType = OAuth2ErrorType.INVALID_CONTENT_TYPE
}