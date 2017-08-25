package com.eldersoss.identitykit

/**
 * Created by IvanVatov on 8/23/2017.
 */
interface CredentialsListener {
    fun onCredentialsRequest()
    fun onCredentialsInvalid()
}