package com.eldersoss.identitykit.storage

import com.eldersoss.identitykit.authorization.Authorizer
import com.eldersoss.identitykit.authorization.BearerAutorizer
import com.eldersoss.identitykit.oauth2.Token
import com.eldersoss.identitykit.oauth2.TokenRefresher
import com.eldersoss.identitykit.oauth2.flows.AuthorizationFlow

/**
 * Created by IvanVatov on 8/30/2017.
 */
class IdentityKit(val flow: AuthorizationFlow, val tokenAuthorizationProvider: (Token) -> Authorizer, val refresher: TokenRefresher, val storage: TokenStorage?) {

    constructor(flow: AuthorizationFlow, tokenAuthorizationMethod: BearerAutorizer.Method, refresher: TokenRefresher, storage: TokenStorage?) :
            this(
                    flow, { token -> BearerAutorizer(tokenAuthorizationMethod, token) }, refresher, storage
            )

}