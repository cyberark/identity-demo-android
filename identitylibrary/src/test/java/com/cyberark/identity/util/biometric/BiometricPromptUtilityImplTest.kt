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

package com.cyberark.identity.util.biometric

import android.content.Context
import android.content.res.Resources
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import com.cyberark.identity.R
import com.cyberark.identity.util.AlertDialogHandler
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.internal.matchers.Null
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.reflect.Whitebox
import org.junit.Assert.*

//import org.powermock.modules.junit4.PowerMockRunner

//@RunWith(::class)

@RunWith(PowerMockRunner::class)
@PrepareForTest(
    BiometricPromptUtilityImpl::class,
    TextUtils::class,
    BiometricPrompt::class,
    AlertDialogHandler::class,
    BiometricAuthenticationCallback::class
)
class BiometricPromptUtilityImplTest {

    private val callback = object : BiometricAuthenticationCallback {
        override fun isAuthenticationSuccess(success: Boolean) {
            println("Authentication success")
        }

        override fun passwordAuthenticationSelected() {
            print("Password authentication selected")
        }

        override fun showErrorMessage(message: String) {
            print("Error message ${message}")
        }

        override fun isHardwareSupported(boolean: Boolean) {
            println("Hanrdware not detected")
        }

        override fun isSdkVersionSupported(boolean: Boolean) {

        }

        override fun isBiometricEnrolled(boolean: Boolean) {

        }

        override fun biometricErrorSecurityUpdateRequired() {

        }

    }

    val mockActivity: AppCompatActivity = PowerMockito.mock(AppCompatActivity::class.java)

    @Test
    fun testShowBioAuthentication() {
        val appContext = PowerMockito.mock(Context::class.java)
        Mockito.`when`(mockActivity.applicationContext).thenReturn(appContext)
        val biometricManager = PowerMockito.spy(BiometricManager.from(mockActivity))
        val biometricPromptUtility = PowerMockito.spy(BiometricPromptUtilityImpl(callback))

        biometricPromptUtility.showBioAuthentication(mockActivity, null, "Use App Pin", false)
        biometricPromptUtility.showBioAuthentication(mockActivity, null, "Use App Pin")
        Mockito.verify(mockActivity, Mockito.atLeastOnce()).applicationContext
    }


    private fun getBioMetric() = PowerMockito.mock(BiometricManager::class.java)

    private fun getBioMetricPrompt() = PowerMockito.mock(BiometricPrompt::class.java)


    @Test
    public fun testCheckBioMetric() {
        val biometricManager = getBioMetric()
        val biometricPromptUtility = PowerMockito.spy(BiometricPromptUtilityImpl(callback))
        val biometricType = BiometricManager.Authenticators.BIOMETRIC_STRONG
        PowerMockito.doReturn(biometricManager)
            .`when`(biometricPromptUtility, "getBioMetric", any())
        PowerMockito.doReturn(BiometricManager.BIOMETRIC_SUCCESS).`when`(
            biometricManager,
            "canAuthenticate",
            biometricType
        )
        Mockito.verify(biometricManager, atMostOnce()).canAuthenticate(biometricType)

        val mockBiometricPrompt = getBioMetricPrompt()
        registerBiometricPrompt(mockBiometricPrompt, biometricPromptUtility)
        Whitebox.invokeMethod<Null>(biometricPromptUtility, "checkAndAuthenticate", mockActivity)

        PowerMockito.doReturn(BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE).`when`(
            biometricManager,
            "canAuthenticate",
            biometricType
        )
        Whitebox.invokeMethod<Null>(biometricPromptUtility, "checkAndAuthenticate", mockActivity)
        Mockito.verify(biometricManager, atMost(2)).canAuthenticate(biometricType)

        PowerMockito.doReturn(BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE).`when`(
            biometricManager,
            "canAuthenticate",
            biometricType
        )
        Whitebox.invokeMethod<Null>(biometricPromptUtility, "checkAndAuthenticate", mockActivity)
        Mockito.verify(biometricManager, atMost(3)).canAuthenticate(biometricType)

        PowerMockito.`when`(mockActivity.getString(R.string.dialog_biometric_prompt_desc))
            .thenReturn("biometricDescription")
        PowerMockito.mock(Resources.Theme::class.java)

        val alertDialogHandler = PowerMockito.mock(AlertDialogHandler::class.java)
        PowerMockito.whenNew(AlertDialogHandler::class.java).withAnyArguments()
            .thenReturn(alertDialogHandler)
        PowerMockito.doReturn(BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED).`when`(
            biometricManager,
            "canAuthenticate",
            biometricType
        )
        Mockito.verify(biometricManager, atMost(4)).canAuthenticate(biometricType)

        Whitebox.invokeMethod<Null>(biometricPromptUtility, "checkAndAuthenticate", mockActivity)
        PowerMockito.doReturn(BiometricManager.BIOMETRIC_STATUS_UNKNOWN).`when`(
            biometricManager,
            "canAuthenticate",
            biometricType
        )
        Whitebox.invokeMethod<Null>(biometricPromptUtility, "checkAndAuthenticate", mockActivity)
        Mockito.verify(biometricManager, atMost(5)).canAuthenticate(biometricType)

        PowerMockito.doReturn(BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED).`when`(
            biometricManager,
            "canAuthenticate",
            biometricType
        )
        Whitebox.invokeMethod<Null>(biometricPromptUtility, "checkAndAuthenticate", mockActivity)
        Mockito.verify(biometricManager, atMost(6)).canAuthenticate(biometricType)

        PowerMockito.doReturn(BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED).`when`(
            biometricManager,
            "canAuthenticate",
            biometricType
        )
        Whitebox.invokeMethod<Null>(biometricPromptUtility, "checkAndAuthenticate", mockActivity)
        Mockito.verify(biometricManager, atMost(7)).canAuthenticate(biometricType)
    }

