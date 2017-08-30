package com.eldersoss.identitykit.network

/**
 * Created by IvanVatov on 8/17/2017.
 */
interface NetworkClient {
    fun execute(request: NetworkRequest, callback: (NetworkResponse) -> Unit)
}