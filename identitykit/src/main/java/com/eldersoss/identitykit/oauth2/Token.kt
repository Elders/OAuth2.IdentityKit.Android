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

package com.eldersoss.identitykit.oauth2

import com.eldersoss.identitykit.errors.OAuth2InvalidTokenResponseError
import com.eldersoss.identitykit.ext.getOptString
import org.json.JSONObject

/**
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-5.1">Successful Response</a>
 *  @constructor Access token object
 */
class Token(val jsonObject: JSONObject) {
    val accessToken: String
    val tokenType: String
    val expiresIn: Long
    val refreshToken: String?
    val scope: String?

    private val creationTime = System.currentTimeMillis() / 1000

    init {
        try {
            accessToken = jsonObject.getString("access_token")
            tokenType = jsonObject.getString("token_type")
            expiresIn = jsonObject.getLong("expires_in")
            //optional fields
            refreshToken = jsonObject.getOptString("refresh_token")
            scope = jsonObject.getOptString("scope")
        } catch (e: Throwable) {
            throw OAuth2InvalidTokenResponseError()
        }
    }

    val isExpired: Boolean
        get() = System.currentTimeMillis() / 1000 > creationTime + expiresIn
}