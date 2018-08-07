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

package com.eldersoss.identitykit.network

import com.eldersoss.identitykit.Error
import org.json.JSONObject

/**
 * @constructor
 * All the properties of response object are optional
 * @property statusCode - integer HTTP status code of the response
 * @property data - array of bytes usually UTF-8 encoded
 * @property headers - Key, Value headers HashMap
 * @property error - Error object in the response help us to identify the reason of the error
 */
class NetworkResponse {

    var statusCode: Int? = null
    var data: ByteArray? = null
    var headers: Map<String, String>? = null
    var error: Error? = null

    /** @return JSON object if response data is in JSON format or null */
    fun getJson(): JSONObject? {
        var stringData = getStringData()
        if (stringData != null) {
            return try {
                JSONObject(stringData)
            } catch (exception: Throwable) {
                null
            }
        }
        return null
    }

    /** @return response data as String */
    fun getStringData(): String? {
        return data?.toString(charset(DEFAULT_CHARSET))
    }
}