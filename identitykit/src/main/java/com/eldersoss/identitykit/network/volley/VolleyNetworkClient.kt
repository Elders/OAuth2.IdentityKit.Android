package com.eldersoss.identitykit.network.volley

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.Volley
import com.eldersoss.identitykit.network.NetworkClient
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.network.NetworkResponse

/**
 * Created by IvanVatov on 8/18/2017.
 */
class VolleyNetworkClient(private val context: Context, private val headers: HashMap<String, String>?, private val maxCacheSizeInBytes: Int, private val threadPoolSize: Int) : NetworkClient {

    init {
        getRequestQueue()
    }

    override fun execute(request: NetworkRequest, callback: (NetworkResponse) -> Unit) {

        val method = when (request.method) {
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

    private fun getRequestQueue(): RequestQueue? {
        if (requestQueue == null) {

            requestQueue = RequestQueue(DiskBasedCache(context.cacheDir, maxCacheSizeInBytes), BasicNetwork(HurlStack()), threadPoolSize)
            requestQueue?.start()
//            requestQueue = Volley.newRequestQueue(context)
        }
        return requestQueue
    }
}