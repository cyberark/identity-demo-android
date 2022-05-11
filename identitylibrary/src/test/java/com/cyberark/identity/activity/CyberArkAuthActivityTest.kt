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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.cyberark.identity.testUtility.Constants
import com.cyberark.identity.testUtility.RobolectricBase
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.provider.manager.CyberArkAuthManager
import com.cyberark.identity.provider.CyberArkAuthProvider
import com.cyberark.identity.util.browser.CustomTabHelper
import org.junit.Assert
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
    fun testOnCreate() {
        PowerMockito.mockStatic(CyberArkAuthProvider::class.java)
        PowerMockito.`when`(CyberArkAuthProvider.getAuthorizeToken(anyOrNull())).thenReturn(true)
        val mockBundle = Bundle()

        val authActivityController = createAuthActivity()
        createAndResume(authActivityController,mockBundle)
        Assert.assertFalse(authActivityController.get().isFinishing)

        //On recreating intent
        mockBundle.putBoolean(ACTIVITY_LAUNCHED,true)
        val recreatedActivity = createAuthActivity()
        createAndResume(recreatedActivity,mockBundle)
        Assert.assertTrue(recreatedActivity.get().isFinishing)
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
            .hostURL("mock.my.dev.mock.app")
            .appId("testoauth4")
            .responseType("code")
            .scope("All")
            .redirectUri("demo://mock.my.dev.mock.app/android/com.mock.mfa/callback")
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