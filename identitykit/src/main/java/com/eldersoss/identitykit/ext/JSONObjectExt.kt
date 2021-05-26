package com.eldersoss.identitykit.ext

import org.json.JSONObject

fun JSONObject.getOptString(key: String): String? {
    if (this.isNull(key))
        return null
    return this.getString(key)
}