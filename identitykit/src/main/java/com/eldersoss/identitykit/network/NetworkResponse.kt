package com.eldersoss.identitykit.network

import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset

/**
 * Created by IvanVatov on 8/17/2017.
 */
class NetworkResponse {

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
        return data?.toString(Charset.forName("UTF-8"))
    }
}