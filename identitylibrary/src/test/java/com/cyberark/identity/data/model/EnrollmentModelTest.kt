package com.cyberark.identity.data.model

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(EnrollmentModel::class)
class EnrollmentModelTest {

    private var enrollmentModel: EnrollmentModel? = null
    private var enrollmentResult: EnrollmentResult? = null

    @Before
    fun setUp() {
        enrollmentResult = EnrollmentResult(
            "Message",
            "Status",
            "Client API Key",
            "Error Code"
        )
        enrollmentModel = EnrollmentModel(
            true,
            enrollmentResult!!,
            "Message",
            "Message ID",
            "Exception",
            "Error ID",
            "Error Code",
            false,
            "Inner Exceptions"
        )
    }

    @Test
    fun getResultMessage() {
        assertThat(enrollmentResult?.Message, `is`("Message"))
    }

    @Test
    fun getResultStatus() {
        assertThat(enrollmentResult?.Status, `is`("Status"))
    }

    @Test
    fun getResultAPIKey() {
        assertThat(enrollmentResult?.ClientApiKey, `is`("Client API Key"))
    }

    @Test
    fun getResultErrorCode() {
        assertThat(enrollmentResult?.ErrorCode, `is`("Error Code"))
    }

    @Test
    fun getSuccess() {
        assertThat(enrollmentModel?.success, `is`(true))
    }

    @Test
    fun getMessage() {
        assertThat(enrollmentModel?.Message, `is`("Message"))
    }

    @Test
    fun getMessageId() {
        assertThat(enrollmentModel?.MessageID, `is`("Message ID"))
    }

    @Test
    fun getException() {
        assertThat(enrollmentModel?.Exception, `is`("Exception"))
    }

    @Test
    fun getErrorId() {
        assertThat(enrollmentModel?.ErrorID, `is`("Error ID"))
    }

    @Test
    fun getErrorCode() {
        assertThat(enrollmentModel?.ErrorCode, `is`("Error Code"))
    }

    @Test
    fun isSoftError() {
        assertThat(enrollmentModel?.IsSoftError, `is`(false))
    }

    @Test
    fun getInnerExceptions() {
        assertThat(enrollmentModel?.InnerExceptions, `is`("Inner Exceptions"))
    }
}