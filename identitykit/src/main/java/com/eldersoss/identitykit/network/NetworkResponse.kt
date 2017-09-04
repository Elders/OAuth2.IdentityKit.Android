package com.eldersoss.identitykit.network

import com.eldersoss.identitykit.oauth2.Error
import org.json.JSONObject
import java.nio.charset.Charset

/**
 * Created by IvanVatov on 8/17/2017.
 */
class NetworkResponse {

    var statusCode: Int? = null
    var data: ByteArray? = null
    var headers: Map<String, String>? = null
    var error: Error? = null

    fun getJson(): JSONObject? {
        var stringData = getStringData()
        if (stringData != null) {
            return try {
                JSONObject(stringData)
            } catch (exception: Throwable) {
                null
            }
        }
        return null
    }

    fun getStringData(): String? {
        return data?.toString(Charset.forName("UTF-8"))
    }
}