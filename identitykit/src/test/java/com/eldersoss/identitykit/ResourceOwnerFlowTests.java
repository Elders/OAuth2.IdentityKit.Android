package com.eldersoss.identitykit;

import com.eldersoss.identitykit.authorization.Authorizer;
import com.eldersoss.identitykit.authorization.BasicAuthorizer;
import com.eldersoss.identitykit.authorization.BearerAutorizer;
import com.eldersoss.identitykit.network.IdClient;;
import com.eldersoss.identitykit.network.IdRequest;
import com.eldersoss.identitykit.oauth2.TokenRefresher;
import com.eldersoss.identitykit.oauth2.flows.AuthorizationFlow;
import com.eldersoss.identitykit.oauth2.flows.ResourceOwnerFlow;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.util.HashMap;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

import static org.junit.Assert.*;

/**
 * Created by IvanVatov on 8/23/2017.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class ResourceOwnerFlowTests {

    @Test
    public void identityKitInitializationTest() throws Exception {
        IdentityKit kit;
        IdClient networkClient;

        Authorizer authorizer = new BasicAuthorizer("client", "secret");
        AuthorizationFlow flow = new ResourceOwnerFlow("read write openid email profile offline_access owner", authorizer);
        Authorizer tokenAuthorizer = new BearerAutorizer(BearerAutorizer.Method.HEADER);
        networkClient = new TestNetworkClient();
        kit = new IdentityKit("https://account.foo.bar/token", flow, tokenAuthorizer, networkClient, new TestCredentialsProvider(), new TestTokenStorage(), new TokenRefresher(authorizer));
        assertTrue(kit != null);
    }

    @Test
    public void successAuthorizeTest() throws Exception {
        IdentityKit kit;
        IdClient networkClient;

        Authorizer authorizer = new BasicAuthorizer("client", "secret");
        AuthorizationFlow flow = new ResourceOwnerFlow("read write openid email profile offline_access owner", authorizer);
        Authorizer tokenAuthorizer = new BearerAutorizer(BearerAutorizer.Method.HEADER);
        networkClient = new TestNetworkClient();
        kit = new IdentityKit("https://account.foo.bar/token", flow, tokenAuthorizer, networkClient, new TestCredentialsProvider(), new TestTokenStorage(), new TokenRefresher(authorizer));

        ((TestNetworkClient) networkClient).setCase(TestNetworkClient.ResponseCase.OK200);
        final IdRequest request = new IdRequest(IdRequest.Method.GET, "https://foo.bar/api/profile", new HashMap<String, String>(), "");
        kit.authorize(request, new Function0<Unit>() {
            @Override
            public Unit invoke() {
                // check headers contains access token
                String headersAuthorization = request.getHeaders().get("Authorization");
                String responseAuthorization = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpZCI6IjYzMjIwNzg0YzUzODA3ZjVmZTc2Yjg4ZjZkNjdlMmExZTIxODlhZTEiLCJjbGllbnRfaWQiOiJUZXN0IENsaWVudCBJRCIsInVzZXJfaWQiOm51bGwsImV4cGlyZXMiOjEzODAwNDQ1NDIsInRva2VuX3R5cGUiOiJiZWFyZXIiLCJzY29wZSI6bnVsbH0.PcC4k8Q_etpU-J4yGFEuBUdeyMJhtpZFkVQ__sXpe78eSi7xTniqOOtgfWa62Y4sj5Npta8xPuDglH8Fueh_APZX4wGCiRE1P4nT4APQCOTbgcuCNXwjmP8znk9F76ID2WxThaMbmpsTTEkuyyUYQKCCdxlIcSbVvcLZUGKZ6-geyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ";
                assertTrue(headersAuthorization.equalsIgnoreCase(responseAuthorization));
                return null;
            }
        });
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
    }
}