package com.eldersoss.identitykit.network.volley

import com.eldersoss.identitykit.Error

/**
 * Created by IvanVatov on 9/1/2017.
 */
enum class VolleyNetworkError : Error {

    SERVER_ERROR;

    override fun getMessage(): String {
        return "Internal server error"
    }
}