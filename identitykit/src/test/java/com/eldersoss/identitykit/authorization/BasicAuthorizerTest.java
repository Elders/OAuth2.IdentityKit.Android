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

import android.util.Base64;

import com.eldersoss.identitykit.network.NetworkRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import java.nio.charset.Charset;
import java.util.HashMap;
import static org.junit.Assert.assertTrue;

/**
 * Created by IvanVatov on 9/11/2017.
 */

@RunWith(RobolectricTestRunner.class)
public class BasicAuthorizerTest {


    @Test
    public void authorizationTest() throws NullPointerException{

        NetworkRequest request = new NetworkRequest(NetworkRequest.Method.GET, NetworkRequest.Priority.HIGH,
                "https://account.foo.bar/profile", new HashMap<String, String>(), "".getBytes());

        Authorizer authorizer = new BasicAuthorizer("clientid", "clientsecret");
        authorizer.authorize(request);

        String responseAuthorization = "Basic " + Base64.encodeToString("clientid:clientsecret".getBytes(Charset.forName("UTF-8")), Base64.NO_WRAP);

        String authHeaderValue = request.getHeaders().getOrDefault("Authorization", null);

        assert authHeaderValue != null;
        assertTrue(authHeaderValue.equalsIgnoreCase(responseAuthorization));
    }
}
