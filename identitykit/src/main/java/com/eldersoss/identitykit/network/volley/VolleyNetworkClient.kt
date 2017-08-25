package com.eldersoss.identitykit.network.volley

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.eldersoss.identitykit.network.IdClient
import com.eldersoss.identitykit.network.IdRequest

/**
 * Created by IvanVatov on 8/18/2017.
 */
class VolleyNetworkClient(val context: Context) : IdClient {

    init {
        getRequestQueue()
    }
// { request.onResponse(null, "n") }
    override fun execute(request: IdRequest) {
        val volleyRequest = VolleyRequest(request, 1, request.url, Response.ErrorListener({}), request.headers, request.getBodyBytes())
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