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
            NETWORK_ERROR -> "There was a network error when performing a request!"
            SERVER_ERROR -> "The server responded with an error response."
            AUTH_FAILURE_ERROR -> "Unauthorized request or invalid authentication credentials for the target resource."
            PARSE_ERROR -> "The server's response could not be parsed."
            NO_CONNECTION_ERROR -> "Cannot connect to Internet. Please check your connection!"
            TIMEOUT_ERROR -> "Connection TimeOut! Please check your internet connection."
        }
    }
}