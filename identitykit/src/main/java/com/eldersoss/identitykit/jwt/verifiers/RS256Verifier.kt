package com.eldersoss.identitykit.jwt.verifiers

import com.eldersoss.identitykit.extensions.base64UrlDecodeBytes
import com.eldersoss.identitykit.jwt.JsonWebToken
import com.eldersoss.identitykit.jwt.interfaces.IVerifier
import java.security.PublicKey
import java.security.Signature

class RS256Verifier(private val publicKey: PublicKey) : IVerifier {

    override fun verify(jwt: JsonWebToken): Boolean {

        val sha = Signature.getInstance("SHA256withRSA")
        sha.initVerify(publicKey)

        sha.update(jwt.header.toByteArray(charset("UTF-8")))
        sha.update(".".toByteArray(charset("UTF-8")))
        sha.update(jwt.payload.toByteArray(charset("UTF-8")))

        return sha.verify(jwt.signature?.base64UrlDecodeBytes())
    }

}