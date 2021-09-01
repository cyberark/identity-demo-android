package com.cyberark.identity.activity

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.cyberark.identity.Utility.RobolectricBase
import org.junit.Assert.assertEquals
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController


class CyberArkRedirectActivityTest : RobolectricBase() {

    @Test
    fun testCreate() {
        val activityController = createAuthActivity()
        activityController.create()
        ApplicationProvider.getApplicationContext<Context>()
        val startedActivity =
            shadowOf(activityController.get()).getNextStartedActivity().component?.className;
        assertEquals(startedActivity,CyberArkAuthActivity::class.java.name)
    }

    private fun createAuthActivity(): ActivityController<CyberArkRedirectActivity> {
        val redirectActivity =
            Robolectric.buildActivity(CyberArkRedirectActivity::class.java, createIntent())
        return redirectActivity
    }

    private fun createIntent(): Intent {
        val intent = Intent()
        return intent
    }
}