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

    var authorizeAndExecuteQueue: Queue<IdRequest> = ArrayDeque<IdRequest>()
    var authorizeQueue: Queue<AuthorizationObject> = ArrayDeque<AuthorizationObject>()

    data class AuthorizationObject(val request: IdRequest, val callback: () -> Unit)

    init {
        flow.setTokenEndPoint(tokenEndPoint)
        tokenRefresher?.setDependencies(credentialsProvider, tokenEndPoint, storage, client, flow)
    }

    fun authorize(request: IdRequest, callback: () -> Unit) {
        when (flow) {
            is ResourceOwnerFlow -> doResourceOwnerFlowAuthorization(request, callback)
            is ClientCredentialsFlow -> doClientCredentialsFlowAuthorization(request, callback)
        }
    }

    fun authorizeAndExecute(request: IdRequest) {
        when (flow) {
            is ResourceOwnerFlow -> doResourceOwnerFlow(request)
            is ClientCredentialsFlow -> doClientCredentialsFlow(request)
        }
    }

    private fun authorizeAndExecuteQueue(token: Token) {
        var request: IdRequest?
        synchronized(authorizeAndExecuteQueue) {
            (authorizer as BearerAutorizer).setToken(token)
            while (!authorizeAndExecuteQueue.isEmpty()) {
                request = authorizeAndExecuteQueue.poll()
                if (request != null) {
                    authorizer.authorize(request!!)
                    client.execute(request!!)
                }
            }
        }
    }

    private fun authorizeQueue(token: Token) {
        var authorizationObject: AuthorizationObject?
        synchronized(authorizeQueue) {
            (authorizer as BearerAutorizer).setToken(token)
            while (!authorizeQueue.isEmpty()) {
                authorizationObject = authorizeQueue.poll()
                if (authorizationObject?.request != null) {
                    authorizer.authorize(authorizationObject?.request!!)
                    authorizationObject?.callback?.invoke()
                }
            }
        }
    }

    private fun doResourceOwnerFlow(request: IdRequest) {
        if (tokenRefresher?.getValidToken { token, error ->
            when {
                token != null -> authorizeAndExecuteQueue(token)
                error != null -> request.onResponse(null, error)
            }
        } == null) {
            synchronized(authorizeAndExecuteQueue) {
                authorizeAndExecuteQueue.add(request)
            }
        } else {
            authorizer.authorize(request)
            client.execute(request)
        }
    }

    private fun doClientCredentialsFlow(request: IdRequest) {
        //TODO implement this

    }

    private fun doClientCredentialsFlowAuthorization(request: IdRequest, callback: () -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun doResourceOwnerFlowAuthorization(request: IdRequest, callback: () -> Unit) {
        if (tokenRefresher?.getValidToken { token, error ->
            when {
                token != null -> authorizeQueue(token)
                error != null -> request.onResponse(null, error)
            }
        } == null) {
            synchronized(authorizeQueue) {
                authorizeQueue.add(AuthorizationObject(request, callback))
            }
        } else {
            authorizer.authorize(request)
            callback.invoke()
        }
    }
}