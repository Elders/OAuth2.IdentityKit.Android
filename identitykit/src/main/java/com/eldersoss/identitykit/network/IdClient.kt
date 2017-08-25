package com.eldersoss.identitykit.network

/**
 * Created by IvanVatov on 8/17/2017.
 */
interface IdClient {
    fun execute(idRequest: IdRequest)
}