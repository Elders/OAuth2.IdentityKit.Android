package com.eldersoss.identitykit

import com.eldersoss.identitykit.authorization.BasicAuthorizer
import com.eldersoss.identitykit.authorization.BearerAuthorizer
import com.eldersoss.identitykit.exceptions.OAuth2Exception
import com.eldersoss.identitykit.exceptions.OAuth2InvalidGrand
import com.eldersoss.identitykit.network.NetworkClient
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.oauth2.DefaultTokenRefresher
import com.eldersoss.identitykit.oauth2.flows.ResourceOwnerFlow
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

    private val configuration = KitConfiguration(
        retryFlowAuthentication = false,
        authenticateOnFailedRefresh = false,
        onAuthenticationRetryInvokeCallbackWithFailure = false
    )

    @Test
    fun identityKitInitializationTest() = runBlockingTest {

        val kit: IdentityKit?
        val networkClient: NetworkClient

        networkClient = MockNetworkClient()
        networkClient.setCase(MockNetworkClient.ResponseCase.OK200)

        val authorizer = BasicAuthorizer("client", "secret")
        val flow = ResourceOwnerFlow(
            "https://account.foo.bar/token",
            TestCredentialsProvider(),
            "read write openid email profile offline_access owner",
            authorizer,
            networkClient
        )
        kit = IdentityKit(
            configuration,
            flow,
            BearerAuthorizer.Method.HEADER,
            DefaultTokenRefresher("https://account.foo.bar/token", networkClient, authorizer),
            TestTokenStorage(),
            networkClient
        )

        kit.authorizeAndExecute(
            NetworkRequest(
                NetworkRequest.Method.GET,
                NetworkRequest.Priority.HIGH,
                "https://account.foo.bar/profile"
            )
        )

        assertTrue(kit != null)
    }

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
            TestCredentialsProvider(),
            "read write openid email profile offline_access owner",
            authorizer,
            networkClient
        )
        kit = IdentityKit(
            configuration,
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
    fun invalidGrandTest() = runBlockingTest {

        val kit: IdentityKit
        val networkClient: NetworkClient
        networkClient = MockNetworkClient()

        networkClient.setCase(MockNetworkClient.ResponseCase.BAD400)

        val authorizer = BasicAuthorizer("client", "secret")
        val flow = ResourceOwnerFlow(
            "https://account.foo.bar/token",
            TestCredentialsProvider(),
            "read write openid email profile offline_access owner",
            authorizer,
            networkClient
        )
        kit = IdentityKit(
            configuration,
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

        var oauth2Exception: OAuth2Exception? = null

        try {

            kit.authorize(request)
        } catch (e: OAuth2InvalidGrand) {

            oauth2Exception = e
        }

        Assert.assertTrue(oauth2Exception is OAuth2InvalidGrand)
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
            TestCredentialsProvider(),
            "read write openid email profile offline_access owner",
            authorizer,
            networkClient
        )
        kit = IdentityKit(
            configuration,
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
}