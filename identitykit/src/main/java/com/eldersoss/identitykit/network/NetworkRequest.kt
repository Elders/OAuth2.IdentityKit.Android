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
 * @property method NetworkRequest.Method.
 * @property url String url address
 * @property headers key, value HashMap of headers
 * @property body usually UTF-8 encoded
 * @constructor Network request
 */
open class NetworkRequest(
    val method: Method,
    val priority: Priority,
    var url: String,
    var headers: HashMap<String, String>,
    var body: ByteArray?
) {

    constructor(method: Method, priority: Priority, url: String) :
            this(method, priority, url, HashMap(), null)

    constructor(method: Method, priority: Priority, url: String, headers: HashMap<String, String>) :
            this(method, priority, url, headers, null)

    constructor(method: Method, priority: Priority, url: String, body: ByteArray) :
            this(method, priority, url, HashMap(), body)

    val contentType: String
        get() = headers["Content-Type"] ?: DEFAULT_BODY_CONTENT_TYPE


    //TODO: add necessary parameters for priority, retry and etc

    enum class Priority {
        LOW,
        NORMAL,
        HIGH,
        IMMEDIATE
    }

    enum class Method(val value: Int) {
        GET(0),
        POST(1),
        PUT(2),
        DELETE(3),
        HEAD(4),
        OPTIONS(5),
        TRACE(6),
        PATCH(7)
    }
}

const val DEFAULT_BODY_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8"

val DEFAULT_CHARSET = charset("UTF-8")
