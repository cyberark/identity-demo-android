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


private class KeyStoreManagerImpl: KeyStoreManager {
    override fun saveAuthToken(authToken: String): Boolean {
        val returnValues = encryptText(Constants.AUTHALIAS,authToken)
        if (returnValues != null) {
            CyberarkPreferenceUtils.putString(Constants.AUTHTOKENIV, Base64.encodeToString(returnValues.first,Base64.DEFAULT))
            CyberarkPreferenceUtils.putString(Constants.AUTHTOKEN, Base64.encodeToString(returnValues.second,Base64.DEFAULT))
            DeCryptor().decryptData(Constants.AUTHALIAS,Base64.decode(CyberarkPreferenceUtils.getString(Constants.AUTHTOKEN,""),Base64.DEFAULT),Base64.decode(CyberarkPreferenceUtils.getString(Constants.AUTHTOKENIV,""),Base64.DEFAULT))
            return true
        }
        return false
    }

    override fun getAuthToken(): String? {
        return getAuthTokenKeyStore()
    }

    override fun getRefreshToken(): String? {
        return getRefreshTokenKeyStore()
    }

    override fun saveRefreshToken(refreshToken: String): Boolean {
        val returnValues = encryptText(Constants.REFRESHALIASKEY,refreshToken)
        if (returnValues != null) {
            CyberarkPreferenceUtils.putString(Constants.REFRESHTOKENIV, Base64.encodeToString(returnValues.first,Base64.DEFAULT))
            CyberarkPreferenceUtils.putString(Constants.REFRESHTOKEN, Base64.encodeToString(returnValues.second,Base64.DEFAULT))
            return true
        }
        return false
    }

    private fun getAuthTokenKeyStore():String? {
        var  decryptedToken:String? = null
        val accessTokenIV = CyberarkPreferenceUtils.getString(Constants.AUTHTOKENIV,null)
        val accessToken = CyberarkPreferenceUtils.getString(Constants.AUTHTOKEN,null)
        if (accessTokenIV != null || accessToken != null) {
            decryptedToken = decryptText(Constants.AUTHALIAS,Base64.decode(accessToken!!,Base64.DEFAULT),Base64.decode(accessTokenIV!!,Base64.DEFAULT))
        }
        return decryptedToken
    }

    private fun getRefreshTokenKeyStore():String? {
        var  decryptedToken:String? = null
        val refreshTokenIV = CyberarkPreferenceUtils.getString(Constants.REFRESHTOKENIV,null)
        val refreshToken = CyberarkPreferenceUtils.getString(Constants.REFRESHTOKEN,null)
        if (refreshTokenIV != null || refreshToken != null) {
//            DeCryptor().decryptData(Constants.REFRESHALIASKEY,Base64.decode(CyberarkPreferenceUtils.getString(Constants.REFRESHTOKEN,""),Base64.DEFAULT),Base64.decode(CyberarkPreferenceUtils.getString(Constants.REFRESHTOKENIV,""),Base64.DEFAULT))
            decryptedToken = decryptText(Constants.REFRESHALIASKEY,Base64.decode(refreshToken!!,Base64.DEFAULT),Base64.decode(refreshTokenIV!!,Base64.DEFAULT))
        }
        return decryptedToken
    }

    private fun encryptText(alias:String,toEncrpt:String):Pair<ByteArray,ByteArray>? {
        try {
            val returnValues = EnCryptor().encryptText(alias,toEncrpt)
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

    private fun decryptText(alias: String,toDerypt:ByteArray, ivKey:ByteArray):String? {
        try {

            return DeCryptor().decryptData(alias,toDerypt,ivKey)

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


object GetKeyStore {
    private var keyStoreManager:KeyStoreManager? = null

    internal fun get(): KeyStoreManager {
        synchronized(GetKeyStore::class.java) {
            if (keyStoreManager == null) {
                keyStoreManager = KeyStoreManagerImpl()
            }
            return keyStoreManager!!
        }
    }

}