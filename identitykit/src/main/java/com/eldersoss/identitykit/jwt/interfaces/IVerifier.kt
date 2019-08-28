package com.eldersoss.identitykit.jwt.interfaces

import com.eldersoss.identitykit.jwt.JsonWebToken

interface IVerifier {

    fun verify(jwt: JsonWebToken): Boolean
}