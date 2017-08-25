package com.eldersoss.identitykit.oauth2.flows

import android.net.Uri
import com.eldersoss.identitykit.authorization.Authorizer
import com.eldersoss.identitykit.network.IdRequest

/**
 * Created by IvanVatov on 8/22/2017.
 */
class ResourceOwnerFlow(scope: String, val authorizer: Authorizer) : AuthorizationFlow {

    var username : String? = null
    var password : String? = null

    internal var uriScope = Uri.encode(scope)

    var endPoint: String = "http://foo.bar/"
    override fun setTokenEndPoint(endPoint: String) {
        this.endPoint = endPoint
    }

    override fun authenticate(): IdRequest {
        val body = "grant_type=password&username=$username&password=$password&scope=$uriScope"
        var headers: HashMap<String, String> = HashMap()
        var request = IdRequest(IdRequest.Method.POST, endPoint, headers, body)
        authorizer.authorize(request)
        return request
    }


}