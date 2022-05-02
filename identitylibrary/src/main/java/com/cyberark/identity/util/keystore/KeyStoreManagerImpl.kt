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
import com.cyberark.identity.util.keystore.encryption.DeCryptor
import com.cyberark.identity.util.keystore.encryption.EnCryptor
import com.cyberark.identity.util.preferences.Constants
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import java.io.IOException
import java.security.*
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

/**
 * Keystore manager implementation
 * 1. save access token and refresh token in shared preference using Android keystore encryption
 * 2. Get access token and refresh token from shared preference using Android keystore decryption
 *
 */
private class KeyStoreManagerImpl : KeyStoreManager {

    /**
     * Save auth token in shared preference
     *
     * @param authToken: access token data
     * @return Boolean: true if the access token is encrypted successfully, else false
     */
    override fun saveAuthToken(authToken: String): Boolean {
        val returnValues = encryptText(Constants.AUTH_ALIAS, authToken)
        if (returnValues != null) {
            CyberArkPreferenceUtil.putString(
                Constants.ACCESS_TOKEN_IV,
                Base64.encodeToString(returnValues.first, Base64.DEFAULT)
            )
            CyberArkPreferenceUtil.putString(
                Constants.ACCESS_TOKEN,
                Base64.encodeToString(returnValues.second, Base64.DEFAULT)
            )
            DeCryptor().decryptData(
                Constants.AUTH_ALIAS,
                Base64.decode(
                    CyberArkPreferenceUtil.getString(Constants.ACCESS_TOKEN, ""),
                    Base64.DEFAULT
                ),
                Base64.decode(
                    CyberArkPreferenceUtil.getString(Constants.ACCESS_TOKEN_IV, ""),
                    Base64.DEFAULT
                )
            )
            return true
        }
        return false
    }

    /**
     * Save refresh token in shared preference
     *
     * @param refreshToken: refresh token data
     * @return Boolean: true if the refresh token is encrypted successfully, else false
     */
    override fun saveRefreshToken(refreshToken: String): Boolean {
        val returnValues = encryptText(Constants.REFRESH_ALIAS, refreshToken)
        if (returnValues != null) {
            CyberArkPreferenceUtil.putString(
                Constants.REFRESH_TOKEN_IV,
                Base64.encodeToString(returnValues.first, Base64.DEFAULT)
            )
            CyberArkPreferenceUtil.putString(
                Constants.REFRESH_TOKEN,
                Base64.encodeToString(returnValues.second, Base64.DEFAULT)
            )
            return true
        }
        return false
    }

    /**
     * Save Id token in shared preference
     *
     * @param idToken: id token data
     * @return Boolean: true if the id token is encrypted successfully, else false
     */
    override fun saveIdToken(idToken: String): Boolean {
        val returnValues = encryptText(Constants.ID_ALIAS, idToken)
        if (returnValues != null) {
            CyberArkPreferenceUtil.putString(
                Constants.ID_TOKEN_IV,
                Base64.encodeToString(returnValues.first, Base64.DEFAULT)
            )
            CyberArkPreferenceUtil.putString(
                Constants.ID_TOKEN,
                Base64.encodeToString(returnValues.second, Base64.DEFAULT)
            )
            return true
        }
        return false
    }

    /**
     * Save session token in shared preference
     *
     * @param sessionToken: session token data
     * @return Boolean: true if the session token is encrypted successfully, else false
     */
    override fun saveSessionToken(sessionToken: String): Boolean {
        val returnValues = encryptText(Constants.SESSION_ALIAS, sessionToken)
        if (returnValues != null) {
            CyberArkPreferenceUtil.putString(
                Constants.SESSION_TOKEN_IV,
                Base64.encodeToString(returnValues.first, Base64.DEFAULT)
            )
            CyberArkPreferenceUtil.putString(
                Constants.SESSION_TOKEN,
                Base64.encodeToString(returnValues.second, Base64.DEFAULT)
            )
            return true
        }
        return false
    }

    /**
     * Save header token in shared preference
     *
     * @param headerToken: header token data
     * @return Boolean: true if the session token is encrypted successfully, else false
     */
    override fun saveHeaderToken(headerToken: String): Boolean {
        val returnValues = encryptText(Constants.HEADER_ALIAS, headerToken)
        if (returnValues != null) {
            CyberArkPreferenceUtil.putString(
                Constants.HEADER_TOKEN_IV,
                Base64.encodeToString(returnValues.first, Base64.DEFAULT)
            )
            CyberArkPreferenceUtil.putString(
                Constants.HEADER_TOKEN,
                Base64.encodeToString(returnValues.second, Base64.DEFAULT)
            )
            return true
        }
        return false
    }

