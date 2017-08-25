package com.eldersoss.identitykit.network

import android.util.Log
import java.io.UnsupportedEncodingException
import kotlin.collections.HashMap

/**
 * Created by IvanVatov on 8/17/2017.
 */
open class IdRequest(val method: Method, val url: String, val headers : HashMap<String, String>, var body : String) {

    var onResponse: (IdResponse?, String?) -> Unit = { _, _ -> Unit }

    fun getBodyBytes(): ByteArray? {
        return try {
            body?.toByteArray(charset("utf-8"))
        } catch (uee: UnsupportedEncodingException) {
            Log.e("Error Encoding utf-8", uee.message)
            null
        }
    }

    enum class Method(val met: Int){
        GET(0),
        POST(1),
        PUT(2),
        HEAD(4);
    }
}