package com.eldersoss.identitykit

import com.eldersoss.identitykit.extensions.base64UrlDecode
import com.eldersoss.identitykit.extensions.base64UrlEncode
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class Base64EncDecTest {

    @Test
    fun urlEncodeDecodeTest() {
        var str = ">>>???aaa"
        var strBase64UrlEncode = str.base64UrlEncode()
        var strBase64UrlDecode = strBase64UrlEncode.base64UrlDecode()

        assert(strBase64UrlEncode == "Pj4-Pz8_YWFh")
        assert(strBase64UrlDecode == str)

        str = ">>>???aa"
        strBase64UrlEncode = str.base64UrlEncode()
        strBase64UrlDecode = strBase64UrlEncode.base64UrlDecode()

        assert(strBase64UrlEncode == "Pj4-Pz8_YWE=")
        assert(strBase64UrlDecode == str)

        str = ">>>???a"
        strBase64UrlEncode = str.base64UrlEncode()
        strBase64UrlDecode = strBase64UrlEncode.base64UrlDecode()

        assert(strBase64UrlEncode == "Pj4-Pz8_YQ==")
        assert(strBase64UrlDecode == str)
    }
}
