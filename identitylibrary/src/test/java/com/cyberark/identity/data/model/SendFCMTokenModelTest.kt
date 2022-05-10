package com.cyberark.identity.data.model

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(SendFCMTokenModel::class)
class SendFCMTokenModelTest {

    private var sendFCMTokenModel: SendFCMTokenModel? = null

    @Before
    fun setUp() {
        sendFCMTokenModel = SendFCMTokenModel(
            true
        )
    }

    @Test
    fun getSuccess() {
        assertThat(sendFCMTokenModel?.Status, `is`(true))
    }

    @Test
    fun getFailStatus() {
        val sendFCMTokenModel = SendFCMTokenModel(
            false
        )
        assertThat(sendFCMTokenModel.Status, `is`(false))
    }
}