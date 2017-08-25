package com.eldersoss.identitykit.oauth2.flows

import com.eldersoss.identitykit.network.IdRequest

/**
 * Created by IvanVatov on 8/17/2017.
 */
interface AuthorizationFlow {
    fun setTokenEndPoint(endPoint: String)
    fun authenticate() : IdRequest
}
