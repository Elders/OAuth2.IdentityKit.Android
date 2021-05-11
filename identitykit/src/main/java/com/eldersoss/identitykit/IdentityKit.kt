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

package com.eldersoss.identitykit

import com.eldersoss.identitykit.authorization.Authorizer
import com.eldersoss.identitykit.authorization.BearerAuthorizer
import com.eldersoss.identitykit.network.NetworkClient
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.network.NetworkResponse
import com.eldersoss.identitykit.oauth2.Token
import com.eldersoss.identitykit.oauth2.TokenRefresher
import com.eldersoss.identitykit.oauth2.flows.AuthorizationFlow
import com.eldersoss.identitykit.oauth2.parseToken
import com.eldersoss.identitykit.storage.REFRESH_TOKEN
import com.eldersoss.identitykit.storage.TokenStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @property kitConfiguration - configuration
 * @property flow - Desired Oauth2 flow
 * @property tokenAuthorizationProvider - function which return authorizer
 * @property refresher - token refresher must implement TokenRefresher, DefaultTokenRefresher provided
 * @property storage - Key, Value storage
 * @property client Network client with ability to execute NetworkRequest
 * @constructor - the primary constructor is not used currently
 */
class IdentityKit(
    private val kitConfiguration: KitConfiguration,
    private val flow: AuthorizationFlow,
    private val tokenAuthorizationProvider: (Token) -> Authorizer,
    private val refresher: TokenRefresher?,
    private val storage: TokenStorage?,
    private val client: NetworkClient
) {
    /**
     * Constructor that take BearerAuthorizer.Method
     */
    constructor(
        kitConfiguration: KitConfiguration,
        flow: AuthorizationFlow,
        tokenAuthorizationMethod: BearerAuthorizer.Method,
        refresher: TokenRefresher?,
        storage: TokenStorage?,
        client: NetworkClient
    ) :
            this(
                kitConfiguration,
                flow,
                { token -> BearerAuthorizer(tokenAuthorizationMethod, token) },
                refresher,
                storage,
                client
            )

    // Internal for unit test only
    internal constructor(
        kitConfiguration: KitConfiguration,
        flow: AuthorizationFlow,
        tokenAuthorizationMethod: BearerAuthorizer.Method,
        refresher: TokenRefresher?,
        storage: TokenStorage?,
        client: NetworkClient,
        dispatcher: CoroutineDispatcher
    ) :
            this(
                kitConfiguration,
                flow,
                { token -> BearerAuthorizer(tokenAuthorizationMethod, token) },
                refresher,
                storage,
                client
            ) {
        _dispatcher = dispatcher
    }

    @Volatile
    private var _token: Token? = null

    private var _dispatcher: CoroutineDispatcher =
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    /**
     * Authotrize and execute the request with provided network client
     * @param request network request
     */
    suspend fun authorizeAndExecute(request: NetworkRequest): NetworkResponse {
        return withContext(_dispatcher) {
            authorizeOrRefresh(request)
            client.execute(request)
        }
    }

    /**
     * Authotrize and return authorized request
     * @param request network request
     * Possible case callback can return unauthorized request and Error
     */
    suspend fun authorize(request: NetworkRequest) {
        return withContext(_dispatcher) {
            authorizeOrRefresh(request)
        }
    }

    /**
     * Just execute network request request
     * @param request network request
     */
    suspend fun execute(request: NetworkRequest): NetworkResponse {

        return client.execute(request)
    }

    fun revokeAuthentication() {
        storage?.let { storage.delete(REFRESH_TOKEN) }
        this._token = null
    }


    suspend fun getValidToken(): Token? {

        return withContext(_dispatcher) {
            var token: Token? = null

            val validToken = validToken()

            if (validToken != null) {

                validToken
            } else {

                val refreshToken = storage?.read(REFRESH_TOKEN)
                // we have refresh token stored
                if (refreshToken != null && refresher != null) {
                    refresherRefreshToken(refreshToken)
                } else {

                    token = flowAuthenticate()
                }
                token
            }
        }
    }

    /**
     * Perform internal logic to authorize the request
     */
    private suspend fun authorizeOrRefresh(request: NetworkRequest) {


        validToken()?.let {
            tokenAuthorizationProvider(it).authorize(request)
            return
        }

        // we have refresh token stored
        val refreshToken = storage?.read(REFRESH_TOKEN)
        var token = if (refreshToken != null && refresher != null) {

            refresherRefreshToken(refreshToken)
        } else {

            flowAuthenticate()
        }

        token?.let {
            tokenAuthorizationProvider(it).authorize(request)
        }
    }


    /** Use the given flow to obtain access token */
    private suspend fun flowAuthenticate(): Token? {

        var token: Token?

        try {

            token = tryFlowAuthenticate()

        } catch (e: Throwable) {

            if (kitConfiguration.retryFlowAuthentication) {

                if (kitConfiguration.onAuthenticationRetryInvokeCallbackWithFailure) {

                    throw e
                }

                token = tryFlowAuthenticate()
            } else {

                throw e
            }
        }

        return token
    }

    private suspend fun tryFlowAuthenticate(): Token? {

        val networkResponse = flow.authenticate()
        return parseToken(networkResponse)
    }

    /** Refresh access token */
    private suspend fun refresherRefreshToken(refreshToken: String): Token? {

        var renewedToken: Token? = null

        try {

            renewedToken = refresher?.refresh(refreshToken, _token?.scope)
            renewedToken?.let { token ->

                this._token = token

                token.refreshToken?.let { refreshedToken ->

                    storage?.let { storage.write(REFRESH_TOKEN, refreshedToken) }
                }
            }

            return renewedToken

        } catch (e: Throwable) {

            storage?.let { storage.delete(REFRESH_TOKEN) }

            if (kitConfiguration.authenticateOnFailedRefresh) {

                if (kitConfiguration.onAuthenticationRetryInvokeCallbackWithFailure) {

                    throw e
                }

                renewedToken = flowAuthenticate()
            }
        }

        return renewedToken
    }

    private fun validToken(): Token? {
        if (this._token != null && this._token?.expiresIn != null) {
            if (this._token?.expiresIn!! > System.currentTimeMillis() / 1000) {
                return this._token
            }
        }
        return null
    }
}