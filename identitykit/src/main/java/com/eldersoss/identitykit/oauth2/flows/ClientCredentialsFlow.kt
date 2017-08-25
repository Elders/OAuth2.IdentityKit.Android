package com.eldersoss.identitykit.oauth2.flows

import android.net.Uri
import com.eldersoss.identitykit.authorization.Authorizer
import com.eldersoss.identitykit.network.IdRequest
import com.eldersoss.identitykit.network.IdRequest.Method

/**
 * Created by IvanVatov on 8/17/2017.
 */
class ClientCredentialsFlow(val scope: String, val authorizer: Authorizer) : AuthorizationFlow {
    var endPoint: String = "http://foo.bar"

    override fun setTokenEndPoint(endPoint: String) {
        this.endPoint = endPoint
    }

    override fun authenticate(): IdRequest {
        val body = Uri.encode("grant_type=client_credentials&scope=$scope")
        var headers: HashMap<String, String> = HashMap()
        return IdRequest(Method.POST, endPoint, headers, body)
    }
}