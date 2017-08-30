package com.eldersoss.identitykit;

import android.os.Handler;

import com.eldersoss.identitykit.network.NetworkClient;
import com.eldersoss.identitykit.network.NetworkRequest;
import com.eldersoss.identitykit.network.NetworkResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IvanVatov on 8/28/2017.
 */

public class TestNetworkClient implements NetworkClient {

    private ResponseCase responseCase;

    enum ResponseCase {
        OK200,
        NONE;
    }

    @Override
    public void execute(final NetworkRequest request) {
        // case resource owner flow token request
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (request.getMethod() == NetworkRequest.Method.POST && request.getBody().equalsIgnoreCase("grant_type=password&username=gg@eldersoss.com&password=ggPass123&scope=read%20write%20openid%20email%20profile%20offline_access%20owner")) {
                    switch (responseCase) {
                        case OK200: {
                            request.getOnResponse().invoke(response200(), null);
                        }
                    }
                } else {
                    request.getOnResponse().invoke(responseProfile(), null);
                }
            }
        }, 200);
    }

    public void setCase(ResponseCase responseCase) {
        this.responseCase = responseCase;
    }

    NetworkResponse response200() {
        NetworkResponse response = new NetworkResponse();
        response.setStatusCode(200);
        Map<String, String> headers = new HashMap();
        putStandartHeaders(headers);
        response.setHeaders(headers);
        byte[] body = "{\"access_token\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpZCI6IjYzMjIwNzg0YzUzODA3ZjVmZTc2Yjg4ZjZkNjdlMmExZTIxODlhZTEiLCJjbGllbnRfaWQiOiJUZXN0IENsaWVudCBJRCIsInVzZXJfaWQiOm51bGwsImV4cGlyZXMiOjEzODAwNDQ1NDIsInRva2VuX3R5cGUiOiJiZWFyZXIiLCJzY29wZSI6bnVsbH0.PcC4k8Q_etpU-J4yGFEuBUdeyMJhtpZFkVQ__sXpe78eSi7xTniqOOtgfWa62Y4sj5Npta8xPuDglH8Fueh_APZX4wGCiRE1P4nT4APQCOTbgcuCNXwjmP8znk9F76ID2WxThaMbmpsTTEkuyyUYQKCCdxlIcSbVvcLZUGKZ6-geyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ\",\"expires_in\":3600,\"token_type\":\"Bearer\",\"refresh_token\":\"4f2aw4gf5ge0c3aa3as2e4f8a958c6\"}".getBytes();
        response.setData(body);
        return response;
    }

    NetworkResponse responseProfile() {
        NetworkResponse response = new NetworkResponse();
        response.setStatusCode(200);
        Map<String, String> headers = new HashMap();
        putStandartHeaders(headers);
        response.setHeaders(headers);
        byte[] body = "{\"result\": [{\"type\": \"profileid\",\"value\": \"123\"},{\"type\": \"name\",\"value\": \"Identity Kit\"}]}".getBytes();
        response.setData(body);
        return response;
    }


    private void putStandartHeaders(Map<String, String> headers){
        headers.put("Cache-Control", "no-store, no-cache, max-age=0, private");
        headers.put("Pragma", "no-cache");
        headers.put("Content-Length", "1000");
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("Server", "Microsoft-IIS/10.0");
        headers.put("X-AspNet-Version", "4.0.30319");
        headers.put("X-Powered-By", "ASP.NET");
        headers.put("Date", "Tue, 22 Aug 2017 12:00:00 GMT");
        headers.put("Connection", "Keep-alive");
    }

}
