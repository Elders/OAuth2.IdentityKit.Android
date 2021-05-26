package com.eldersoss.identitykit.errors

class OAuth2InvalidScopeError : OAuth2Error() {

    override val errorType: OAuth2ErrorType = OAuth2ErrorType.INVALID_SCOPE
}