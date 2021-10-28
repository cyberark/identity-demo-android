package com.cyberark.identity.provider

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.cyberark.identity.testUtility.Constants
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.data.network.CyberArkAuthBuilder
import com.cyberark.identity.data.network.CyberArkAuthHelper
import com.cyberark.identity.util.device.DeviceInfoHelper
import com.cyberark.identity.viewmodel.EnrollmentViewModel
import com.cyberark.identity.viewmodel.base.CyberArkViewModelFactory
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PowerMockIgnore
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(
    CyberArkAccountBuilder::class,
    EnrollmentViewModel::class,
    CyberArkViewModelFactory::class,
    ViewModelStore::class,
    CyberArkEnrollmentManager::class,
    DeviceInfoHelper::class
)
@PowerMockIgnore("javax.net.ssl.*")
class CyberArkEnrollmentManagerTest  {

    @Mock
    lateinit var appCompactActivty: AppCompatActivity
    @Mock
    internal lateinit var enrollmentViewModel: EnrollmentViewModel
    @Mock
    internal lateinit var cyberArkBuilder: CyberArkAccountBuilder

    private lateinit var authEnrollManager:CyberArkEnrollmentManager


    @Before
    fun setUp() {
        val application = PowerMockito.mock(Application::class.java)
        val viewModelStore = PowerMockito.mock(ViewModelStore::class.java)
        PowerMockito.`when`(appCompactActivty.application).thenReturn(application)
        PowerMockito.`when`(appCompactActivty.viewModelStore).thenReturn(viewModelStore)

        val viewModelFactory = PowerMockito.mock(CyberArkViewModelFactory::class.java)
        PowerMockito.whenNew(CyberArkViewModelFactory::class.java).withArguments(
            Mockito.any(
                CyberArkAuthHelper::class.java
            )
        ).thenReturn(viewModelFactory)
        CyberArkViewModelFactory(CyberArkAuthHelper(CyberArkAuthBuilder.CYBER_ARK_AUTH_SERVICE))
        val viewModelProvider = PowerMockito.mock(ViewModelProvider::class.java)
        PowerMockito.whenNew(ViewModelProvider::class.java).withAnyArguments().thenReturn(viewModelProvider)
        PowerMockito.`when`(viewModelProvider.get(EnrollmentViewModel::class.java)).thenReturn(enrollmentViewModel)
        PowerMockito.`when`(cyberArkBuilder.getBaseSystemUrl).thenReturn(Constants.systemURL)
        authEnrollManager = CyberArkEnrollmentManager(appCompactActivty,Constants.accessToken,cyberArkBuilder)
    }

    @Test
    public fun enroll() {
//        PowerMockito.mockStatic(Build::class.java)
//        PowerMockito.`when`(Build.MODEL).thenReturn(deviceName)
        val deviceInfoHelper = PowerMockito.mock(DeviceInfoHelper::class.java)

        PowerMockito.`when`(deviceInfoHelper.getDeviceName()).thenReturn(Constants.deviceName)
        PowerMockito.whenNew(DeviceInfoHelper::class.java).withAnyArguments().thenReturn(deviceInfoHelper)
        authEnrollManager.enroll()
        verify(enrollmentViewModel).handleEnrollment(any(), any())
    }
}