package com.eldersoss.identitykit.errors

class OAuth2AuthorizationInvalidMethodError : OAuth2Error() {

    override val errorType: OAuth2ErrorType = OAuth2ErrorType.INVALID_METHOD
}