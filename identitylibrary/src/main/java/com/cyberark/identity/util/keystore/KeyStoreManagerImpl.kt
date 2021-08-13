/*
 * Copyright (c) 2021 CyberArk Software Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyberark.identity.util.keystore

import android.util.Base64
import com.cyberark.identity.util.keystore.Encryption.DeCryptor
import com.cyberark.identity.util.keystore.Encryption.EnCryptor
import com.cyberark.identity.util.preferences.Constants
import com.cyberark.identity.util.preferences.CyberarkPreferenceUtils
import java.io.IOException
import java.security.*
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

/**
 * Key store manager impl
 *
 * @constructor Create empty Key store manager impl
 */
private class KeyStoreManagerImpl : KeyStoreManager {

    /**
     * Save auth token
     *
     * @param authToken
     * @return
     */
    override fun saveAuthToken(authToken: String): Boolean {
        val returnValues = encryptText(Constants.AUTH_ALIAS, authToken)
        if (returnValues != null) {
            CyberarkPreferenceUtils.putString(Constants.AUTH_TOKEN_IV, Base64.encodeToString(returnValues.first, Base64.DEFAULT))
            CyberarkPreferenceUtils.putString(Constants.AUTH_TOKEN, Base64.encodeToString(returnValues.second, Base64.DEFAULT))
            DeCryptor().decryptData(Constants.AUTH_ALIAS, Base64.decode(CyberarkPreferenceUtils.getString(Constants.AUTH_TOKEN, ""), Base64.DEFAULT), Base64.decode(CyberarkPreferenceUtils.getString(Constants.AUTH_TOKEN_IV, ""), Base64.DEFAULT))
            return true
        }
        return false
    }

    /**
     * Get auth token
     *
     * @return
     */
    override fun getAuthToken(): String? {
        return getAuthTokenKeyStore()
    }

    /**
     * Get refresh token
     *
     * @return
     */
    override fun getRefreshToken(): String? {
        return getRefreshTokenKeyStore()
    }

    /**
     * Save refresh token
     *
     * @param refreshToken
     * @return
     */
    override fun saveRefreshToken(refreshToken: String): Boolean {
        val returnValues = encryptText(Constants.REFRESH_ALIAS, refreshToken)
        if (returnValues != null) {
            CyberarkPreferenceUtils.putString(Constants.REFRESH_TOKEN_IV, Base64.encodeToString(returnValues.first, Base64.DEFAULT))
            CyberarkPreferenceUtils.putString(Constants.REFRESH_TOKEN, Base64.encodeToString(returnValues.second, Base64.DEFAULT))
            return true
        }
        return false
    }

    /**
     * Get auth token key store
     *
     * @return
     */
    private fun getAuthTokenKeyStore(): String? {
        var decryptedToken: String? = null
        val accessTokenIV = CyberarkPreferenceUtils.getString(Constants.AUTH_TOKEN_IV, null)
        val accessToken = CyberarkPreferenceUtils.getString(Constants.AUTH_TOKEN, null)
        if (accessTokenIV != null || accessToken != null) {
            decryptedToken = decryptText(Constants.AUTH_ALIAS, Base64.decode(accessToken!!, Base64.DEFAULT), Base64.decode(accessTokenIV!!, Base64.DEFAULT))
        }
        return decryptedToken
    }

    /**
     * Get refresh token key store
     *
     * @return
     */
    private fun getRefreshTokenKeyStore(): String? {
        var decryptedToken: String? = null
        val refreshTokenIV = CyberarkPreferenceUtils.getString(Constants.REFRESH_TOKEN_IV, null)
        val refreshToken = CyberarkPreferenceUtils.getString(Constants.REFRESH_TOKEN, null)
        if (refreshTokenIV != null || refreshToken != null) {
            decryptedToken = decryptText(Constants.REFRESH_ALIAS, Base64.decode(refreshToken!!, Base64.DEFAULT), Base64.decode(refreshTokenIV!!, Base64.DEFAULT))
        }
        return decryptedToken
    }

    /**
     * Encrypt text
     *
     * @param alias
     * @param toEncrpt
     * @return
     */
    private fun encryptText(alias: String, toEncrpt: String): Pair<ByteArray, ByteArray>? {
        try {
            val returnValues = EnCryptor().encryptText(alias, toEncrpt)
            return returnValues
        } catch (e: UnrecoverableEntryException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: NoSuchProviderException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Decrypt text
     *
     * @param alias
     * @param toDerypt
     * @param ivKey
     * @return
     */
    private fun decryptText(alias: String, toDerypt: ByteArray, ivKey: ByteArray): String? {
        try {

            return DeCryptor().decryptData(alias, toDerypt, ivKey)

        } catch (e: UnrecoverableEntryException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: NoSuchProviderException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        }
        return null
    }

}

/**
 * Key store provider
 *
 * @constructor Create empty Key store provider
 */
object KeyStoreProvider {
    private var keyStoreManager: KeyStoreManager? = null

    fun get(): KeyStoreManager {
        synchronized(KeyStoreProvider::class.java) {
            if (keyStoreManager == null) {
                keyStoreManager = KeyStoreManagerImpl()
            }
            return keyStoreManager!!
        }
    }

}