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

package com.eldersoss.identitykit

/**
 * Created by IvanVatov on 8/24/2017.
 */

typealias Credentials = (username: String, password: String) -> Unit
typealias Username = String
typealias Password = String

/**
 * Required for Bearer authorization
 */
interface CredentialsProvider {

    fun provideCredentials(handler: Credentials)

    fun onAuthenticationException(throwable: Throwable)
}