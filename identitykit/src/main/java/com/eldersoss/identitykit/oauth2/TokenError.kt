package com.eldersoss.identitykit.oauth2


/**
 * Created by IvanVatov on 8/17/2017.
 */
class TokenError(val errorMessage: String) {
    fun getMessage(): String{
        when(errorMessage) {
            "invalid_request" -> return ""
            "invalid_client" -> return ""
            "invalid_grant" -> return ""
            "unauthorized_client" -> return ""
            "unsupported_grant_type" -> return ""
            "invalid_scope" -> return ""
            else -> return "Unknown error"
        }
    }
}