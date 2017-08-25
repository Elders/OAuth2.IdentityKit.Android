package com.eldersoss.identitykit

/**
 * Created by IvanVatov on 8/24/2017.
 */

typealias Credentials = (username: String, password: String) -> Unit

interface CredentialsProvider {
    fun provideCredentials(handler: Credentials)
}