package com.eldersoss.identitykit.oauth2.flows

import com.eldersoss.identitykit.authorization.Authorizer
import com.eldersoss.identitykit.network.NetworkClient
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.network.NetworkResponse

/**
 * Created by IvanVatov on 8/17/2017.
 */
interface AuthorizationFlow {
    fun authenticate(callback: (NetworkResponse) -> Unit)
}

fun authorizeAndPerform(request: NetworkRequest, authorizer: Authorizer, networkClient: NetworkClient, callback: (NetworkResponse) -> Unit) {
    authorizer.authorize(request, { networkRequest: NetworkRequest, error ->
        if (error == null) {
            networkClient.execute(networkRequest, { networkResponse ->
                callback(networkResponse)
            })
        } else {
            var errorResponse = NetworkResponse()
            errorResponse.error = error
            callback(errorResponse)
        }
    })
}
