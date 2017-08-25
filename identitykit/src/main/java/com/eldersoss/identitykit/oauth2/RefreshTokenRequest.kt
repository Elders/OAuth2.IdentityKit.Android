package com.eldersoss.identitykit.oauth2

import com.eldersoss.identitykit.network.IdRequest

/**
 * Created by IvanVatov on 8/23/2017.
 */
class RefreshTokenRequest(token: Token, method: Method, url: String, headers: HashMap<String, String>, body: String) : IdRequest(method, url, headers, body){
    init {
        val refreshToken = token.refreshToken
        this.body = "grant_type=refresh_token&refresh_token=$refreshToken"
    }
}
