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

package com.eldersoss.identitykit;

import com.eldersoss.identitykit.authorization.Authorizer;
import com.eldersoss.identitykit.authorization.BearerAuthorizer;
import com.eldersoss.identitykit.network.NetworkRequest;
import com.eldersoss.identitykit.oauth2.Token;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

import static com.eldersoss.identitykit.network.NetworkRequestKt.DEFAULT_CHARSET;
import static org.junit.Assert.assertTrue;

/**
 * Created by IvanVatov on 9/11/2017.
 */
@RunWith(RobolectricTestRunner.class)
public class BearerAuthorizerTest {


    @Test
    public void headerAuthorizationTest() throws Exception {

        final TestResultHandler handler = new TestResultHandler();
        Token token = new Token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9", "Bearer", 3600, "4f2aw4gf5ge0c3aa3as2e4f8a958c6", null);
        Authorizer authorizer = new BearerAuthorizer(BearerAuthorizer.Method.HEADER, token);
        authorizer.authorize(new NetworkRequest(NetworkRequest.Method.GET, NetworkRequest.Priority.HIGH, "https://account.foo.bar/profile"), new Function2<NetworkRequest, Error, Unit>() {
            @Override
            public Unit invoke(NetworkRequest networkRequest, Error error) {
                handler.value = networkRequest.getHeaders().get("Authorization");
                return null;
            }
        });
        String responseAuthorization = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9";
        assertTrue(handler.value.equalsIgnoreCase(responseAuthorization));
    }

    @Test
    public void bodyAuthorizationTest() throws Exception {

        final TestResultHandler handler = new TestResultHandler();
        Token token = new Token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9", "Bearer", 3600, "4f2aw4gf5ge0c3aa3as2e4f8a958c6", null);
        Authorizer authorizer = new BearerAuthorizer(BearerAuthorizer.Method.BODY, token);
        authorizer.authorize(new NetworkRequest(NetworkRequest.Method.GET, NetworkRequest.Priority.HIGH, "https://account.foo.bar/profile"), new Function2<NetworkRequest, Error, Unit>() {
            @Override
            public Unit invoke(NetworkRequest networkRequest, Error error) {

                try {
                    handler.value = new String(networkRequest.getBody(), DEFAULT_CHARSET);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        String responseAuthorization = "access_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9";
        assertTrue(handler.value.contains(responseAuthorization));
    }

    @Test
    public void queryAuthorizationTest() throws Exception {

        final TestResultHandler handler = new TestResultHandler();
        Token token = new Token("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9", "Bearer", 3600, "4f2aw4gf5ge0c3aa3as2e4f8a958c6", null);
        Authorizer authorizer = new BearerAuthorizer(BearerAuthorizer.Method.QUERY, token);
        authorizer.authorize(new NetworkRequest(NetworkRequest.Method.GET, NetworkRequest.Priority.HIGH, "https://account.foo.bar/profile"), new Function2<NetworkRequest, Error, Unit>() {
            @Override
            public Unit invoke(NetworkRequest networkRequest, Error error) {
                handler.value = networkRequest.getUrl();
                return null;
            }
        });
        String responseAuthorization = "access_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9";
        assertTrue(handler.value.contains(responseAuthorization));
    }

}
