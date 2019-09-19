package com.eldersoss.identitykit

import android.net.Uri
import com.eldersoss.identitykit.network.ParamsBuilder
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ParamsBuilderTest {

    @Test
    fun testUriParams() {
        val uriParams = ParamsBuilder()
                .add("asd&dsa", "dsa&asd")
                .add("qwe&dsa", "d sa&qwe")
                .add("same", "1=23")
                .add("same", "45=6")
                .build()

        val expected = "${Uri.encode("asd&dsa")}=${Uri.encode("dsa&asd")}&" +
                "${Uri.encode("qwe&dsa")}=${Uri.encode("d sa&qwe")}&" +
                "${Uri.encode("same")}=${Uri.encode("1=23")}&" +
                "${Uri.encode("same")}=${Uri.encode("45=6")}"

        assert(uriParams == expected)
    }
}