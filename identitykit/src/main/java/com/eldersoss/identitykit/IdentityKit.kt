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
import com.eldersoss.identitykit.storage.REFRESH_TOKEN
import com.eldersoss.identitykit.storage.TokenStorage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.Exception

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

    @Volatile
    private var _token: Token? = null

    private val mutex = Mutex()

    /**
     * Authorize given request
     */
    suspend fun authorize(request: NetworkRequest) {

        tokenAuthorizationProvider(getValidToken()).authorize(request)
    }

    /**
     * Authorize and execute the request with provided network client
     * @param request network request
     */
    suspend fun authorizeAndExecute(request: NetworkRequest): NetworkResponse {

        authorize(request)
        return client.execute(request)
    }

    /**
     * Just execute network request request
     * @param request network request
     */
    suspend fun execute(request: NetworkRequest): NetworkResponse {

        return client.execute(request)
    }

    /**
     * Get valid token, can be used to perform authentication
     */
    suspend fun getValidToken(): Token {

        mutex.withLock {
            val validToken = validToken()

            return if (validToken != null) {

                validToken
            } else {

                val refreshToken = storage?.read(REFRESH_TOKEN)

                // we have refresh token stored
                _token = if (refreshToken != null && refresher != null) {
                    refresherRefreshToken(refreshToken)
                } else {
                    flowAuthenticate()
                }
                _token ?: throw Exception("Edge case exception")
            }
        }
    }

    /**
     * Clear stored access and refresh tokens
     */
    fun revokeAuthentication() {
        storage?.let { storage.delete(REFRESH_TOKEN) }
        this._token = null
    }

    /** Use the given flow to obtain access token */
    private suspend fun flowAuthenticate(): Token {

        return try {
            tryFlowAuthenticate()
        } catch (e: Throwable) {

            if (kitConfiguration.retryFlowAuthentication) {
                flowAuthenticate()
            } else {
                throw e
            }
        }
    }

    private suspend fun tryFlowAuthenticate(): Token {

        return flow.authenticate()
    }

    /** Refresh access token */
    private suspend fun refresherRefreshToken(refreshToken: String): Token? {

        return try {

            refresher?.refresh(refreshToken, _token?.scope)?.also {

                this._token = it

                it.refreshToken?.let { refreshedToken ->

                    storage?.let { storage.write(REFRESH_TOKEN, refreshedToken) }
                }
            }

        } catch (e: Throwable) {

            storage?.let { storage.delete(REFRESH_TOKEN) }

            if (kitConfiguration.authenticateOnFailedRefresh) {
                flowAuthenticate()
            } else {
                throw e
            }
        }
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