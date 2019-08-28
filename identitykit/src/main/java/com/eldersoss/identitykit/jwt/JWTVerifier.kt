package com.eldersoss.identitykit.jwt

import android.util.Base64
import com.eldersoss.identitykit.extensions.base64UrlDecodeBytes
import com.eldersoss.identitykit.jwt.interfaces.IVerifier
import com.eldersoss.identitykit.jwt.verifiers.*
import java.io.ByteArrayInputStream
import java.security.PublicKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.RSAPublicKeySpec
import java.math.BigInteger
import java.security.KeyFactory

/**
 * Default JsonWebToken verifier implementation
 * it is possible to verify RS256 and HS256
 * before verification specified publicKey or clientSecret
 * must be provided based on algorithm used for token signing
 */

class JWTVerifier : IVerifier {

    var publicKey: PublicKey? = null
    var clientSecret: String? = null

    override fun verify(jwt: JsonWebToken): Boolean {

        val verifier: IVerifier = when (jwt.getParameter("alg")) {
            "HS256" -> {
                HS256Verifier(clientSecret!!)
            }
            "RS256" -> {
                RS256Verifier(publicKey!!)
            }
            else -> {
                throw UnsupportedAlgorithmException()
            }
        }

        return verifier.verify(jwt)
    }

    fun jwksFromModulusAndExponent(modulus: String, exponent: String) {
        val keyFactory = KeyFactory.getInstance("RSA")
        val m = BigInteger(1, modulus.base64UrlDecodeBytes())
        val e = BigInteger(1, exponent.base64UrlDecodeBytes())
        publicKey = keyFactory.generatePublic(RSAPublicKeySpec(m, e))
    }

    fun jwksFromX509CertificateChain(x509CertificateChain: String) {
        val fact = CertificateFactory.getInstance("X.509")
        val cer = fact.generateCertificate(ByteArrayInputStream(Base64.decode(x509CertificateChain, Base64.NO_WRAP))) as X509Certificate
        publicKey = cer.publicKey
    }
}
