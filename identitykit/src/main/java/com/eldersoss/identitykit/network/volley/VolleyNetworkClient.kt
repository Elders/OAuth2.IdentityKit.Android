package com.eldersoss.identitykit.network.volley

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.eldersoss.identitykit.network.NetworkClient
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.network.NetworkResponse

/**
 * Created by IvanVatov on 8/18/2017.
 */
class VolleyNetworkClient(val context: Context, val headers: HashMap<String, String>?) : NetworkClient {

    init {
        getRequestQueue()
    }

    override fun execute(request: NetworkRequest, callback: (NetworkResponse) -> Unit) {

        val method = when(request.method) {
            "GET" -> 0
            "POST" -> 1
            "PUT" -> 2
            "DELETE" -> 3
            "HEAD" -> 4
            "OPTIONS" -> 5
            "TRACE" -> 6
            "PATCH" -> 7
            else -> -1
        }
        var mergedHeaders = request.headers
        if (headers != null) {
            mergedHeaders.putAll(headers)
        }
        val volleyRequest = VolleyRequest(request, method, request.url, Response.ErrorListener({}), mergedHeaders, request.body, callback)
        volleyRequest.retryPolicy = DefaultRetryPolicy(30000, 1, 1f)
        requestQueue?.add(volleyRequest)
    }

    private var requestQueue: RequestQueue? = null

    fun getRequestQueue(): RequestQueue? {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context)
        }
        return requestQueue
    }
}