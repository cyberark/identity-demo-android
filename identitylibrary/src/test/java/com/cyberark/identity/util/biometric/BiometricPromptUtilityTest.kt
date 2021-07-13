package com.cyberark.identity.util.biometric


import android.content.Context
import android.content.res.Resources
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.cyberark.identity.R
import com.cyberark.identity.util.AlertButtonType
import com.cyberark.identity.util.AlertDialogButtonCallback
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
    BiometricPromptUtility::class,
    TextUtils::class,
    BiometricPrompt::class,
    AlertDialogHandler::class,
    BiometricAuthenticationCallback::class
)
class BiometricPromptUtilityTest {

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
    public fun testShowBioAuthentication() {
        val appContext = PowerMockito.mock(Context::class.java)
        Mockito.`when`(mockActivity.applicationContext).thenReturn(appContext)
        val biometricManager = PowerMockito.spy(BiometricManager.from(mockActivity))
        val biometricPromptUtility = PowerMockito.spy(BiometricPromptUtility(callback))

        biometricPromptUtility.showBioAuthentication(mockActivity, null, "Use App Pin", false)
        biometricPromptUtility.showBioAuthentication(mockActivity, null, "Use App Pin")
        Mockito.verify(mockActivity, Mockito.atLeastOnce()).applicationContext
    }


    private fun getBioMetric() = PowerMockito.mock(BiometricManager::class.java)

    private fun getBioMetricPrompt() = PowerMockito.mock(BiometricPrompt::class.java)


    @Test
    public fun testCheckBioMetric() {
        val biometricManager = getBioMetric()
        val biometricPromptUtility = PowerMockito.spy(BiometricPromptUtility(callback))
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

        PowerMockito.`when`(mockActivity.getString(R.string.cyberArkTitle)).thenReturn("Cyberark")
        PowerMockito.`when`(mockActivity.getString(R.string.biometricDescription))
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
        biometricPromptUtility: BiometricPromptUtility
    ) = PowerMockito.doReturn(biometricPrompt).`when`(
        biometricPromptUtility, "getBioMetricPrompt", any(), any(),
        any()
    )

    @Test
    public fun testCheckAuthenticate() {
        val biometricPromptUtility = PowerMockito.spy(BiometricPromptUtility(callback))
        val mockBiometricPrompt = getBioMetricPrompt()
        registerBiometricPrompt(mockBiometricPrompt, biometricPromptUtility)

        Whitebox.invokeMethod<BiometricPromptUtility>(
            biometricPromptUtility,
            "showBiometricPrompt",
            mockActivity
        )
        verify(mockBiometricPrompt, atMostOnce()).authenticate(any())
    }

    @Test
    public fun testSetAuthenticationError() {
        val biometricPromptUtility = PowerMockito.spy(BiometricPromptUtility(callback))
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
        val biometricPromptUtility = PowerMockito.spy(BiometricPromptUtility(callback))
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
        val biometricPromptUtility = PowerMockito.spy(BiometricPromptUtility(callback))
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
        val biometricPromptUtility = PowerMockito.spy(BiometricPromptUtility(callback))
        Whitebox.invokeMethod<Null>(biometricPromptUtility, "launchBiometricSetup", mockActivity)
        verify(mockActivity, atMostOnce()).startActivity(any())
    }


}