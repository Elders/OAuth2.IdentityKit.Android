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

import com.eldersoss.identitykit.authorization.Authorizer;
import com.eldersoss.identitykit.authorization.BearerAuthorizer;
import com.eldersoss.identitykit.network.NetworkRequest;
import com.eldersoss.identitykit.oauth2.Token;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.UnsupportedEncodingException;

import static com.eldersoss.identitykit.network.NetworkRequestKt.DEFAULT_CHARSET;
import static org.junit.Assert.assertTrue;

/**
 * Created by IvanVatov on 9/11/2017.
 */
@RunWith(RobolectricTestRunner.class)
public class BearerAuthorizerTest {

    @Test
    public void headerAuthorizationTest() {

        Token token = new Token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9", "Bearer", 3600, "4f2aw4gf5ge0c3aa3as2e4f8a958c6", null);
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

        Token token = new Token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9", "Bearer", 3600, "4f2aw4gf5ge0c3aa3as2e4f8a958c6", null);
        NetworkRequest request = new NetworkRequest(NetworkRequest.Method.GET, NetworkRequest.Priority.HIGH, "https://account.foo.bar/profile");
        
        Authorizer authorizer = new BearerAuthorizer(BearerAuthorizer.Method.BODY, token);
        authorizer.authorize(request);
                
        String authValue = null;
        
        try {
           
            authValue = new String(request.getBody(), DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            
            e.printStackTrace();
        }

        String responseAuthorization = "access_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9";

        assert authValue != null;
        assertTrue(authValue.contains(responseAuthorization));
    }

    @Test
    public void queryAuthorizationTest() {

        Token token = new Token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9", "Bearer", 3600, "4f2aw4gf5ge0c3aa3as2e4f8a958c6", null);

        NetworkRequest request = new NetworkRequest(NetworkRequest.Method.GET, NetworkRequest.Priority.HIGH, "https://account.foo.bar/profile");
        Authorizer authorizer = new BearerAuthorizer(BearerAuthorizer.Method.QUERY, token);
        authorizer.authorize(request);

        String requestUrl = request.getUrl();
        String responseAuthorization = "access_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9";
        assertTrue(requestUrl.contains(responseAuthorization));
    }

}
