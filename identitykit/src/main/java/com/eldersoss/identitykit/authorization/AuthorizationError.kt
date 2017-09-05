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

import com.eldersoss.identitykit.oauth2.Error

/**
 * Created by IvanVatov on 9/4/2017.
 */
enum class AuthorizationError(val errorMessage: String?) : Error {
    invalid_method("invalid_method"),
    invalid_content_type("invalid_content_type"),
    unknown(null);

    override fun getMessage(): String{
        return when(errorMessage) {
            "invalid_method" -> "The requested method is not valid for this authorization"
            "invalid_content_type" -> "Invalid content type for this authorization"
            else -> "Unknown error"
        }
    }
}