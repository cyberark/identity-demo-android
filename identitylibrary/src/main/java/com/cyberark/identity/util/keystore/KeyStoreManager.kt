package com.cyberark.identity.util.keystore

internal interface KeyStoreManager {
    /**
     * Save the authentication token by encrypting using android Keystore
     * and saving the encrypted value of authKey and generated crypto IV into Shared preferences
     *
     * @param authToken token need to be encrypted and saved
     *
     * @return boolean to indicate if the token successfully encrypted and saved
     * **/
    fun saveAuthToken(authKey:String):Boolean

    /**
     * Get the auth token saved using @see saveAuthToken(authKey:String)
     * @return string value if the encrypted token exists previously otherwise null
     * **/
    fun getAuthToken():String?
    /**
     * Get the refresh token saved using @see saveRefreshToken(refreshToken:String)
     * @return string value if the encrypted token exists previously otherwise null
     * **/
    fun getRefreshToken():String?
    /**
     * Save the refresh token by encrypting using android Keystore
     * and saving the encrypted value of refreshToken and generated crypto IV into Shared preferences
     *
     * @param authToken token need to be encrypted and saved
     *
     * @return boolean to indicate if the token successfully encrypted and saved
     * **/
    fun saveRefreshToken(refreshToken:String):Boolean
}