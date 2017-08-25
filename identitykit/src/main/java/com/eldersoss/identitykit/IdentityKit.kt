package com.eldersoss.identitykit

import com.eldersoss.identitykit.authorization.Authorizer
import com.eldersoss.identitykit.authorization.BearerAutorizer
import com.eldersoss.identitykit.network.IdClient
import com.eldersoss.identitykit.network.IdRequest
import com.eldersoss.identitykit.oauth2.Token
import com.eldersoss.identitykit.oauth2.TokenRefresher
import com.eldersoss.identitykit.oauth2.flows.AuthorizationFlow
import com.eldersoss.identitykit.oauth2.flows.ClientCredentialsFlow
import com.eldersoss.identitykit.oauth2.flows.ResourceOwnerFlow
import com.eldersoss.identitykit.storage.TokenStorage
import java.util.*

/**
 * Created by IvanVatov on 8/17/2017.
 */
class IdentityKit(val tokenEndPoint: String, var flow: AuthorizationFlow, var authorizer: Authorizer, var client: IdClient, val credentialsProvider: CredentialsProvider, val storage: TokenStorage?, val tokenRefresher: TokenRefresher?) {

    var queue: Queue<IdRequest> = ArrayDeque<IdRequest>()

    init {
        flow.setTokenEndPoint(tokenEndPoint)
        tokenRefresher?.setDependencies(credentialsProvider, tokenEndPoint, storage, client, flow)
    }

    fun authorizeAndExecute(request: IdRequest) {
        when (flow) {
            is ResourceOwnerFlow -> doResourceOwnerFlow(request)
            is ClientCredentialsFlow -> doClientCredentialsFlow(request)
        }
    }

    fun authorizeQueue(token: Token) {
        var request: IdRequest?
        synchronized(queue) {
            while (!queue.isEmpty()) {
                request = queue.poll()
                if (request != null) {
                    authorizer.authorize(request!!)
                    (authorizer as BearerAutorizer).token = token
                    client.execute(request!!)
                }
            }
        }
    }


    private fun doResourceOwnerFlow(request: IdRequest) {
        tokenRefresher?.getValidToken { token, _ ->
            when {
                token != null -> {
                    authorizer.authorize(request)
                    client.execute(request)
                }
                token == null -> {
                    synchronized(queue) { queue.add(request) }
                    tokenRefresher?.onTokenValid = { token, error ->
                        run {
                            when {
                                token != null -> authorizeQueue(token)
                                error != null -> request.onResponse(null, error)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun doClientCredentialsFlow(request: IdRequest) {
        //TODO implement this

    }
}