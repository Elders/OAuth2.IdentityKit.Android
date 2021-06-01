package com.eldersoss.identitykit.oauth2.flows

import com.eldersoss.identitykit.*
import com.eldersoss.identitykit.authorization.BasicAuthorizer
import com.eldersoss.identitykit.authorization.BearerAuthorizer
import com.eldersoss.identitykit.network.NetworkClient
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.network.NetworkResponse
import com.eldersoss.identitykit.oauth2.DefaultTokenRefresher
import com.eldersoss.identitykit.storage.REFRESH_TOKEN
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.nio.charset.Charset

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

        runBlocking {
            kit.authorize(request)
        }

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

        runBlocking {
            kit.authorize(request)
        }

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

        runBlocking {
            kit.authorize(request)
        }

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

        networkClient.setCase(MockNetworkClient.ResponseCase.INVALID_GRANT)

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

        runBlocking {
            kit.authorize(request)
        }

        val authHeaderValue = request.headers["Authorization"]
        val responseAuthorization = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9"

        assertTrue(authHeaderValue.equals(responseAuthorization))
    }

    @Test
    fun retryIfExecutedRequestReturnsUnauthorized() = runBlockingTest {


        val profileApiResult =
            "{\"result\": [{\"type\": \"profileid\",\"value\": \"123\"},{\"type\": \"name\",\"value\": \"Identity Kit\"}]}"

        var credentialsRequestedCounter = 0
        var authenticationException: Throwable? = null

        var profileRequestedWithUnauthorizedResponse = false
        var tokenIsRefreshed = false

        val networkClient = object : NetworkClient {
            override suspend fun execute(request: NetworkRequest): NetworkResponse {
                if (request.url == "https://account.foo.bar/token") {
                    if (request.body?.toString(Charset.defaultCharset()) == "grant_type=refresh_token&refresh_token=4f2aw4gf5ge0c3aa3as2e4f8a958c6") {
                        tokenIsRefreshed = true
                    }
                    return NetworkResponse().apply {
                        statusCode = 200
                        headers = MockNetworkClient.putStandardHeaders(mutableMapOf())
                        data =
                            "{\"access_token\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9\",\"expires_in\":3600,\"token_type\":\"Bearer\",\"refresh_token\":\"4f2aw4gf5ge0c3aa3as2e4f8a958c6\"}".toByteArray()
                    }
                } else {
                    if (!profileRequestedWithUnauthorizedResponse) {

                        profileRequestedWithUnauthorizedResponse = true

                        return NetworkResponse().apply {
                            statusCode = 401
                            headers = MockNetworkClient.putStandardHeaders(mutableMapOf())
                        }
                    }

                    return NetworkResponse().apply {
                        statusCode = 200
                        headers = MockNetworkClient.putStandardHeaders(mutableMapOf())
                        data = profileApiResult.toByteArray()
                    }
                }
            }
        }
        val tokenStorage = TestTokenStorage()

        val authorizer = BasicAuthorizer("client", "secret")
        val flow = ResourceOwnerFlow(
            "https://account.foo.bar/token",
            object : CredentialsProvider {
                override fun provideCredentials(handler: Credentials) {

                    credentialsRequestedCounter++
                    handler.invoke("gg@eldersoss.com", "ggPass123")
                }

                override fun onAuthenticationException(throwable: Throwable) {
                    authenticationException = throwable
                }
            },
            "read write openid email profile offline_access owner",
            authorizer,
            networkClient
        )
        val kit = IdentityKit(
            KitConfiguration(
                retryFlowAuthentication = true,
                authenticateOnFailedRefresh = true
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

        var response: NetworkResponse?

        runBlocking {
            response = kit.authorizeAndExecute(request)
        }

        Assert.assertEquals(credentialsRequestedCounter, 1)
        Assert.assertTrue(tokenIsRefreshed)
        Assert.assertNull(authenticationException)
        Assert.assertTrue(profileRequestedWithUnauthorizedResponse)
        Assert.assertEquals(response?.getStringData(), profileApiResult)
    }
}