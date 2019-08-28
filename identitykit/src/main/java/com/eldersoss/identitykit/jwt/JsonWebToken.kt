package com.eldersoss.identitykit.jwt

import com.eldersoss.identitykit.extensions.base64UrlDecode
import org.json.JSONObject

/**
 * JsonWebToken implements openId token spec
 */
class JsonWebToken(token: String) {

    val header: String
    val payload: String
    val signature: String?

    private val headerJson: JSONObject
    private val payloadJson: JSONObject

    init {
        val splitToken = token.split(".")
        header = splitToken[0]
        payload = splitToken[1]
        signature = if (splitToken.size > 2) splitToken[2] else null

        headerJson = JSONObject(header.base64UrlDecode())
        payloadJson = JSONObject(payload.base64UrlDecode())
    }

    fun getClaim(str: String): String? {
        return payloadJson.opt(str).toString()
    }

    fun getParameter(str: String): String? {
        return headerJson.opt(str).toString()
    }
}