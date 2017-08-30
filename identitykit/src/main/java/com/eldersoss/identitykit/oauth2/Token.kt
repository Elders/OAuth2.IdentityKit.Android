package com.eldersoss.identitykit.oauth2

/**
 * Created by IvanVatov on 8/21/2017.
 */
// https://tools.ietf.org/html/rfc6749#section-5.1
data class Token(val accessToken: String,
            val tokenType: String,
            val expiresIn: Long,
            val refreshToken: String?,
            val scope: String?)