/*
 * Copyright (c) 2017. Elders LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eldersoss.identitykit.storage

import android.content.Context
import android.util.Base64
import com.eldersoss.identitykit.network.DEFAULT_CHARSET
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec

/**
 * Created by IvanVatov on 9/11/2017.
 */
class DefaultTokenStorage(val context: Context) : TokenStorage {

    private val salt = "49um32t4v0vt42509"
    private val preferenceFileName = "IdentityKit"
    private val sharedPref = context.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE)

    override fun read(key: String): String? {
        val encodedString = sharedPref.getString(key, null) ?: return null
        return encDec(encodedString, false)
    }

    override fun delete(key: String) {
        sharedPref.edit().remove(key).commit()
    }

    override fun write(key: String, value: String) {
        sharedPref.edit().putString(key, encDec(value, true)).commit()
    }

    @Synchronized
    private fun encDec(value: String, isEncode: Boolean): String{
        val keySpec = DESKeySpec(salt.toByteArray(charset(DEFAULT_CHARSET)))
        val keyFactory = SecretKeyFactory.getInstance("DES")
        val key = keyFactory.generateSecret(keySpec)
        val cipher = Cipher.getInstance("DES")

        return if (isEncode) {
            val byteArray: ByteArray = value.toByteArray(charset(DEFAULT_CHARSET))
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encryptedBytes = cipher.doFinal(byteArray)
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        } else {
            val byteArray = Base64.decode(value, Base64.DEFAULT)
            cipher.init(Cipher.DECRYPT_MODE, key)
            val plainTextBytes =(cipher.doFinal(byteArray))
            plainTextBytes.toString(charset(DEFAULT_CHARSET))
        }
    }
}