    private fun registerBiometricPrompt(
        biometricPrompt: BiometricPrompt,
        biometricPromptUtilityImpl: BiometricPromptUtilityImpl
    ) = PowerMockito.doReturn(biometricPrompt).`when`(
        biometricPromptUtilityImpl, "getBioMetricPrompt", any(), any(),
        any()
    )

    @Test
    public fun testCheckAuthenticate() {
        val biometricPromptUtility = PowerMockito.spy(BiometricPromptUtilityImpl(callback))
        val mockBiometricPrompt = getBioMetricPrompt()
        registerBiometricPrompt(mockBiometricPrompt, biometricPromptUtility)

        Whitebox.invokeMethod<BiometricPromptUtilityImpl>(
            biometricPromptUtility,
            "showBiometricPrompt",
            mockActivity
        )
        verify(mockBiometricPrompt, atMostOnce()).authenticate(any())
    }

    @Test
    public fun testSetAuthenticationError() {
        val biometricPromptUtility = PowerMockito.spy(BiometricPromptUtilityImpl(callback))
        val biometricPrompt  = getBioMetricPrompt()
        Whitebox.setInternalState(biometricPromptUtility, "mPrompt", biometricPrompt)
        val callback = Whitebox.invokeMethod<BiometricPrompt.AuthenticationCallback>(
            biometricPromptUtility,
            "getBioMetricCallback",
            null
        )
        callback.onAuthenticationError(
            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
            "Negitive button selected"
        )
        assertNull(Whitebox.getInternalState<BiometricPrompt>(biometricPromptUtility,"mPrompt"))

        callback.onAuthenticationError(
            BiometricPrompt.ERROR_USER_CANCELED,
            "User cancelled biometric authentication"
        )
        assertNull(Whitebox.getInternalState<BiometricPrompt>(biometricPromptUtility,"mPrompt"))
    }

    @Test
    public fun testSetAuthenticationFailed() {
        val biometricPromptUtility = PowerMockito.spy(BiometricPromptUtilityImpl(callback))
        Whitebox.setInternalState(biometricPromptUtility, "mPrompt", getBioMetricPrompt())
        val callback = Whitebox.invokeMethod<BiometricPrompt.AuthenticationCallback>(
            biometricPromptUtility,
            "getBioMetricCallback",
            null
        )
        Whitebox.setInternalState(biometricPromptUtility, "isAutoCancelElabled", true)
        callback.onAuthenticationFailed()
        assertNull(Whitebox.getInternalState<BiometricPrompt>(biometricPromptUtility,"mPrompt"))
    }

    @Test
    public fun testSetAuthenticationSuccess() {
        val biometricPromptUtility = PowerMockito.spy(BiometricPromptUtilityImpl(callback))
        Whitebox.setInternalState(biometricPromptUtility, "mPrompt", getBioMetricPrompt())
        val callback = Whitebox.invokeMethod<BiometricPrompt.AuthenticationCallback>(
            biometricPromptUtility,
            "getBioMetricCallback",
            null
        )
        Whitebox.setInternalState(biometricPromptUtility, "isAutoCancelElabled", true)
        callback.onAuthenticationSucceeded(PowerMockito.mock(BiometricPrompt.AuthenticationResult::class.java))
        assertNull(Whitebox.getInternalState<BiometricPrompt>(biometricPromptUtility,"mPrompt"))
    }

    @Test
    public fun testLaunchBiometricSetup() {
        val biometricPromptUtility = PowerMockito.spy(BiometricPromptUtilityImpl(callback))
        Whitebox.invokeMethod<Null>(biometricPromptUtility, "launchBiometricSetup", mockActivity)
        verify(mockActivity, atMostOnce()).startActivity(any())
    }


}