package com.cyberark.identity.activity

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import com.cyberark.identity.data.model.QRCodeLoginModel
import com.cyberark.identity.util.ResponseHandler
import com.cyberark.identity.viewmodel.ScanQRCodeViewModel
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.journeyapps.barcodescanner.CaptureActivity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.internal.matchers.Null
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PowerMockIgnore
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.rule.PowerMockRule
import org.powermock.reflect.Whitebox
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowToast

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
@PowerMockIgnore("org.mockito.*", "org.robolectric.*", "android.*", "androidx.*","javax.net.ssl.*")
@PrepareForTest(IntentIntegrator::class,IntentResult::class,ScanQRCodeViewModel::class,ViewModelProvider::class)
class CyberArkQRCodeLoginActivityTest {
    @get:Rule
    var rule = PowerMockRule()
    private lateinit var qrActivityController:ActivityController<CyberArkQRCodeLoginActivity>;
    private lateinit var liveData:MutableLiveData<ResponseHandler<QRCodeLoginModel>>

    @Before
    public fun setUp() {
        setUpViewModel()
    }

    @Test
    public fun testCreate() {
        val application: Application = ApplicationProvider.getApplicationContext()
        val app: ShadowApplication = Shadows.shadowOf(application)
        app.grantPermissions(
            Manifest.permission.CAMERA
        )
        qrActivityController = createQRAuthActivity()
//        qrActivityController.create()
        setActivityResult()
    }

    @Test
    public fun testSendQRResult() {
        testCreate()
        val qrModel = QRCodeLoginModel()
        Whitebox.invokeMethod<Null>(qrActivityController.get(),"gotUpdatedResult",ResponseHandler.success(qrModel))
        assertEquals(shadowOf(qrActivityController.get()).resultCode, Activity.RESULT_OK)

        Whitebox.invokeMethod<Null>(qrActivityController.get(),"gotUpdatedResult",ResponseHandler.error("Fetch failed",null))
        assertEquals(shadowOf(qrActivityController.get()).resultCode, Activity.RESULT_OK)
    }

    private fun setUpViewModel() {
        val viewModelProvider = PowerMockito.mock(ViewModelProvider::class.java)

        PowerMockito.whenNew(ViewModelProvider::class.java).withAnyArguments().thenReturn(viewModelProvider)
        val mockModel = PowerMockito.mock(ScanQRCodeViewModel::class.java)
        PowerMockito.`when`(viewModelProvider.get(ScanQRCodeViewModel::class.java)).thenReturn(mockModel)
        liveData = MutableLiveData<ResponseHandler<QRCodeLoginModel>>()
        PowerMockito.`when`(mockModel.qrCodeLogin()).thenReturn(liveData)
    }

    private fun setActivityResult() {

        val startIntent = Intent(qrActivityController.get(), CaptureActivity::class.java)
        val completedIntent = Intent()
        PowerMockito.mockStatic(IntentIntegrator::class.java)
        val intentResult = PowerMockito.mock(IntentResult::class.java)
        PowerMockito.`when`(IntentIntegrator.parseActivityResult(1, 2,startIntent)).thenReturn(intentResult)

        qrActivityController.get().onActivityResult(1,2,startIntent)
        assertEquals(ShadowToast.getTextOfLatestToast().toString(),"Cancelled")

        val qrCodeData = "QRCodedata"

//        PowerMockito.`when`(intentResult.contents).thenReturn(qrCodeData)
        qrActivityController.get().onActivityResult(1,2,startIntent)
//        liveData.postValue(Respon)
//        val activity = shadowOf(qrActivityController.get())
//        activity.receiveResult(startIntent,Activity.RESULT_OK,completedIntent)
    }

    private fun createQRAuthActivity(): ActivityController<CyberArkQRCodeLoginActivity> {
        val redirectActivity =
            Robolectric.buildActivity(CyberArkQRCodeLoginActivity::class.java,createIntent())
        return redirectActivity
    }

    private fun createIntent(): Intent {
        val intent = Intent()
        intent.putExtra("access_token","sampleAccessToken")
        return intent
    }

}