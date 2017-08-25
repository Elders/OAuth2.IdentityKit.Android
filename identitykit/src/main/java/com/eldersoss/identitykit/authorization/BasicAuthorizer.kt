package com.eldersoss.identitykit.authorization

import android.util.Base64
import com.eldersoss.identitykit.network.IdRequest

/**
 * Created by IvanVatov on 8/21/2017.
 */
class BasicAuthorizer(val userName : String, val password : String) : Authorizer {

    override fun authorize(request: IdRequest){
        request.headers.put("Authorization", "Basic " + encodeString("$userName:$password"))
    }

    fun encodeString(str : String) : String{
        return Base64.encodeToString(String.format(str).toByteArray(), 0)
    }
}