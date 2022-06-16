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

package com.cyberark.identity.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.cyberark.identity.testUtility.RobolectricBase
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
@Ignore
class CyberArkRedirectActivityTest : RobolectricBase() {

    @Test
    fun testCreate() {
        val activityController = createAuthActivity()
        activityController.create()
        ApplicationProvider.getApplicationContext<Context>()
        val startedActivity =
            shadowOf(activityController.get()).nextStartedActivity.component?.className
        assertEquals(startedActivity,CyberArkAuthActivity::class.java.name)
    }

    private fun createAuthActivity(): ActivityController<CyberArkRedirectActivity> {
        val redirectActivity =
            Robolectric.buildActivity(CyberArkRedirectActivity::class.java, createIntent())
        return redirectActivity
    }

    private fun createIntent(): Intent {
        val intent = Intent()
        intent.data = Uri.EMPTY
        return intent
    }
}