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
class ResourceOwnerFlowFailureTests {

    private val configuration = KitConfiguration(
        retryFlowAuthentication = false,
        authenticateOnFailedRefresh = false
    )

    @Test
    fun invalidGrandTest() = runBlockingTest {

        var error: Throwable? = null

        val kit: IdentityKit
        val networkClient: NetworkClient
        networkClient = MockNetworkClient()

        networkClient.setCase(MockNetworkClient.ResponseCase.INVALID_GRANT)

        val authorizer = BasicAuthorizer("client", "secret")
        val flow = ResourceOwnerFlow(
            "https://account.foo.bar/token",
            object : CredentialsProvider {
                override fun provideCredentials(handler: Credentials) {
                    handler.invoke("gg@eldersoss.com", "ggPass123")
                }

                override fun onAuthenticationException(throwable: Throwable) {
                    error = throwable
                }
            },
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

        Assert.assertTrue(error is OAuth2InvalidGrand)

        Assert.assertTrue(oauth2Exception is OAuth2InvalidGrand)
    }

    @Test
    fun refreshTokenFailureRequestCredentials() = runBlockingTest {

        var credentialsRequested = false

        val kit: IdentityKit
        val networkClient: NetworkClient
        val tokenStorage = TestTokenStorage()
        networkClient = MockNetworkClient()

        tokenStorage.write(REFRESH_TOKEN, "4f2aw4gf5ge0c3aa3as2e4f8a958c6")

        networkClient.setCase(MockNetworkClient.ResponseCase.INVALID_GRANT)

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

        try {

            kit.authorize(request)
        } catch (e: OAuth2InvalidGrand) {


        }


        assertTrue(credentialsRequested)
    }

    @Test
    fun refreshTokenFailureNoInternet() = runBlockingTest {

        var credentialsRequested = false

        val kit: IdentityKit
        val networkClient: NetworkClient
        val tokenStorage = TestTokenStorage()
        networkClient = MockNetworkClient()

        tokenStorage.write(REFRESH_TOKEN, "4f2aw4gf5ge0c3aa3as2e4f8a958c6")

        networkClient.setCase(MockNetworkClient.ResponseCase.NO_INTERNET)

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

        var exception: Throwable? = null

        try {

            kit.authorize(request)
        } catch (e: Throwable) {

            exception = e
        }

        assertTrue(exception !is OAuth2Exception)
        assertTrue(!credentialsRequested)
        assertTrue(tokenStorage.read(REFRESH_TOKEN) != null)
    }

}