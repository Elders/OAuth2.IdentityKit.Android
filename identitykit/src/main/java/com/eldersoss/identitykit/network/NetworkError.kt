package com.eldersoss.identitykit.network

import com.eldersoss.identitykit.Error

class NetworkError(private val mes: String?) : Error {

    override fun getMessage(): String {
        if (mes != null) {
            return mes
        }
        return "There is no error message in network response"
    }

}