package com.eldersoss.identitykit.oauth2.flows

import com.eldersoss.identitykit.*
import com.eldersoss.identitykit.authorization.BasicAuthorizer
import com.eldersoss.identitykit.authorization.BearerAuthorizer
import com.eldersoss.identitykit.exceptions.OAuth2Exception
import com.eldersoss.identitykit.exceptions.OAuth2InvalidGrand
import com.eldersoss.identitykit.network.NetworkClient
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.oauth2.DefaultTokenRefresher
import com.eldersoss.identitykit.storage.REFRESH_TOKEN
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Created by IvanVatov on 11/7/2017.
 */

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class ResourceOwnerFlowTest {

    @Test
    fun successAuthorizeTest() = runBlockingTest {

        val responseAuthorization = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9"

        val kit: IdentityKit
        val networkClient: NetworkClient
        networkClient = MockNetworkClient()
        networkClient.setCase(MockNetworkClient.ResponseCase.OK200)

        val authorizer = BasicAuthorizer("client", "secret")
        val flow = ResourceOwnerFlow(
            "https://account.foo.bar/token",
            object : CredentialsProvider {
                override fun provideCredentials(handler: Credentials) {
                    handler.invoke("gg@eldersoss.com", "ggPass123")
                }

                override fun onAuthenticationException(throwable: Throwable) {

                }
            },
            "read write openid email profile offline_access owner",
            authorizer,
            networkClient
        )
        kit = IdentityKit(
            KitConfiguration(
                retryFlowAuthentication = false,
                authenticateOnFailedRefresh = false
            ),
            flow,
            BearerAuthorizer.Method.HEADER,
            DefaultTokenRefresher("https://account.foo.bar/token", networkClient, authorizer),
            TestTokenStorage(),
            networkClient
        )

        val request = NetworkRequest(
            NetworkRequest.Method.GET,
            NetworkRequest.Priority.HIGH,
            "https://account.foo.bar/api/profile"
        )

        kit.authorize(request)

        val authHeaderValue = request.headers["Authorization"]

        assertTrue(authHeaderValue.equals(responseAuthorization))
    }

    @Test
    fun refreshToken() = runBlockingTest {

        val kit: IdentityKit
        val networkClient: NetworkClient
        val tokenStorage = TestTokenStorage()
        networkClient = MockNetworkClient()

        tokenStorage.write(REFRESH_TOKEN, "4f2aw4gf5ge0c3aa3as2e4f8a958c6")

        networkClient.setCase(MockNetworkClient.ResponseCase.REFRESH200)

        val authorizer = BasicAuthorizer("client", "secret")
        val flow = ResourceOwnerFlow(
            "https://account.foo.bar/token",
            object : CredentialsProvider {
                override fun provideCredentials(handler: Credentials) {

                }

                override fun onAuthenticationException(throwable: Throwable) {

                }
            },
            "read write openid email profile offline_access owner",
            authorizer,
            networkClient
        )
        kit = IdentityKit(
            KitConfiguration(
                retryFlowAuthentication = false,
                authenticateOnFailedRefresh = false
            ),
            flow,
            BearerAuthorizer.Method.HEADER,
            DefaultTokenRefresher("https://account.foo.bar/token", networkClient, authorizer),
            tokenStorage,
            networkClient
        )

        val request = NetworkRequest(
            NetworkRequest.Method.GET,
            NetworkRequest.Priority.HIGH,
            "https://account.foo.bar/api/profile"
        )

        kit.authorize(request)

        val authHeaderValue = request.headers["Authorization"]
        val responseAuthorization = "Bearer TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ"

        assertTrue(authHeaderValue.equals(responseAuthorization))
    }

    @Test
    fun refreshTokenFailure() = runBlockingTest {

        var credentialsRequested = false

        val kit: IdentityKit
        val networkClient: NetworkClient
        val tokenStorage = TestTokenStorage()
        networkClient = MockNetworkClient()

        tokenStorage.write(REFRESH_TOKEN, "4f2aw4gf5ge0c3aa3as2e4f8a958c6")

        networkClient.setCase(MockNetworkClient.ResponseCase.REFRESH200)

        val authorizer = BasicAuthorizer("client", "secret")
        val flow = ResourceOwnerFlow(
            "https://account.foo.bar/token",
            object : CredentialsProvider {
                override fun provideCredentials(handler: Credentials) {
                    credentialsRequested = true
                    handler.invoke("gg@eldersoss.com", "ggPass123")
                }

                override fun onAuthenticationException(throwable: Throwable) {

                }
            },
            "read write openid email profile offline_access owner",
            authorizer,
            networkClient
        )
        kit = IdentityKit(
            KitConfiguration(
                retryFlowAuthentication = false,
                authenticateOnFailedRefresh = false
            ),
            flow,
            BearerAuthorizer.Method.HEADER,
            DefaultTokenRefresher("https://account.foo.bar/token", networkClient, authorizer),
            tokenStorage,
            networkClient
        )

        val request = NetworkRequest(
            NetworkRequest.Method.GET,
            NetworkRequest.Priority.HIGH,
            "https://account.foo.bar/api/profile"
        )

        kit.authorize(request)

        val authHeaderValue = request.headers["Authorization"]
        val responseAuthorization = "Bearer TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ"

        assertTrue(!credentialsRequested)

        assertTrue(authHeaderValue.equals(responseAuthorization))
    }

    @Test
    fun retryAuthentication() = runBlockingTest {

        var credentialsRequested = false

        val kit: IdentityKit
        val networkClient: NetworkClient
        val tokenStorage = TestTokenStorage()
        networkClient = MockNetworkClient()

        networkClient.setCase(MockNetworkClient.ResponseCase.BAD400)

        val authorizer = BasicAuthorizer("client", "secret")
        val flow = ResourceOwnerFlow(
            "https://account.foo.bar/token",
            object : CredentialsProvider {
                override fun provideCredentials(handler: Credentials) {
                    if (credentialsRequested) {

                        networkClient.setCase(MockNetworkClient.ResponseCase.OK200)
                    }

                    credentialsRequested = true
                    handler.invoke("gg@eldersoss.com", "ggPass123")
                }

                override fun onAuthenticationException(throwable: Throwable) {

                }
            },
            "read write openid email profile offline_access owner",
            authorizer,
            networkClient
        )
        kit = IdentityKit(
            KitConfiguration(
                retryFlowAuthentication = true,
                authenticateOnFailedRefresh = false
            ),
            flow,
            BearerAuthorizer.Method.HEADER,
            DefaultTokenRefresher("https://account.foo.bar/token", networkClient, authorizer),
            tokenStorage,
            networkClient
        )

        val request = NetworkRequest(
            NetworkRequest.Method.GET,
            NetworkRequest.Priority.HIGH,
            "https://account.foo.bar/api/profile"
        )


        kit.authorize(request)


        val authHeaderValue = request.headers["Authorization"]
        val responseAuthorization = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9"

        assertTrue(authHeaderValue.equals(responseAuthorization))
    }
}