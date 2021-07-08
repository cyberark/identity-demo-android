package com.cyberark.identity.provider

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.cyberark.identity.CyberarkAuthActivity
import com.cyberark.identity.builder.CyberarkAccountBuilder
import com.cyberark.identity.data.network.CyberarkAuthBuilder
import com.cyberark.identity.data.network.CyberarkAuthHelper
import com.cyberark.identity.viewmodel.AuthenticationViewModel
import com.cyberark.identity.viewmodel.base.CyberarkViewModelFactory

object CyberarkAuthProvider {

    private val TAG: String? = CyberarkAuthProvider::class.simpleName
    private lateinit var viewModel: AuthenticationViewModel
    private lateinit var account: CyberarkAccountBuilder

    fun login(context: Context, account: CyberarkAccountBuilder): Builder {
        this.account = account
        return Builder(context, account)
    }

    fun endSession(context: Context, account: CyberarkAccountBuilder): EndSessionBuilder {
        this.account = account
        return EndSessionBuilder(context)
    }

    @JvmStatic
    public fun getAuthorizeToken(intent: Intent?): Boolean {
        val code = intent?.data?.getQueryParameter(CyberarkAccountBuilder.KEY_CODE)

        val params = HashMap<String?, String?>()
        params[CyberarkAccountBuilder.KEY_GRANT_TYPE] = "code"
        params[CyberarkAccountBuilder.KEY_CODE] = code.toString()

        params[CyberarkAccountBuilder.KEY_REDIRECT_URI] = account.getRedirectURL
        params[CyberarkAccountBuilder.KEY_CLIENT_ID] = account.getClientId
        params[CyberarkAccountBuilder.KEY_CODE_VERIFIER] = account.getCodeVerifier

        //TODO.. for testing only added this log and should be removed later
        Log.i(TAG, "params" + params.toString())

        if (code != null) {
            Log.i(TAG, "Code exchange for access token")
            viewModel.handleAuthorizationCode(params)
        } else {
            Log.i(TAG, "Unable to fetch code from server to get access token")
            // TODO.. handle error
        }
        return true
    }

    @JvmStatic
    public fun getAuthViewModel() = viewModel

    class Builder internal constructor(
        context: Context,
        private val account: CyberarkAccountBuilder
    ) {

        private val context: Context = context
        private val appContext: AppCompatActivity = context as AppCompatActivity

        fun setupViewModel(): Builder {
            viewModel = ViewModelProviders.of(
                appContext,
                CyberarkViewModelFactory(CyberarkAuthHelper(CyberarkAuthBuilder.cyberarkAuthService))
            ).get(AuthenticationViewModel::class.java)
            return this
        }

        fun start() {
            Log.i(TAG, "Start browser based authentication flow")
            CyberarkAuthActivity.authenticateUsingCustomTab(
                context,
                Uri.parse(account.OAuthBaseURL)
            )
        }
    }

    class EndSessionBuilder internal constructor(
        context: Context
    ) {
        private val context: Context = context

        fun start() {
            Log.i(TAG, "Invoke end session")
            CyberarkAuthActivity.authenticateUsingCustomTab(
                context,
                Uri.parse(account.OAuthEndSessionURL)
            )
        }
    }
}