package com.cyberark.identity.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.cyberark.identity.Utility.Constants
import com.cyberark.identity.Utility.RobolectricBase
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.provider.CyberArkAuthManager
import com.cyberark.identity.provider.CyberArkAuthProvider
import com.cyberark.identity.util.browser.CustomTabHelper
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController


@PrepareForTest(
    CustomTabHelper::class, CyberArkAuthProvider::class,
    CyberArkAuthProvider.LoginBuilder::class, CyberArkAuthManager::class
)
class CyberArkAuthActivityTest: RobolectricBase() {

    private val ACTIVITY_LAUNCHED = "ACTIVITY_LAUNCHED"

    @Test
    public fun testOnCreate() {
        PowerMockito.mockStatic(CyberArkAuthProvider::class.java)
        PowerMockito.`when`(CyberArkAuthProvider.getAuthorizeToken(anyOrNull())).thenReturn(true)
        val mockBundle = Bundle()

        val authActivityController = createAuthActivity()
        createAndResume(authActivityController,mockBundle)
        assertFalse(authActivityController.get().isFinishing)

        //On recreating intent
        mockBundle.putBoolean(ACTIVITY_LAUNCHED,true)
        val recreatedActivity = createAuthActivity()
        createAndResume(recreatedActivity,mockBundle)
        assertTrue(recreatedActivity.get().isFinishing)
    }

    private fun createAndResume(authActivityController: ActivityController<CyberArkAuthActivity>, bundle: Bundle) {
        authActivityController.create(bundle)
        authActivityController.resume()
    }

    private fun createAuthActivity(): ActivityController<CyberArkAuthActivity> {
        val authActivityController =
            Robolectric.buildActivity(CyberArkAuthActivity::class.java, createIntent())
        return authActivityController
    }


    private fun setupAccount(): CyberArkAccountBuilder {
        val cyberArkAccountBuilder = CyberArkAccountBuilder.Builder()
            .clientId("Axis")
            .domainURL("aaj7617.my.dev.idaptive.app")
            .appId("testoauth4")
            .responseType("code")
            .scope("All")
            .redirectUri("demo://aaj7617.my.dev.idaptive.app/android/com.cyberark.mfa/callback")
            .build()
        // Print authorize URL
        return cyberArkAccountBuilder
    }

    private fun createIntent(): Intent {
        val intent = Intent()
        val AUTHORIZE_URI = "AUTHORIZE_URI"

        intent.putExtra(AUTHORIZE_URI, Uri.parse(Constants.systemURL))
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return intent
    }
}