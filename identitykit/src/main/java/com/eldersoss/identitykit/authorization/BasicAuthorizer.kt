package com.eldersoss.identitykit.authorization

import android.util.Base64
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.oauth2.Error

/**
 * Created by IvanVatov on 8/21/2017.
 */
class BasicAuthorizer(val userName : String, val password : String) : Authorizer {

    override fun authorize(request: NetworkRequest, handler: (NetworkRequest, Error?) -> Unit){
        request.headers.put("Authorization", "Basic " + encodeString("$userName:$password"))
        handler(request, null)
    }

    fun encodeString(str : String) : String{
        return Base64.encodeToString(String.format(str).toByteArray(), Base64.DEFAULT)
    }
}