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

package com.eldersoss.identitykit.authorization

import com.eldersoss.identitykit.Error

/**
 * Created by IvanVatov on 9/4/2017.
 */
enum class AuthorizationError : Error {
    INVALID_METHOD,
    INVALID_CONTENT_TYPE;

    override fun getMessage(): String {
        return when (this) {
            INVALID_METHOD -> "The requested method is not valid for this authorization"
            INVALID_CONTENT_TYPE -> "Invalid content type for this authorization"
        }
    }
}