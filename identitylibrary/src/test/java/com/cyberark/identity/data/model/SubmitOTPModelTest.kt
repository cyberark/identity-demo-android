/*
 * Copyright (c) 2022 CyberArk Software Ltd. All rights reserved.
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