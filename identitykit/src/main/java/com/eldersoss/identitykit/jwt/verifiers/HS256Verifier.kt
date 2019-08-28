package com.eldersoss.identitykit.jwt.verifiers

import com.eldersoss.identitykit.extensions.base64UrlDecodeBytes
import com.eldersoss.identitykit.jwt.JsonWebToken
import com.eldersoss.identitykit.jwt.interfaces.IVerifier
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class HS256Verifier(private val secret: String) : IVerifier {

    override fun verify(jwt: JsonWebToken): Boolean {
        val hMac = Mac.getInstance("HmacSHA256")
        hMac.init(SecretKeySpec(secret.toByteArray(charset("UTF-8")), "HmacSHA256"))
        hMac.update(jwt.header.toByteArray(charset("UTF-8")))
        hMac.update(".".toByteArray(charset("UTF-8")))
        hMac.update(jwt.payload.toByteArray(charset("UTF-8")))
        val hMacByteArray: ByteArray = hMac.doFinal()
        return hMacByteArray.contentEquals(jwt.signature!!.base64UrlDecodeBytes())
    }

}