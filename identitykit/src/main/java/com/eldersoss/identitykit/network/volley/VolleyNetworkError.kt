package com.eldersoss.identitykit.network.volley

import com.eldersoss.identitykit.Error

/**
 * Created by IvanVatov on 9/1/2017.
 */
enum class VolleyNetworkError : Error {

    NETWORK_ERROR,
    SERVER_ERROR,
    AUTH_FAILURE_ERROR,
    PARSE_ERROR,
    NO_CONNECTION_ERROR,
    TIMEOUT_ERROR;

    override fun getMessage(): String {
        return when (this) {
            NETWORK_ERROR -> "Cannot connect to Internet...Please check your connection!"
            SERVER_ERROR -> "The server could not be found. Please try again after some time!"
            AUTH_FAILURE_ERROR -> "Cannot connect to Internet...Please check your connection!"
            PARSE_ERROR -> "Parsing error! Please try again after some time!!"
            NO_CONNECTION_ERROR -> "Cannot connect to Internet...Please check your connection!"
            TIMEOUT_ERROR -> "Connection TimeOut! Please check your internet connection."
        }
    }
}