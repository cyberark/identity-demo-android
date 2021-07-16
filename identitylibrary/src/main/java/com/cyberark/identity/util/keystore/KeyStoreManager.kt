package com.cyberark.identity.util.keystore

internal interface KeyStoreManager {
    fun saveAuthToken(authKey:String):Boolean
    fun getAuthToken():String?
    fun getRefreshToken():String?
    fun saveRefreshToken(refreshToken:String):Boolean
}