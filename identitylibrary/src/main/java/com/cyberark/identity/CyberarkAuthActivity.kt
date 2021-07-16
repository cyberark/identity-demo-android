package com.cyberark.identity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.cyberark.identity.provider.CyberarkAuthProvider.getAuthorizeToken
import com.cyberark.identity.util.browser.CustomTabHelper
import com.cyberark.identity.util.preferences.CyberarkPreferenceUtils

class CyberarkAuthActivity : AppCompatActivity() {

    private val TAG: String? = CyberarkAuthActivity::class.simpleName

    private var activityLaunched = false
    private var customTabHelper: CustomTabHelper = CustomTabHelper()

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CyberarkPreferenceUtils.init(this)
        if (savedInstanceState != null) {
            activityLaunched = savedInstanceState.getBoolean(ACTIVITY_LAUNCHED, false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putBoolean(ACTIVITY_LAUNCHED, activityLaunched)
    }

    override fun onResume() {
        super.onResume()
        val authData = intent
        if (!activityLaunched && authData.extras == null) {
            Log.i(TAG, "Activity intent doesn't hold the authorize uri to launch in browser")
            finish()
            return
        } else if (!activityLaunched) {
            Log.i(TAG, "launch uri in browser successfully")
            activityLaunched = true
            val extras = intent.extras
            val authorizeUri = extras!!.getParcelable<Uri>(AUTHORIZE_URI)
            launchUri(this, authorizeUri!!)
            return
        }
        Log.i(TAG, "Get authorize token")
        getAuthorizeToken(authData)
        finish()
    }

    internal companion object {
        private const val AUTHORIZE_URI = "com.cyberark.identity.AUTHORIZE_URI"
        private const val ACTIVITY_LAUNCHED = "com.cyberark.identity.ACTIVITY_LAUNCHED"

        @JvmStatic
        internal fun authenticateUsingCustomTab(
                context: Context,
                authorizeUri: Uri
        ) {
            val intent = Intent(context, CyberarkAuthActivity::class.java)
            intent.putExtra(AUTHORIZE_URI, authorizeUri)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(intent)
        }
    }

    fun launchUri(context: Context, uri: Uri?) {
        val builder = CustomTabsIntent.Builder()

        // show website title
        builder.setShowTitle(true)

        // animation for enter and exit of tab
        builder.setStartAnimations(context, android.R.anim.fade_in, android.R.anim.fade_out)
        builder.setExitAnimations(context, android.R.anim.fade_in, android.R.anim.fade_out)

        val customTabsIntent = builder.build()

        // check is chrome available
        val packageName = customTabHelper.getPackageNameToUse(context, uri.toString())

        if (packageName == null) {
            Log.i(TAG, "Chrome custom tab is not available")
            // if chrome not available open in web view
            //TODO.. handle error scenario
        } else {
            Log.i(TAG, "Chrome custom tab is available")
            customTabsIntent.intent.setPackage(packageName)
            customTabsIntent.launchUrl(context, uri!!)
        }
    }
}