package com.eldersoss.identitykit.errors

class OAuth2UnauthorizedClientError: OAuth2Error() {

    override val errorType: OAuth2ErrorType = OAuth2ErrorType.UNAUTHORIZED_CLIENT
}