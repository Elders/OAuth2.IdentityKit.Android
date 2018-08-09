/*
 * Copyright (c) 2018. Elders LTD
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
 * @property authenticateOnAllOAuth2Errors - Request credentials on all Oauth2 errors
 * @property retryFlowAuthentication - if flow authentication failed, this property true will retry authentication
 * @property authenticateOnFailedRefresh - if refreshing failed with OAuth2 error this property true will trigger authentication process
 * @property onAuthenticationRetryInvokeCallbackWithFailure - invoke callback with failure on retrying authentication, it requires retryFlowAuthentication true
 * @constructor - KitConfiguration
 */
data class KitConfiguration(val authenticateOnAllOAuth2Errors: Boolean, val retryFlowAuthentication: Boolean, val authenticateOnFailedRefresh: Boolean, val onAuthenticationRetryInvokeCallbackWithFailure: Boolean)