package com.cyberark.identity

import android.os.Bundle
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.cyberark.identity.test", appContext.packageName)
    }

//    @Test
//    fun testEvent() {
//        val scenario = ActivityScenario.launch(ScanQRCodeLoginActivity::class.java, Bundle().apply {
//            putString("access_token", "accessTokenData")
//        })
//
//    }
}