/*
 * Copyright (c) 2017. Elders LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eldersoss.identitykit.oauth2.flows

import com.eldersoss.identitykit.authorization.Authorizer
import com.eldersoss.identitykit.network.NetworkClient
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.network.NetworkResponse
import com.eldersoss.identitykit.oauth2.OAuth2Error

/**
 * Created by IvanVatov on 8/17/2017.
 */
interface AuthorizationFlow {
    fun authenticate(callback: (NetworkResponse) -> Unit)
}

/**
 * Extension function to help execution of authentication request
 */
fun authorizeAndPerform(request: NetworkRequest, authorizer: Authorizer, networkClient: NetworkClient, callback: (NetworkResponse) -> Unit) {
    authorizer.authorize(request, { networkRequest: NetworkRequest, error ->
        if (error == null) {
            networkClient.execute(networkRequest, { networkResponse ->
                if (networkResponse.statusCode in 400..499) {
                    var errorResponse = NetworkResponse()
                    errorResponse.error = OAuth2Error.get(networkResponse.getJson()?.optString("error"))
                    callback(errorResponse)
                } else {
                    callback(networkResponse)
                }
            })
        } else {
            var errorResponse = NetworkResponse()
            errorResponse.error = error
            callback(errorResponse)
        }
    })
}
