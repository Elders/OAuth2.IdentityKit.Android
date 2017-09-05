package com.eldersoss.identitykit;

import com.eldersoss.identitykit.authorization.Authorizer;
import com.eldersoss.identitykit.authorization.BasicAuthorizer;
import com.eldersoss.identitykit.authorization.BearerAuthorizer;
import com.eldersoss.identitykit.network.NetworkClient;
import com.eldersoss.identitykit.network.NetworkRequest;
import com.eldersoss.identitykit.network.NetworkResponse;
import com.eldersoss.identitykit.oauth2.DefaultTokenRefresher;
import com.eldersoss.identitykit.oauth2.Error;
import com.eldersoss.identitykit.oauth2.OAuth2Error;
import com.eldersoss.identitykit.oauth2.flows.AuthorizationFlow;
import com.eldersoss.identitykit.oauth2.flows.ClientCredentialsFlow;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.util.HashMap;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

import static com.eldersoss.identitykit.storage.ConstantsKt.REFRESH_TOKEN;
import static org.junit.Assert.assertTrue;

/**
 * Created by IvanVatov on 9/4/2017.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class ClientCredentialsFlowTest {

    @Test
    public void identityKitInitializationTest() throws Exception {
        IdentityKit kit;
        NetworkClient networkClient;

        networkClient = new TestNetworkClient();
        Authorizer authorizer = new BasicAuthorizer("client", "secret");
        AuthorizationFlow flow = new ClientCredentialsFlow("https://account.foo.bar/token", "read write openid email profile offline_access owner", authorizer, networkClient);
        kit = new IdentityKit(flow, BearerAuthorizer.Method.HEADER, new DefaultTokenRefresher("https://account.foo.bar/token", networkClient, authorizer), new TestTokenStorage(), networkClient);

        kit.authorizeAndExecute(new NetworkRequest("GET", "https://account.foo.bar/profile", new HashMap<String, String>(), "".getBytes()), new Function1<NetworkResponse, Unit>() {
            @Override
            public Unit invoke(NetworkResponse networkResponse) {
                return null;
            }
        });
        assertTrue(kit != null);
    }

    @Test
    public void successAuthorizeTest() throws Exception {
        IdentityKit kit;
        NetworkClient networkClient;
        networkClient = new TestNetworkClient();

        // test stuffs
        final TestResultHandler handler = new TestResultHandler();
        ((TestNetworkClient) networkClient).setCase(TestNetworkClient.ResponseCase.OK200);

        Authorizer authorizer = new BasicAuthorizer("client", "secret");
        AuthorizationFlow flow = new ClientCredentialsFlow("https://account.foo.bar/token", "read write openid email profile offline_access owner", authorizer, networkClient);
        kit = new IdentityKit(flow, BearerAuthorizer.Method.HEADER, new DefaultTokenRefresher("https://account.foo.bar/token", networkClient, authorizer), new TestTokenStorage(), networkClient);

        final NetworkRequest request = new NetworkRequest("GET", "https://account.foo.bar/api/profile", new HashMap<String, String>(), "".getBytes());
        kit.authorize(request, new Function2<NetworkRequest, Error, Unit>() {
            @Override
            public Unit invoke(NetworkRequest networkRequest, Error error) {
                handler.value = networkRequest.getHeaders().get("Authorization");
                return null;
            }
        });
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        String responseAuthorization = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9";
        assertTrue(handler.value.equalsIgnoreCase(responseAuthorization));
    }

    @Test
    public void invalidGrandTest() throws Exception {
        IdentityKit kit;
        NetworkClient networkClient;
        networkClient = new TestNetworkClient();

        // test stuffs
        final TestResultHandler handler = new TestResultHandler();
        ((TestNetworkClient) networkClient).setCase(TestNetworkClient.ResponseCase.BAD400);

        // building IdentityKit
        Authorizer authorizer = new BasicAuthorizer("client", "secret");
        AuthorizationFlow flow = new ClientCredentialsFlow("https://account.foo.bar/token", "read write openid email profile offline_access owner", authorizer, networkClient);
        kit = new IdentityKit(flow, BearerAuthorizer.Method.HEADER, new DefaultTokenRefresher("https://account.foo.bar/token", networkClient, authorizer), new TestTokenStorage(), networkClient);

        final NetworkRequest request = new NetworkRequest("GET", "https://account.foo.bar/api/profile", new HashMap<String, String>(), "".getBytes());
        kit.authorize(request, new Function2<NetworkRequest, Error, Unit>() {
            @Override
            public Unit invoke(NetworkRequest networkRequest, Error error) {
                handler.error = error;
                return null;
            }
        });
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        assertTrue(handler.error.equals(OAuth2Error.invalid_grant));
    }

    @Test
    public void refreshToken() throws Exception {
        IdentityKit kit;
        NetworkClient networkClient;
        TestTokenStorage tokenStorage = new TestTokenStorage();
        networkClient = new TestNetworkClient();

        // test stuffs
        final TestResultHandler handler = new TestResultHandler();
        tokenStorage.write(REFRESH_TOKEN, "4f2aw4gf5ge0c3aa3as2e4f8a958c6");
        ((TestNetworkClient) networkClient).setCase(TestNetworkClient.ResponseCase.REFRESH200);

        Authorizer authorizer = new BasicAuthorizer("client", "secret");
        AuthorizationFlow flow = new ClientCredentialsFlow("https://account.foo.bar/token", "read write openid email profile offline_access owner", authorizer, networkClient);
        kit = new IdentityKit(flow, BearerAuthorizer.Method.HEADER, new DefaultTokenRefresher("https://account.foo.bar/token", networkClient, authorizer), tokenStorage, networkClient);

        final NetworkRequest request = new NetworkRequest("GET", "https://account.foo.bar/api/profile", new HashMap<String, String>(), "".getBytes());
        kit.authorize(request, new Function2<NetworkRequest, Error, Unit>() {
            @Override
            public Unit invoke(NetworkRequest networkRequest, Error error) {
                handler.value = networkRequest.getHeaders().get("Authorization");
                return null;
            }
        });
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        String responseAuthorization = "Bearer TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ";
        assertTrue(handler.value.equalsIgnoreCase(responseAuthorization));
    }
}
