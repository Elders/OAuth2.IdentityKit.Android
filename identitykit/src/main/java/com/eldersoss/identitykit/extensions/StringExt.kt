package com.eldersoss.identitykit.extensions

import android.util.Base64

fun String.base64UrlEncode(): String {
    return Base64.encodeToString(this.toByteArray(charset("UTF-8")), Base64.URL_SAFE or Base64.NO_WRAP)
}

fun String.base64UrlDecode(): String {
    return Base64.decode(this, Base64.URL_SAFE).toString(charset("UTF-8"))
}

fun String.base64UrlDecodeBytes(): ByteArray {
    return Base64.decode(this, Base64.URL_SAFE)
}