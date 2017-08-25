package com.eldersoss.identitykit.storage

import com.eldersoss.identitykit.oauth2.Token

/**
 * Created by IvanVatov on 8/21/2017.
 */
interface TokenStorage {
    fun readToken() : Token?
    fun deleteToken()
    fun writeToken(token : Token)
}