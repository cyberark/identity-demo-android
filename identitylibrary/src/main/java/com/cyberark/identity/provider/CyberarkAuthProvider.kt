package com.cyberark.identity.provider

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import com.cyberark.identity.builder.CyberarkAccountBuilder
import com.cyberark.identity.data.model.AuthCodeFlowModel
import com.cyberark.identity.data.model.EnrollmentModel
import com.cyberark.identity.data.model.RefreshTokenModel
import com.cyberark.identity.util.ResponseHandler

object CyberarkAuthProvider {

    private val TAG: String? = CyberarkAuthProvider::class.simpleName
    internal var cyberarkAuthInterface: CyberarkAuthInterface? = null

    fun login(account: CyberarkAccountBuilder): Builder {
        return Builder(account)
    }

    fun endSession(account: CyberarkAccountBuilder): EndSessionBuilder {
        return EndSessionBuilder(account)
    }

    fun refreshToken(account: CyberarkAccountBuilder): RefreshTokenBuilder {
        return RefreshTokenBuilder(account)
    }

    fun enroll(): EnrollmentBuilder {
        return EnrollmentBuilder()
    }

    @JvmStatic
    public fun getAuthorizeToken(intent: Intent?): Boolean {
        if (cyberarkAuthInterface == null) {
            Log.i(TAG, "no previous instance present.")
            return false
        }
        if (cyberarkAuthInterface!!.updateResult(intent)) {
            cleanUp()
        }
        return true
    }

    internal fun cleanUp() {
        cyberarkAuthInterface = null
    }

    class Builder internal constructor(
            private val account: CyberarkAccountBuilder
    ) {
        fun start(context: Context): LiveData<ResponseHandler<AuthCodeFlowModel>> {
            Log.i(TAG, "Invoke browser login flow")
            cleanUp()
            val cyberarkAuthManager = CyberarkAuthManager(context, account)
            cyberarkAuthInterface = cyberarkAuthManager
            cyberarkAuthManager.startAuthentication()

            return cyberarkAuthManager.getViewModelInstance.getAccessToken()
        }
    }

    class EndSessionBuilder internal constructor(
            private val account: CyberarkAccountBuilder
    ) {
        fun start(context: Context) {
            Log.i(TAG, "Invoke end session flow")
            cleanUp()
            val cyberarkAuthManager = CyberarkAuthManager(context, account)
            cyberarkAuthInterface = cyberarkAuthManager
            cyberarkAuthManager.endSession()
        }
    }

    class RefreshTokenBuilder internal constructor(
            private val account: CyberarkAccountBuilder
    ) {
        fun start(context: Context, refreshTokenData: String): LiveData<ResponseHandler<RefreshTokenModel>> {
            Log.i(TAG, "Invoke new access token using refresh token")
            val cyberarkAuthManager = CyberarkAuthManager(context, account)
            cyberarkAuthManager.refreshToken(refreshTokenData)

            return cyberarkAuthManager.getViewModelInstance.getRefreshToken()
        }
    }

    class EnrollmentBuilder internal constructor(
    ) {
        fun start(context: Context, accessToken: String): LiveData<ResponseHandler<EnrollmentModel>> {
            Log.i(TAG, "Start enroll")
            val cyberarkEnrollmentManager = CyberarkEnrollmentManager(context, accessToken)
            cyberarkEnrollmentManager.enroll()

            return cyberarkEnrollmentManager.getViewModelInstance.getEnrolledData()
        }
    }
}