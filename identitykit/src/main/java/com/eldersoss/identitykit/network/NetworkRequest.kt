package com.eldersoss.identitykit.network

import android.util.Log
import java.io.UnsupportedEncodingException
import kotlin.collections.HashMap

/**
 * Created by IvanVatov on 8/17/2017.
 */

/**
 * @param method request method.
 * @param url url address
 * @param headers headers
 * @property body body - string UTF-8 encoded.
 * @constructor Creates network request.
 */
open class NetworkRequest(val method: String, val url: String, val headers : HashMap<String, String>, var body : String) {

    //var onResponse: (NetworkResponse?, String?) -> Unit = { _, _ -> Unit }
    fun getBodyBytes(): ByteArray? {
        return try {
            body?.toByteArray(charset("utf-8"))
        } catch (uee: UnsupportedEncodingException) {
            Log.e("Error Encoding utf-8", uee.message)
            null
        }
    }
}