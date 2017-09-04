package com.eldersoss.identitykit.network.volley

import com.eldersoss.identitykit.oauth2.Error

/**
 * Created by IvanVatov on 9/1/2017.
 */
enum class VolleyNetworkError(private val errorMessage: String?) : Error {

    server_error("server_error");

    override fun getMessage(): String{
        return when(errorMessage) {
            "server_error" -> "Internal server error"
            else -> "Unknown error"
        }
    }
}