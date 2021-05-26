package com.eldersoss.identitykit.errors

class OAuth2InvalidTokenResponseError: OAuth2Error() {

    override val errorType: OAuth2ErrorType = OAuth2ErrorType.INVALID_TOKEN_RESPONSE
}