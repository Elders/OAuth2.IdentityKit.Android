package com.eldersoss.identitykit;

import android.util.Base64;

import com.eldersoss.identitykit.authorization.Authorizer;
import com.eldersoss.identitykit.authorization.BasicAuthorizer;
import com.eldersoss.identitykit.network.IdClient;
import com.eldersoss.identitykit.network.IdRequest;
import com.eldersoss.identitykit.network.IdResponse;
import com.eldersoss.identitykit.oauth2.Token;
import com.eldersoss.identitykit.oauth2.flows.AuthorizationFlow;
import com.eldersoss.identitykit.oauth2.flows.ClientCredentialsFlow;
import com.eldersoss.identitykit.storage.TokenStorage;

import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by IvanVatov on 8/23/2017.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Base64.class)
public class NetworkingTest implements TokenStorage {
    private Token token;

    enum ResponseCase {
        OK200,
        NONE;
    }

//    @Before
//    public void setup() {
//        PowerMockito.mock(Base64.class);
//    }

    @Test
    public void addition_isCorrect() throws Exception {

//        Mockito.mock(Base64.class);
//        Base64 base64 = Mockito.mock(Base64.class);
//        when(base64.encodeToString()).thenReturn("gtg");
        PowerMockito.mock(Base64.class);

        Authorizer authorizer = new BasicAuthorizer("gg", "gg");
        AuthorizationFlow flow = new ClientCredentialsFlow("read", authorizer, this);
        IdClient clien = new TestNetworkClient();
        IdentityKit kit = new IdentityKit("http://foo.bar", flow, authorizer, clien, this);

    }

    public class TestNetworkClient implements IdClient {

        public ResponseCase responseCase = ResponseCase.OK200;

        @Override
        public void execute(IdRequest request) {
            switch (responseCase) {
                case OK200: {
                    request.getListener().onSuccess(response200());
                }
            }
        }

        void setCase(ResponseCase responseCase) {
            this.responseCase = responseCase;
        }

        IdResponse response200() {
            IdResponse response = new IdResponse();
            response.setStatusCode(200);
            Map<String, String> headers = new HashMap();
            headers.put("Cache-Control", "no-store, no-cache, max-age=0, private");
            headers.put("Pragma", "no-cache");
            headers.put("Content-Length", "1000");
            headers.put("Content-Type", "application/json; charset=utf-8");
            headers.put("Server", "Microsoft-IIS/10.0");
            headers.put("X-AspNet-Version", "4.0.30319");
            headers.put("X-Powered-By", "ASP.NET");
            headers.put("Date", "Tue, 22 Aug 2017 12:00:00 GMT");
            headers.put("Connection", "Keep-alive");
            response.setHeaders(headers);
            byte[] body = "{\"access_token\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpZCI6IjYzMjIwNzg0YzUzODA3ZjVmZTc2Yjg4ZjZkNjdlMmExZTIxODlhZTEiLCJjbGllbnRfaWQiOiJUZXN0IENsaWVudCBJRCIsInVzZXJfaWQiOm51bGwsImV4cGlyZXMiOjEzODAwNDQ1NDIsInRva2VuX3R5cGUiOiJiZWFyZXIiLCJzY29wZSI6bnVsbH0.PcC4k8Q_etpU-J4yGFEuBUdeyMJhtpZFkVQ__sXpe78eSi7xTniqOOtgfWa62Y4sj5Npta8xPuDglH8Fueh_APZX4wGCiRE1P4nT4APQCOTbgcuCNXwjmP8znk9F76ID2WxThaMbmpsTTEkuyyUYQKCCdxlIcSbVvcLZUGKZ6-geyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ\",\"expires_in\":3600,\"token_type\":\"Bearer\",\"refresh_token\":\"4f2aw4gf5ge0c3aa3as2e4f8a958c6\"}".getBytes();
            response.setData(body);
            return response;
        }

    }


    @Override
    public void onSuccess(IdResponse networkResponse) {

    }

    @Override
    public void onError(String message) {

    }

    @Nullable
    @Override
    public Token readToken() {
        return token;
    }

    @Override
    public void deleteToken() {
        token = null;
    }

    @Override
    public void writeToken(Token token) {
        this.token = token;
    }
}