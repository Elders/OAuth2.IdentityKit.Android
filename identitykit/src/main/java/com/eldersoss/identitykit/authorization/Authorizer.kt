package com.eldersoss.identitykit.authorization

import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.oauth2.TokenError

/**
 * Created by IvanVatov on 8/21/2017.
 */
interface Authorizer{
    fun authorize(request : NetworkRequest, handler: (NetworkRequest, TokenError?) -> Unit)
}