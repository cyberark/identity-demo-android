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
@PrepareForTest(QRCodeLoginModel::class, QRCodeLoginResult::class)
class QRCodeLoginModelTest {

    private var qrCodeLoginModel: QRCodeLoginModel? = null
    private var qrCodeLoginResult: QRCodeLoginResult? = null

    @Before
    fun setUp() {
        qrCodeLoginResult = QRCodeLoginResult(
            "Auth Level",
            "Display Name",
            "Auth",
            "User ID",
            "Email Address",
            "User Directory",
            "POD FQDN",
            "User",
            "Customer ID",
            "System ID",
            "Source DS Type",
            "Summary"
        )
        qrCodeLoginModel = QRCodeLoginModel(
            true,
            qrCodeLoginResult,
            "Message",
            "Message ID",
            "Exception",
            "Error ID",
            "Error Code",
            false,
            "Inner Exceptions",
            "Plain Result"
        )
    }

    @Test
    fun shouldReturnUserIDIfMissingOtherDetails() {
        val userId = "User ID"
        qrCodeLoginResult = QRCodeLoginResult(
            null,
            null,
            null,
            userId,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
        qrCodeLoginModel = QRCodeLoginModel(
            true,
            qrCodeLoginResult,
            null,
            null,
            null,
            null,
            null,
            false,
            null,
            null
        )
        assertThat(qrCodeLoginResult?.UserId, `is`("User ID"))
        assertThat(qrCodeLoginModel?.Result?.UserId, `is`("User ID"))
    }

    @Test
    fun shouldGetNullIdIfUserIDMissing() {
        qrCodeLoginResult = QRCodeLoginResult(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
        qrCodeLoginModel = QRCodeLoginModel(
            true,
            qrCodeLoginResult,
            null,
            null,
            null,
            null,
            null,
            false,
            null,
            null
        )
        assertThat(qrCodeLoginResult?.UserId, `is`(IsNull.nullValue()))
        assertThat(qrCodeLoginModel?.Result?.UserId, `is`(IsNull.nullValue()))
    }

    @Test
    fun getAuthLevel() {
        assertThat(qrCodeLoginResult?.AuthLevel, `is`("Auth Level"))
    }

    @Test
    fun getDisplayName() {
        assertThat(qrCodeLoginResult?.DisplayName, `is`("Display Name"))
    }

    @Test
    fun getAuth() {
        assertThat(qrCodeLoginResult?.Auth, `is`("Auth"))
    }

    @Test
    fun getUserId() {
        assertThat(qrCodeLoginResult?.UserId, `is`("User ID"))
    }

    @Test
    fun getEmailAddress() {
        assertThat(qrCodeLoginResult?.EmailAddress, `is`("Email Address"))
    }

    @Test
    fun getUserDirectory() {
        assertThat(qrCodeLoginResult?.UserDirectory, `is`("User Directory"))
    }

    @Test
    fun getPodFqdn() {
        assertThat(qrCodeLoginResult?.PodFqdn, `is`("POD FQDN"))
    }

    @Test
    fun getUser() {
        assertThat(qrCodeLoginResult?.User, `is`("User"))
    }

    @Test
    fun getCustomerId() {
        assertThat(qrCodeLoginResult?.CustomerID, `is`("Customer ID"))
    }

    @Test
    fun getSystemId() {
        assertThat(qrCodeLoginResult?.SystemID, `is`("System ID"))
    }

    @Test
    fun getSourceDSType() {
        assertThat(qrCodeLoginResult?.SourceDsType, `is`("Source DS Type"))
    }

    @Test
    fun getSummary() {
        assertThat(qrCodeLoginResult?.Summary, `is`("Summary"))
    }

    @Test
    fun getSuccess() {
        assertThat(qrCodeLoginModel?.success, `is`(true))
    }

    @Test
    fun getMessage() {
        assertThat(qrCodeLoginModel?.Message, `is`("Message"))
    }

    @Test
    fun getMessageId() {
        assertThat(qrCodeLoginModel?.MessageID, `is`("Message ID"))
    }

    @Test
    fun getException() {
        assertThat(qrCodeLoginModel?.Exception, `is`("Exception"))
    }

    @Test
    fun getErrorId() {
        assertThat(qrCodeLoginModel?.ErrorID, `is`("Error ID"))
    }

    @Test
    fun getErrorCode() {
        assertThat(qrCodeLoginModel?.ErrorCode, `is`("Error Code"))
    }

    @Test
    fun isSoftError() {
        assertThat(qrCodeLoginModel?.IsSoftError, `is`(false))
    }

    @Test
    fun getInnerExceptions() {
        assertThat(qrCodeLoginModel?.InnerExceptions, `is`("Inner Exceptions"))
    }

    @Test
    fun getPlianResult() {
        assertThat(qrCodeLoginModel?.plainResult, `is`("Plain Result"))
    }
}