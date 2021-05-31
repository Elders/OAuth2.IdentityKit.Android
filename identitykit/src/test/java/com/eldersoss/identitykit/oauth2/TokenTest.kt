package com.eldersoss.identitykit.oauth2

import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TokenTest {

    @Test
    fun expiredTokenTest() {

        val token = Token(
            JSONObject(
                mapOf(
                    Pair("access_token", "a"),
                    Pair("token_type", "b"),
                    Pair("expires_in", 2)
                )
            )
        )
        Thread.sleep(3000)

        Assert.assertTrue(token.isExpired)
    }

    @Test
    fun notExpiredTokenTest() {

        val token = Token(
            JSONObject(
                mapOf(
                    Pair("access_token", "a"),
                    Pair("token_type", "b"),
                    Pair("expires_in", 3)
                )
            )
        )
        Thread.sleep(2000)

        Assert.assertFalse(token.isExpired)
    }
}