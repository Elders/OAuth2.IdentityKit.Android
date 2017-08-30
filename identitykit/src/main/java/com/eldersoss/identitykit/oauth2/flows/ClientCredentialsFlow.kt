package com.eldersoss.identitykit.oauth2.flows

import android.net.Uri
import com.eldersoss.identitykit.authorization.Authorizer
import com.eldersoss.identitykit.network.NetworkClient
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.oauth2.Token
import com.eldersoss.identitykit.oauth2.TokenError

/**
 * Created by IvanVatov on 8/17/2017.
 */
class ClientCredentialsFlow(val tokenEndPoint: String, val scope: String, val authorizer: Authorizer, val networkClient: NetworkClient) : AuthorizationFlow {
    override fun authenticate(callback: (Token?, TokenError?) -> Unit) {
        val uriScope = Uri.encode(scope)
        val request = NetworkRequest("POST", tokenEndPoint, HashMap(), "grant_type=client_credentials&scope=$uriScope")
        authorizeAndPerform(request, authorizer, networkClient, callback)
    }
}