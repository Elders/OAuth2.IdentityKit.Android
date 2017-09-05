package com.eldersoss.identitykit;

import android.os.Handler;

import com.eldersoss.identitykit.network.NetworkClient;
import com.eldersoss.identitykit.network.NetworkRequest;
import com.eldersoss.identitykit.network.NetworkResponse;
import com.eldersoss.identitykit.network.volley.VolleyNetworkError;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import static com.eldersoss.identitykit.network.NetworkRequestKt.DEFAULT_CHARSET;

/**
 * Created by IvanVatov on 8/28/2017.
 */

public class TestNetworkClient implements NetworkClient {

    private ResponseCase responseCase;

    enum ResponseCase {
        OK200,
        REFRESH200,
        BAD400,
        NONE;
    }

    @Override
    public void execute(final NetworkRequest request, final Function1<? super NetworkResponse, Unit> callback) {
        // case resource owner flow token request
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String bodyString = "";
                try {
                    bodyString = new String(request.getBody(), DEFAULT_CHARSET);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (request.getMethod() == "POST"
                        && bodyString.equalsIgnoreCase("grant_type=password&username=gg@eldersoss.com&password=ggPass123&scope=read%20write%20openid%20email%20profile%20offline_access%20owner")
                        && request.getHeaders().get("Authorization").equalsIgnoreCase("Basic Y2xpZW50OnNlY3JldA==\n")) {
                    switch (responseCase) {
                        case OK200: {
                            callback.invoke(response200());
                            break;
                        }
                        case BAD400: {
                            callback.invoke(response400());
                            break;
                        }
                        default: {
                            callback.invoke(internalServerError());
                        }
                    }
                    //refresh token response
                } else if (request.getMethod() == "POST"
                        && bodyString.equalsIgnoreCase("grant_type=refresh_token&refresh_token=4f2aw4gf5ge0c3aa3as2e4f8a958c6")
                        && request.getHeaders().get("Authorization").equalsIgnoreCase("Basic Y2xpZW50OnNlY3JldA==\n")) {
                    switch (responseCase) {
                        case OK200: {
                            callback.invoke(response200());
                            break;
                        }
                        case REFRESH200: {
                            callback.invoke(response200refresh());
                            break;
                        }
                        case BAD400: {
                            callback.invoke(response400());
                            break;
                        }
                        default: {
                            callback.invoke(internalServerError());
                        }
                    }
                    //client credentials response
                } else if (request.getMethod() == "POST"
                        && bodyString.equalsIgnoreCase("grant_type=client_credentials&scope=read%20write%20openid%20email%20profile%20offline_access%20owner")
                        && request.getHeaders().get("Authorization").equalsIgnoreCase("Basic Y2xpZW50OnNlY3JldA==\n")) {
                    switch (responseCase) {
                        case OK200: {
                            callback.invoke(response200());
                            break;
                        }
                        case REFRESH200: {
                            callback.invoke(response200refresh());
                            break;
                        }
                        case BAD400: {
                            callback.invoke(response400());
                            break;
                        }
                        default: {
                            callback.invoke(internalServerError());
                        }
                    }
                    // Profile response
                } else if (request.getMethod() == "GET"
                        && request.getHeaders().get("Authorization").equalsIgnoreCase("Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9")) {
                    callback.invoke(responseProfile());
                    // Network error response
                } else {
                    callback.invoke(internalServerError());
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
        byte[] body = "{\"access_token\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9\",\"expires_in\":3600,\"token_type\":\"Bearer\",\"refresh_token\":\"4f2aw4gf5ge0c3aa3as2e4f8a958c6\"}".getBytes();
        response.setData(body);
        return response;
    }

    NetworkResponse response200refresh() {
        NetworkResponse response = new NetworkResponse();
        response.setStatusCode(200);
        Map<String, String> headers = new HashMap();
        putStandartHeaders(headers);
        response.setHeaders(headers);
        byte[] body = "{\"access_token\":\"TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ\",\"expires_in\":3600,\"token_type\":\"Bearer\",\"refresh_token\":\"4f2aw4gf5ge0c3aa3as2e4f8a958c6\"}".getBytes();
        response.setData(body);
        return response;
    }

    NetworkResponse response400() {
        NetworkResponse response = new NetworkResponse();
        response.setStatusCode(400);
        Map<String, String> headers = new HashMap();
        putStandartHeaders(headers);
        response.setHeaders(headers);
        byte[] body = "{\"error\":\"invalid_grant\"}".getBytes();
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

    NetworkResponse internalServerError() {
        NetworkResponse response = new NetworkResponse();
        response.setError(VolleyNetworkError.server_error);
        response.setStatusCode(500);
        Map<String, String> headers = new HashMap();
        putStandartHeaders(headers);
        response.setHeaders(headers);
        byte[] body = "Internal Server Error".getBytes();
        response.setData(body);
        return response;
    }

    private void putStandartHeaders(Map<String, String> headers) {
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
