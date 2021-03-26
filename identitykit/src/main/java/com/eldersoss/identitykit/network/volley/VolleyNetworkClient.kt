package com.eldersoss.identitykit.network.volley

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.android.volley.DefaultRetryPolicy
import com.android.volley.ExecutorDelivery
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.eldersoss.identitykit.network.NetworkClient
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.network.NetworkResponse
import java.util.concurrent.Executor

/**
 * Created by IvanVatov on 8/18/2017.
 */
class VolleyNetworkClient : NetworkClient {

    private val requestQueue: RequestQueue
    private val headers: HashMap<String, String>?
    private val initialTimeoutMs: Int

    constructor(context: Context, headers: HashMap<String, String>?, maxCacheSizeInBytes: Int, threadPoolSize: Int, timeoutMs: Int) : this(context, headers, maxCacheSizeInBytes, threadPoolSize, ExecutorDelivery(Handler(Looper.getMainLooper())), timeoutMs)

    constructor(context: Context, headers: HashMap<String, String>?, maxCacheSizeInBytes: Int, threadPoolSize: Int, executor: Executor, timeoutMs: Int) : this(context, headers, maxCacheSizeInBytes, threadPoolSize, ExecutorDelivery(executor), timeoutMs)

    constructor(context: Context, headers: HashMap<String, String>?, maxCacheSizeInBytes: Int, threadPoolSize: Int, executorDelivery: ExecutorDelivery, timeoutMs: Int) {
        this.headers = headers
        requestQueue = RequestQueue(DiskBasedCache(context.cacheDir, maxCacheSizeInBytes), BasicNetwork(HurlStack()), threadPoolSize, executorDelivery)
        requestQueue.start()
        initialTimeoutMs = timeoutMs
    }

    override fun execute(request: NetworkRequest, callback: (NetworkResponse) -> Unit) {

        val mergedHeaders = request.headers
        if (headers != null) {
            mergedHeaders.putAll(headers)
        }
        val volleyRequest = VolleyRequest(request, request.method.value, request.url, {}, mergedHeaders, request.body, callback)
        volleyRequest.retryPolicy = DefaultRetryPolicy(initialTimeoutMs, 1, 1f)
        requestQueue.add(volleyRequest)
    }
}