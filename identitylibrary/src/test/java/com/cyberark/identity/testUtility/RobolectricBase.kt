package com.cyberark.identity.testUtility

import android.os.Build
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.core.classloader.annotations.PowerMockIgnore
import org.powermock.modules.junit4.PowerMockRunnerDelegate
import org.powermock.modules.junit4.rule.PowerMockRule
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
//@PowerMockRunnerDelegate(RobolectricTestRunner::class)
@PowerMockIgnore("org.mockito.*", "org.robolectric.*", "android.*", "androidx.*","javax.net.ssl.*")

open class RobolectricBase {
    @get:Rule
    var rule = PowerMockRule()

    @Test
    fun testMethod(){

    }
}