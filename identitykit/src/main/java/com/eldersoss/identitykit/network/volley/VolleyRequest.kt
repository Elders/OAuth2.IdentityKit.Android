package com.eldersoss.identitykit.network.volley

import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.network.NetworkResponse as KitResponse


/**
 * Created by IvanVatov on 8/18/2017.
 */
class VolleyRequest(var request: NetworkRequest, method: Int, url: String, listener: Response.ErrorListener, var headers: HashMap<String, String>, val bytes : ByteArray?, val callback: (KitResponse) -> Unit) : Request<KitResponse>(method, url, listener) {

    override fun parseNetworkResponse(response: NetworkResponse): Response<KitResponse> {
        var result = KitResponse()
        result.data = response.data
        result.statusCode = response.statusCode
        result.headers = response.headers
        return Response.success(result, HttpHeaderParser.parseCacheHeaders(response))
    }

    override fun deliverResponse(response: KitResponse) {
        callback(response)
    }

    override fun deliverError(volleyError: VolleyError){
        var response = KitResponse()
        response.data = volleyError.networkResponse?.data
        response.statusCode = volleyError.networkResponse?.statusCode
        response.headers = volleyError.networkResponse?.headers
        response.error = VolleyNetworkError.server_error
        callback(response)
    }

    override fun getHeaders(): Map<String, String> {
        return headers
    }

    override fun getBody(): ByteArray? {
        return bytes
    }

    override fun getBodyContentType(): String {
        return "application/x-www-form-urlencoded; charset=$paramsEncoding"
    }

    override fun getPriority(): Priority {
        return super.getPriority()
    }

    override fun getRetryPolicy(): RetryPolicy {
        return super.getRetryPolicy()
    }

    override fun getParams(): MutableMap<String, String> {
        return super.getParams()
    }
}