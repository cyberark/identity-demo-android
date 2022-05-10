package com.cyberark.identity.data.model

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(SubmitOTPModel::class)
class SubmitOTPModelTest {

    private var submitOTPModel: SubmitOTPModel? = null

    @Before
    fun setUp() {
        submitOTPModel = SubmitOTPModel(
            true,
            "Submit OTP result",
            "Submit OTP message",
            "452242",
            "Exception",
            "ErrorID",
            "ErrorCode",
            false,
            "InnerExceptions"
        )
    }

    @Test
    fun getSuccess() {
        assertThat(submitOTPModel?.success, `is`(true))
    }

    @Test
    fun getResult() {
        assertThat(submitOTPModel?.Result, `is`("Submit OTP result"))
    }

    @Test
    fun getMessage() {
        assertThat(submitOTPModel?.Message, `is`("Submit OTP message"))
    }

    @Test
    fun getMessageID() {
        assertThat(submitOTPModel?.MessageID, `is`("452242"))
    }

    @Test
    fun getException() {
        assertThat(submitOTPModel?.Exception, `is`("Exception"))
    }

    @Test
    fun getErrorID() {
        assertThat(submitOTPModel?.ErrorID, `is`("ErrorID"))
    }

    @Test
    fun getErrorCode() {
        assertThat(submitOTPModel?.ErrorCode, `is`("ErrorCode"))
    }

    @Test
    fun isSoftError() {
        assertThat(submitOTPModel?.IsSoftError, `is`(false))
    }

    @Test
    fun getInnerExceptions() {
        assertThat(submitOTPModel?.InnerExceptions, `is`("InnerExceptions"))
    }
}