    /**
     * Get auth token from Android keystore
     *
     * @return String: decrypted auth token
     */
    private fun getAuthTokenKeyStore(): String? {
        var decryptedToken: String? = null
        val accessTokenIV = CyberArkPreferenceUtil.getString(Constants.ACCESS_TOKEN_IV, null)
        val accessToken = CyberArkPreferenceUtil.getString(Constants.ACCESS_TOKEN, null)
        if (accessTokenIV != null || accessToken != null) {
            decryptedToken = decryptText(
                Constants.AUTH_ALIAS,
                Base64.decode(accessToken!!, Base64.DEFAULT),
                Base64.decode(accessTokenIV!!, Base64.DEFAULT)
            )
        }
        return decryptedToken
    }

    /**
     * Get refresh token from Android keystore
     *
     * @return String: decrypted refresh token
     */
    private fun getRefreshTokenKeyStore(): String? {
        var decryptedToken: String? = null
        val refreshTokenIV = CyberArkPreferenceUtil.getString(Constants.REFRESH_TOKEN_IV, null)
        val refreshToken = CyberArkPreferenceUtil.getString(Constants.REFRESH_TOKEN, null)
        if (refreshTokenIV != null || refreshToken != null) {
            decryptedToken = decryptText(
                Constants.REFRESH_ALIAS,
                Base64.decode(refreshToken!!, Base64.DEFAULT),
                Base64.decode(refreshTokenIV!!, Base64.DEFAULT)
            )
        }
        return decryptedToken
    }

    /**
     * Get Id token from Android keystore
     *
     * @return String: decrypted id token
     */
    private fun getIdTokenKeyStore(): String? {
        var decryptedToken: String? = null
        val idTokenIV = CyberArkPreferenceUtil.getString(Constants.ID_TOKEN_IV, null)
        val idToken = CyberArkPreferenceUtil.getString(Constants.ID_TOKEN, null)
        if (idTokenIV != null || idToken != null) {
            decryptedToken = decryptText(
                Constants.REFRESH_ALIAS,
                Base64.decode(idToken!!, Base64.DEFAULT),
                Base64.decode(idTokenIV!!, Base64.DEFAULT)
            )
        }
        return decryptedToken
    }

    /**
     * Get session token from Android keystore
     *
     * @return String: decrypted session token
     */
    private fun getSessionTokenKeyStore(): String? {
        var decryptedToken: String? = null
        val sessionTokenIV = CyberArkPreferenceUtil.getString(Constants.SESSION_TOKEN_IV, null)
        val sessionToken = CyberArkPreferenceUtil.getString(Constants.SESSION_TOKEN, null)
        if (sessionTokenIV != null || sessionToken != null) {
            decryptedToken = decryptText(
                Constants.SESSION_ALIAS,
                Base64.decode(sessionToken!!, Base64.DEFAULT),
                Base64.decode(sessionTokenIV!!, Base64.DEFAULT)
            )
        }
        return decryptedToken
    }

    /**
     * Get header token from Android keystore
     *
     * @return String: decrypted header token
     */
    private fun getHeaderTokenKeyStore(): String? {
        var decryptedToken: String? = null
        val headerTokenIV = CyberArkPreferenceUtil.getString(Constants.HEADER_TOKEN_IV, null)
        val headerToken = CyberArkPreferenceUtil.getString(Constants.HEADER_TOKEN, null)
        if (headerTokenIV != null || headerToken != null) {
            decryptedToken = decryptText(
                Constants.HEADER_ALIAS,
                Base64.decode(headerToken!!, Base64.DEFAULT),
                Base64.decode(headerTokenIV!!, Base64.DEFAULT)
            )
        }
        return decryptedToken
    }

    /**
     * Get auth token from Android keystore
     *
     * @return String: decrypted auth token
     */
    override fun getAuthToken(): String? {
        return getAuthTokenKeyStore()
    }

    /**
     * Get refresh token from Android keystore
     *
     * @return String: decrypted refresh token
     */
    override fun getRefreshToken(): String? {
        return getRefreshTokenKeyStore()
    }

    /**
     * Get Id token from Android keystore
     *
     * @return String: decrypted id token
     */
    override fun getIdToken(): String? {
        return getIdTokenKeyStore()
    }

    /**
     * Get session token from Android keystore
     *
     * @return String: decrypted session token
     */
    override fun getSessionToken(): String? {
        return getSessionTokenKeyStore()
    }

    /**
     * Get header token from Android keystore
     *
     * @return String: decrypted header token
     */
    override fun getHeaderToken(): String? {
        return getHeaderTokenKeyStore()
    }

    /**
     * Encrypt data
     *
     * @param alias: alias string
     * @param toEncrpt: data need to be encrypted
     * @return Pair<ByteArray, ByteArray>
     */
    private fun encryptText(alias: String, toEncrpt: String): Pair<ByteArray, ByteArray>? {
        try {
            return EnCryptor().encryptText(alias, toEncrpt)
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
     * Decrypt data
     *
     * @param alias: alias string
     * @param toDerypt: data need to be decrypted
     * @param ivKey: iv key
     * @return String
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
 * Keystore provider singleton class
 *
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