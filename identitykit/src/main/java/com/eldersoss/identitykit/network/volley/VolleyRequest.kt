package com.eldersoss.identitykit.network.volley

import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.eldersoss.identitykit.network.IdRequest
import com.eldersoss.identitykit.network.IdResponse


/**
 * Created by IvanVatov on 8/18/2017.
 */
class VolleyRequest(var request: IdRequest, method: Int, url: String, listener: Response.ErrorListener, var headers: HashMap<String, String>, val bytes : ByteArray?) : Request<IdResponse>(method, url, listener) {

    override fun parseNetworkResponse(response: NetworkResponse): Response<IdResponse> {
        var result = IdResponse()
        result.data = response.data
        result.statusCode = response.statusCode
        result.headers = response.headers
        return Response.success(result, HttpHeaderParser.parseCacheHeaders(response))
    }

    override fun deliverResponse(response: IdResponse) {
        request.onResponse(response, "GG")
        //listen.onSuccess(response)
    }

    override fun deliverError(volleyError: VolleyError){
        request.onResponse(null, volleyError.message)
    }

    override fun getHeaders(): Map<String, String> {
        return headers
    }

    override fun getBody(): ByteArray? {
        return bytes
    }

    override fun getBodyContentType(): String {
        return "application/x-www-form-urlencoded"
    }

}