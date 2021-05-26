package com.eldersoss.identitykit.network.volley

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.ExecutorDelivery
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.eldersoss.identitykit.network.NetworkClient
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.network.NetworkResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executor
import kotlin.coroutines.resumeWithException

/**
 * Created by IvanVatov on 8/18/2017.
 */
class VolleyNetworkClient : NetworkClient {

    private val requestQueue: RequestQueue
    private val headers: HashMap<String, String>?
    private val initialTimeoutMs: Int

    constructor(context: Context, headers: HashMap<String, String>?, maxCacheSizeInBytes: Int, threadPoolSize: Int, executor: Executor, timeoutMs: Int) :
            this(context, headers, maxCacheSizeInBytes, threadPoolSize, ExecutorDelivery(executor), timeoutMs)

    constructor(context: Context, headers: HashMap<String, String>?, maxCacheSizeInBytes: Int, threadPoolSize: Int, executorDelivery: ExecutorDelivery, timeoutMs: Int) {
        this.headers = headers
        requestQueue = RequestQueue(DiskBasedCache(context.cacheDir, maxCacheSizeInBytes), BasicNetwork(HurlStack()), threadPoolSize, executorDelivery)
        requestQueue.start()
        initialTimeoutMs = timeoutMs
    }

    override suspend fun execute(request: NetworkRequest): NetworkResponse {

        val mergedHeaders = request.headers
        if (headers != null) {

            mergedHeaders.putAll(headers)
        }

        return suspendCancellableCoroutine { continuation ->

            val volleyRequest = VolleyRequest(
                request,
                request.method.value,
                request.url,
                {},
                mergedHeaders,
                request.body
            ) { networkResponse ->

                continuation.resumeWith(Result.success(networkResponse))
            }

            volleyRequest.retryPolicy = DefaultRetryPolicy(initialTimeoutMs, 1, 1f)
            continuation.invokeOnCancellation { volleyRequest.cancel() }

            try {

                requestQueue.add(volleyRequest)
            } catch (e: Throwable) {

                continuation.resumeWithException(e)
            }
        }
    }
}