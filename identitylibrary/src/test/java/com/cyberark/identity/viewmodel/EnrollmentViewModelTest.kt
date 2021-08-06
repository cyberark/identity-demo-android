package com.cyberark.identity.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.cyberark.identity.data.model.EnrollmentModel
import com.cyberark.identity.data.network.CyberarkAuthHelper
import com.cyberark.identity.util.ResponseHandler
import com.cyberark.identity.util.endpoint.EndpointUrls
import com.google.gson.JsonObject
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import okhttp3.RequestBody
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.reflect.Whitebox

@RunWith(PowerMockRunner::class)
@PrepareForTest(EnrollmentViewModel::class, CyberarkAuthHelper::class,JSONObject::class,EnrollmentModel::class)
class EnrollmentViewModelTest : TestCase() {

    private lateinit var cyberarkAuthHelper:CyberarkAuthHelper;
    private lateinit var enrollmentViewModel:EnrollmentViewModel

    @Mock
    private lateinit var enrollObserver: Observer<ResponseHandler<EnrollmentModel>>

    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    public override fun setUp() {
        cyberarkAuthHelper = PowerMockito.mock(CyberarkAuthHelper::class.java)
        enrollmentViewModel = EnrollmentViewModel(cyberarkAuthHelper)
    }

    @Test
    public fun testHandleEnrollment() {

        val acceptCode = "Code"
        val authorization = "Authrorization"
        val isIdaptive = true
        val isCentrify = true

        val jsonObject = JSONObject()
        jsonObject.put(EndpointUrls.HEADER_ACCEPT_LANGUAGE,acceptCode)
        jsonObject.put(EndpointUrls.HEADER_AUTHORIZATION,authorization)
        jsonObject.put(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT,isIdaptive)
        jsonObject.put(EndpointUrls.HEADER_X_CENTRIFY_NATIVE_CLIENT,isCentrify)

        val requestBody = Whitebox.invokeMethod<RequestBody>(enrollmentViewModel,"createJsonBody",getBodyPayload().toString())

        runBlocking {
            PowerMockito.`when`(cyberarkAuthHelper.fastEnrollV3(isCentrify,isIdaptive,acceptCode,authorization,requestBody)).thenReturn( PowerMockito.mock(EnrollmentModel::class.java))
            enrollmentViewModel.getEnrolledData().observeForever(enrollObserver)
            enrollmentViewModel.handleEnrollment(jsonObject,getBodyPayload())
            verify(enrollObserver, atLeastOnce()).onChanged(any())
        }

    }

    //TODO.. need to remove all hardcoded values
    private fun getBodyPayload(): JSONObject {
        val payload = JSONObject()
        payload.put("devicetype", "A")
        payload.put("name", "Pixel 5 (IMEI: 3fcf45ed38044067)")
        payload.put("simpleName", "Pixel 5")
        payload.put("version", "11")
        payload.put("udid", "3fcf45ed38044067-3fcf45ed38044067-1560043795")
        payload.put("Manufacturer", "Google")
        payload.put("imei", "3fcf45ed38044067")
        payload.put("os", "Android")
        payload.put("GoogleServiceEnabled", true)
        return payload
    }
}