package com.eldersoss.identitykit.network

import android.net.Uri

class ParamsBuilder {

    private val paramsList = ArrayList<Pair<String, String>>()

    fun add(key: String, value: String): ParamsBuilder {
        paramsList.add(Pair(Uri.encode(key), Uri.encode(value)))
        return this
    }

    fun build(): String {
        val sb = StringBuilder()
        for (i in paramsList.indices) {
            sb.append(paramsList[i].first)
            sb.append("=")
            sb.append(paramsList[i].second)
            if (i < paramsList.lastIndex) {
                sb.append("&")
            }
        }
        return sb.toString()
    }
}