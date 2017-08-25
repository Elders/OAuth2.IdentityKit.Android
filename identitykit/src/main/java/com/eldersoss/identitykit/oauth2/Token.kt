package com.eldersoss.identitykit.oauth2

/**
 * Created by IvanVatov on 8/21/2017.
 */
class Token(val accessToken: String,
            val tokenType: String,
            val expiresIn: Long,
            val refreshToken: String?,
            val scope: String?)