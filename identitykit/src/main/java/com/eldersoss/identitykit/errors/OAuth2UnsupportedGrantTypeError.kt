package com.eldersoss.identitykit.errors

class OAuth2UnsupportedGrantTypeError : OAuth2Error() {

    override val errorType: OAuth2ErrorType = OAuth2ErrorType.UNSUPPORTED_GRANT_TYPE
}