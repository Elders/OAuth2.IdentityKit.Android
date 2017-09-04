package com.eldersoss.identitykit.storage

/**
 * Created by IvanVatov on 8/21/2017.
 */
interface TokenStorage {
    fun read(key: String) : String?
    fun delete(key: String)
    fun write(key: String, value: String)
}