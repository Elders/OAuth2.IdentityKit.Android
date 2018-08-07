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

import kotlin.collections.HashMap

/**
 * Sample network request, the library have ability to authorize this type of network request
 * @property method String request method.
 * @property url String url address
 * @property headers key, value HashMap of headers
 * @property body usually UTF-8 encoded
 * @constructor Network request
 */
open class NetworkRequest(val method: String, val priority: Priority, var url: String, var headers: HashMap<String, String>, var body: ByteArray) {

    var bodyContentType = BODY_CONTENT_TYPE

    //TODO: add necessary parameters for priority, retry and etc

    enum class Priority {
        LOW,
        NORMAL,
        HIGH,
        IMMEDIATE
    }
}

const val BODY_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8"

const val DEFAULT_CHARSET = "UTF-8"
