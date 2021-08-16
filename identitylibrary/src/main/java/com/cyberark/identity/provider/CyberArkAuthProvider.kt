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

package com.cyberark.identity.provider

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.data.model.AuthCodeFlowModel
import com.cyberark.identity.data.model.EnrollmentModel
import com.cyberark.identity.data.model.RefreshTokenModel
import com.cyberark.identity.util.ResponseHandler

object CyberArkAuthProvider {

    private val TAG: String? = CyberArkAuthProvider::class.simpleName
    internal var cyberArkAuthInterface: CyberArkAuthInterface? = null

    /**
     * Login
     *
     * @param account
     * @return
     */
    fun login(account: CyberArkAccountBuilder): LoginBuilder {
        return LoginBuilder(account)
    }

    /**
     * End session
     *
     * @param account
     * @return
     */
    fun endSession(account: CyberArkAccountBuilder): EndSessionBuilder {
        return EndSessionBuilder(account)
    }

    /**
     * Refresh token
     *
     * @param account
     * @return
     */
    fun refreshToken(account: CyberArkAccountBuilder): RefreshTokenBuilder {
        return RefreshTokenBuilder(account)
    }

    /**
     * Enroll
     *
     * @return
     */
    fun enroll(): EnrollmentBuilder {
        return EnrollmentBuilder()
    }

    /**
     * Get authorize token
     *
     * @param intent
     * @return
     */
    @JvmStatic
    fun getAuthorizeToken(intent: Intent?): Boolean {
        if (cyberArkAuthInterface == null) {
            Log.i(TAG, "no previous instance present.")
            return false
        }
        if (cyberArkAuthInterface!!.updateResult(intent)) {
            cleanUp()
        }
        return true
    }

    /**
     * Clean up
     *
     */
    internal fun cleanUp() {
        cyberArkAuthInterface = null
    }

    /**
     * Login builder
     *
     * @property account
     * @constructor Create empty Login builder
     */
    class LoginBuilder internal constructor(
            private val account: CyberArkAccountBuilder
    ) {
        fun start(context: Context): LiveData<ResponseHandler<AuthCodeFlowModel>> {
            Log.i(TAG, "Invoke browser login flow")
            cleanUp()
            val cyberarkAuthManager = CyberArkAuthManager(context, account)
            cyberArkAuthInterface = cyberarkAuthManager
            cyberarkAuthManager.startAuthentication()

            return cyberarkAuthManager.getViewModelInstance.getAccessToken()
        }
    }

    /**
     * End session builder
     *
     * @property account
     * @constructor Create empty End session builder
     */
    class EndSessionBuilder internal constructor(
            private val account: CyberArkAccountBuilder
    ) {
        fun start(context: Context) {
            Log.i(TAG, "Invoke end session flow")
            cleanUp()
            val cyberarkAuthManager = CyberArkAuthManager(context, account)
            cyberArkAuthInterface = cyberarkAuthManager
            cyberarkAuthManager.endSession()
        }
    }

    /**
     * Refresh token builder
     *
     * @property account
     * @constructor Create empty Refresh token builder
     */
    class RefreshTokenBuilder internal constructor(
            private val account: CyberArkAccountBuilder
    ) {
        fun start(context: Context, refreshTokenData: String): LiveData<ResponseHandler<RefreshTokenModel>> {
            Log.i(TAG, "Invoke new access token using refresh token")
            val cyberarkAuthManager = CyberArkAuthManager(context, account)
            cyberarkAuthManager.refreshToken(refreshTokenData)

            return cyberarkAuthManager.getViewModelInstance.getRefreshToken()
        }
    }

    /**
     * Enrollment builder
     *
     * @constructor Create empty Enrollment builder
     */
    class EnrollmentBuilder internal constructor(
    ) {
        fun start(context: Context, accessToken: String): LiveData<ResponseHandler<EnrollmentModel>> {
            Log.i(TAG, "Start enroll")
            val cyberarkEnrollmentManager = CyberArkEnrollmentManager(context, accessToken)
            cyberarkEnrollmentManager.enroll()

            return cyberarkEnrollmentManager.getViewModelInstance.getEnrolledData()
        }
    }
}