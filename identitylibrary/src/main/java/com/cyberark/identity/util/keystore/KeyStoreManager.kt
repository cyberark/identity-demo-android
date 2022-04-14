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

interface KeyStoreManager {
    /**
     * Save the authentication token by encrypting using android Keystore
     * and saving the encrypted value of authKey and generated crypto IV into Shared preferences
     *
     * @param authToken need to be encrypted and saved
     *
     * @return boolean to indicate if the token successfully encrypted and saved
     */
    fun saveAuthToken(authToken: String): Boolean

    /**
     * Save the refresh token by encrypting using android Keystore
     * and saving the encrypted value of refreshToken and generated crypto IV into Shared preferences
     *
     * @param refreshToken token need to be encrypted and saved
     *
     * @return boolean to indicate if the token successfully encrypted and saved
     */
    fun saveRefreshToken(refreshToken: String): Boolean

    /**
     * Save the session token by encrypting using android Keystore
     * and saving the encrypted value of sessionToken and generated crypto IV into Shared preferences
     *
     * @param sessionToken token need to be encrypted and saved
     *
     * @return boolean to indicate if the token successfully encrypted and saved
     */
    fun saveSessionToken(sessionToken: String): Boolean

    /**
     * Save the header token by encrypting using android Keystore
     * and saving the encrypted value of headerToken and generated crypto IV into Shared preferences
     *
     * @param headerToken token need to be encrypted and saved
     *
     * @return boolean to indicate if the token successfully encrypted and saved
     */
    fun saveHeaderToken(headerToken: String): Boolean

    /**
     * Get the auth token saved using @see saveAuthToken(authKey:String)
     * @return string value if the encrypted token exists previously otherwise null
     */
    fun getAuthToken(): String?

    /**
     * Get the refresh token saved using @see saveRefreshToken(refreshToken:String)
     * @return string value if the encrypted token exists previously otherwise null
     */
    fun getRefreshToken(): String?

    /**
     * Get the session token saved using @see saveSessionToken(sessionToken:String)
     * @return string value if the encrypted token exists previously otherwise null
     */
    fun getSessionToken(): String?

    /**
     * Get the header token saved using @see saveHeaderToken(headerToken:String)
     * @return string value if the encrypted token exists previously otherwise null
     */
    fun getHeaderToken(): String?
}