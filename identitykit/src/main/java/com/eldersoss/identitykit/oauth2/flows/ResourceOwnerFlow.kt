package com.eldersoss.identitykit.oauth2.flows

import android.net.Uri
import com.eldersoss.identitykit.CredentialsProvider
import com.eldersoss.identitykit.authorization.Authorizer
import com.eldersoss.identitykit.network.NetworkClient
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.oauth2.Token
import com.eldersoss.identitykit.oauth2.TokenError

/**
 * Created by IvanVatov on 8/22/2017.
 */
class ResourceOwnerFlow(val tokenEndPoint: String, val credentialsProvider: CredentialsProvider, val scope: String, val authorizer: Authorizer, val networkClient: NetworkClient) : AuthorizationFlow {
    override fun authenticate(callback: (Token?, TokenError?) -> Unit) {
        credentialsProvider.provideCredentials { username, password ->
            val uriScope = Uri.encode(scope)
            val request = NetworkRequest("POST", tokenEndPoint, HashMap(), "grant_type=password&username=$username&password=$password&scope=$uriScope")
            authorizeAndPerform(request, authorizer, networkClient, callback)
        }
    }
}