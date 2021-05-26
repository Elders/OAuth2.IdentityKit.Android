/*
 * Copyright (c) 2017. Elders LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eldersoss.identitykit.authorization;

import com.eldersoss.identitykit.errors.OAuth2AuthorizationInvalidContentTypeError;
import com.eldersoss.identitykit.errors.OAuth2AuthorizationInvalidMethodError;
import com.eldersoss.identitykit.network.NetworkRequest;
import com.eldersoss.identitykit.oauth2.Token;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertTrue;

/**
 * Created by IvanVatov on 9/11/2017.
 */
@RunWith(RobolectricTestRunner.class)
public class BearerAuthorizerTest {


    Token token;

    {
        try {
            token = new Token(new JSONObject("{\n" +
                    "\t\"access_token\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9\",\n" +
                    "\t\"token_type\": \"Bearer\",\n" +
                    "\t\"expires_in\": 3600,\n" +
                    "\t\"refresh_token\": \"4f2aw4gf5ge0c3aa3as2e4f8a958c6\"\n" +
                    "}"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void headerAuthorizationTest() {
        NetworkRequest request = new NetworkRequest(NetworkRequest.Method.GET, NetworkRequest.Priority.HIGH, "https://account.foo.bar/profile");

        Authorizer authorizer = new BearerAuthorizer(BearerAuthorizer.Method.HEADER, token);
        authorizer.authorize(request);

        String authHeaderValue = request.getHeaders().getOrDefault("Authorization", null);
        String responseAuthorization = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9";

        assert authHeaderValue != null;
        assertTrue(authHeaderValue.equalsIgnoreCase(responseAuthorization));
    }

    @Test
    public void bodyAuthorizationTest() {

        NetworkRequest request = new NetworkRequest(NetworkRequest.Method.POST, NetworkRequest.Priority.HIGH, "https://account.foo.bar/profile", "asd=123&gg=asd".getBytes(StandardCharsets.UTF_8));

        Authorizer authorizer = new BearerAuthorizer(BearerAuthorizer.Method.BODY, token);
        authorizer.authorize(request);

        String authValue = null;

        try {

            authValue = new String(request.getBody(), Charset.forName("UTF-8"));
        } catch (Throwable e) {

            e.printStackTrace();
        }

        String responseAuthorization = "asd=123&gg=asd&access_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9";

        assert authValue != null;
        assertTrue(authValue.contains(responseAuthorization));
    }

    @Test
    public void bodyAuthorizationWrongMethodTest() {

        NetworkRequest request = new NetworkRequest(NetworkRequest.Method.GET, NetworkRequest.Priority.HIGH, "https://account.foo.bar/profile", "asd=123&gg=asd".getBytes(StandardCharsets.UTF_8));

        Authorizer authorizer = new BearerAuthorizer(BearerAuthorizer.Method.BODY, token);

        Throwable throwable = null;

        try {
            authorizer.authorize(request);
        } catch (Throwable e) {
            throwable = e;
        }

        Assert.assertTrue(throwable instanceof OAuth2AuthorizationInvalidMethodError);
    }

    @Test
    public void bodyAuthorizationWrongContentTypeTest() {

        NetworkRequest request = new NetworkRequest(NetworkRequest.Method.POST, NetworkRequest.Priority.HIGH, "https://account.foo.bar/profile", "asd=123&gg=asd".getBytes(StandardCharsets.UTF_8));
        request.getHeaders().put("Content-Type", "application/json");

        Authorizer authorizer = new BearerAuthorizer(BearerAuthorizer.Method.BODY, token);

        Throwable throwable = null;

        try {
            authorizer.authorize(request);
        } catch (Throwable e) {
            throwable = e;
        }

        Assert.assertTrue(throwable instanceof OAuth2AuthorizationInvalidContentTypeError);
    }

    @Test
    public void queryAuthorizationTest() {

        String url = "https://account.foo.bar/profile?id=123";

        NetworkRequest request = new NetworkRequest(NetworkRequest.Method.GET, NetworkRequest.Priority.HIGH, url);
        Authorizer authorizer = new BearerAuthorizer(BearerAuthorizer.Method.QUERY, token);
        authorizer.authorize(request);

        String requestUrl = request.getUrl();
        String responseAuthorization = "access_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9";
        Assert.assertEquals(requestUrl, url + "&" + responseAuthorization);
    }

    @Test
    public void queryAuthorizationTest2() {

        String url = "https://account.foo.bar/profile";

        NetworkRequest request = new NetworkRequest(NetworkRequest.Method.GET, NetworkRequest.Priority.HIGH, url);
        Authorizer authorizer = new BearerAuthorizer(BearerAuthorizer.Method.QUERY, token);
        authorizer.authorize(request);

        String requestUrl = request.getUrl();
        String responseAuthorization = "access_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9";
        Assert.assertEquals(requestUrl, url + "?" + responseAuthorization);
    }

}
