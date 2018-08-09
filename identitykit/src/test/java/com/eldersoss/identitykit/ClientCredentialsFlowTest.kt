package com.eldersoss.identitykit

import com.eldersoss.identitykit.authorization.BasicAuthorizer
import com.eldersoss.identitykit.authorization.BearerAuthorizer
import com.eldersoss.identitykit.network.NetworkClient
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.oauth2.DefaultTokenRefresher
import com.eldersoss.identitykit.oauth2.OAuth2Error
import com.eldersoss.identitykit.oauth2.flows.ClientCredentialsFlow
import com.eldersoss.identitykit.storage.REFRESH_TOKEN
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.HashMap

/**
 * Created by IvanVatov on 11/7/2017.
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class ClientCredentialsFlowTest {

    private val mainLock = java.lang.Object()

    private val configuration = KitConfiguration(false, false, false)

    @Test
    fun identityKitInitializationTest() {
        val kit: IdentityKit?
        val networkClient: NetworkClient

        networkClient = TestNetworkClient()
        val authorizer = BasicAuthorizer("client", "secret")
        val flow = ClientCredentialsFlow("https://account.foo.bar/token", "read write openid email profile offline_access owner", authorizer, networkClient)
        kit = IdentityKit(configuration, flow, BearerAuthorizer.Method.HEADER, DefaultTokenRefresher("https://account.foo.bar/token", networkClient, authorizer), TestTokenStorage(), networkClient)

        kit.authorizeAndExecute(NetworkRequest("GET", NetworkRequest.Priority.HIGH, "https://account.foo.bar/profile", HashMap(), "".toByteArray())) { null }
        assertTrue(kit != null)
    }

    /**
     * Unit test on asynchronous implementation in this depth cannot be provided by currently available test instruments
     */
    @Test
    fun successAuthorizeTest() {

        val handler = TestResultHandler()

        val workerThread = Thread {
            val kit: IdentityKit
            val networkClient: NetworkClient
            networkClient = TestNetworkClient()
            networkClient.setCase(TestNetworkClient.ResponseCase.OK200)

            val authorizer = BasicAuthorizer("client", "secret")
            val flow = ClientCredentialsFlow("https://account.foo.bar/token", "read write openid email profile offline_access owner", authorizer, networkClient)
            kit = IdentityKit(configuration, flow, BearerAuthorizer.Method.HEADER, DefaultTokenRefresher("https://account.foo.bar/token", networkClient, authorizer), TestTokenStorage(), networkClient)

            val request = NetworkRequest("GET", NetworkRequest.Priority.HIGH, "https://account.foo.bar/api/profile", HashMap(), "".toByteArray())
            kit.authorize(request) { networkRequest, error ->
                handler.value = networkRequest.headers["Authorization"]
                null
            }
            synchronized(mainLock) {
                mainLock.wait(1000)
            }
        }
        workerThread.start()
        workerThread.join()

        val responseAuthorization = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9"
        assertTrue(handler.value.equals(responseAuthorization))
    }

    @Test
    fun invalidGrandTest() {

        val handler = TestResultHandler()

        val workerThread = Thread {

            val kit: IdentityKit
            val networkClient: NetworkClient
            networkClient = TestNetworkClient()
            networkClient.setCase(TestNetworkClient.ResponseCase.BAD400)

            // building IdentityKit
            val authorizer = BasicAuthorizer("client", "secret")
            val flow = ClientCredentialsFlow("https://account.foo.bar/token", "read write openid email profile offline_access owner", authorizer, networkClient)
            kit = IdentityKit(configuration, flow, BearerAuthorizer.Method.HEADER, DefaultTokenRefresher("https://account.foo.bar/token", networkClient, authorizer), TestTokenStorage(), networkClient)

            val request = NetworkRequest("GET", NetworkRequest.Priority.HIGH, "https://account.foo.bar/api/profile", HashMap(), "".toByteArray())
            kit.authorize(request) { networkRequest, error ->
                handler.error = error
                null
            }
            synchronized(mainLock) {
                mainLock.wait(1000)
            }
        }

        workerThread.start()
        workerThread.join()
        assertTrue(handler.error == OAuth2Error.INVALID_GRAND)
    }

    /**
     * Unit test on asynchronous implementation in this depth cannot be provided by currently available test instruments
     */
    @Test
    fun refreshToken() {

        val handler = TestResultHandler()

        val workerThread = Thread {
            val kit: IdentityKit
            val networkClient: NetworkClient
            val tokenStorage = TestTokenStorage()
            networkClient = TestNetworkClient()
            tokenStorage.write(REFRESH_TOKEN, "4f2aw4gf5ge0c3aa3as2e4f8a958c6")
            networkClient.setCase(TestNetworkClient.ResponseCase.REFRESH200)

            val authorizer = BasicAuthorizer("client", "secret")
            val flow = ClientCredentialsFlow("https://account.foo.bar/token", "read write openid email profile offline_access owner", authorizer, networkClient)
            kit = IdentityKit(configuration, flow, BearerAuthorizer.Method.HEADER, DefaultTokenRefresher("https://account.foo.bar/token", networkClient, authorizer), tokenStorage, networkClient)

            val request = NetworkRequest("GET", NetworkRequest.Priority.HIGH, "https://account.foo.bar/api/profile", HashMap(), "".toByteArray())
            kit.authorize(request) { networkRequest, error ->
                handler.value = networkRequest.headers["Authorization"]
                null
            }
            synchronized(mainLock) {
                mainLock.wait(1000)
            }
        }
        workerThread.start()
        workerThread.join()

        val responseAuthorization = "Bearer TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ"
        assertTrue(handler.value.equals(responseAuthorization))
    }

}