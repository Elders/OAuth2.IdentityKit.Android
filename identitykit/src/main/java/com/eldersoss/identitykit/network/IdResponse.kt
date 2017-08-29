package com.eldersoss.identitykit.network

import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset

/**
 * Created by IvanVatov on 8/17/2017.
 */
class IdResponse {

    var statusCode: Int? = null
    var data: ByteArray? = null
    var headers: Map<String, String> = HashMap()

    fun getJson(): JSONObject? {
        var stringData = getStringData()
        return try {
            JSONObject(stringData)
        } catch (exception: JSONException) {
            null
        }
        return null
    }

    fun getStringData(): String? {
        return data?.toString(Charset.forName(getContentType()))
    }

    fun getContentType(): String {
        val contentType = headers?.get("Content-Type")
        if (contentType != null) {
            val params = contentType!!.split(";".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            for (i in 1..params.size - 1) {
                val pair = params[i].trim({ it <= ' ' }).split("=".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                if (pair.size == 2) {
                    if (pair[0] == "charset") {
                        return pair[1]
                    }
                }
            }
        }
        return "us-ascii"
    }
}