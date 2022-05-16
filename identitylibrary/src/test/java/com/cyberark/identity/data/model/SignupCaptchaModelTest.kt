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
@PrepareForTest(
    SignupCaptchaModel::class,
    SignupCaptchaResult::class,
    SignupRoleIntegrationResult::class
)
class SignupCaptchaModelTest {

    private var signupCaptchaModel: SignupCaptchaModel? = null
    private var signupCaptchaResult: SignupCaptchaResult? = null
    private var signupRoleIntegrationResult: SignupRoleIntegrationResult? = null

    @Before
    fun setUp() {
        signupRoleIntegrationResult = SignupRoleIntegrationResult(
            true,
            "Signup role Message"
        )

        signupCaptchaResult = SignupCaptchaResult(
            signupRoleIntegrationResult!!,
            "User ID"
        )
        signupCaptchaModel = SignupCaptchaModel(
            true,
            signupCaptchaResult!!,
            "Signup Captcha Message",
            "Message ID",
            "Exception",
            "Error ID",
            "Error Code",
            false,
            "Inner Exceptions"
        )
    }

    @Test
    fun getSuccess() {
        assertThat(signupRoleIntegrationResult?.success, `is`(true))
    }

    @Test
    fun getMessage() {
        assertThat(signupRoleIntegrationResult?.Message, `is`("Signup role Message"))
    }

    @Test
    fun getCaptchaSuccess() {
        assertThat(signupCaptchaResult?.SignupRoleIntegrationResult?.success, `is`(true))
    }

    @Test
    fun getCaptchaMessage() {
        assertThat(
            signupCaptchaResult?.SignupRoleIntegrationResult?.Message,
            `is`("Signup role Message")
        )
    }

    @Test
    fun getUserId() {
        assertThat(signupCaptchaResult?.UserId, `is`("User ID"))
    }

    @Test
    fun getCaptchaModelSuccess() {
        assertThat(signupCaptchaModel?.success, `is`(true))
    }

    @Test
    fun getCaptchaModelMessage() {
        assertThat(signupCaptchaModel?.Message, `is`("Signup Captcha Message"))
    }

    @Test
    fun getCaptchaModelMessageId() {
        assertThat(signupCaptchaModel?.MessageID, `is`("Message ID"))
    }

    @Test
    fun getCaptchaModelException() {
        assertThat(signupCaptchaModel?.Exception, `is`("Exception"))
    }

    @Test
    fun getCaptchaModelErrorId() {
        assertThat(signupCaptchaModel?.ErrorID, `is`("Error ID"))
    }

    @Test
    fun getCaptchaModelErrorCode() {
        assertThat(signupCaptchaModel?.ErrorCode, `is`("Error Code"))
    }

    @Test
    fun getCaptchaModelIsSoftError() {
        assertThat(signupCaptchaModel?.IsSoftError, `is`(false))
    }

    @Test
    fun getCaptchaModelInnerExceptions() {
        assertThat(signupCaptchaModel?.InnerExceptions, `is`("Inner Exceptions"))
    }

    @Test
    fun getUserIdFromCaptchaModelInstance() {
        assertThat(signupCaptchaModel?.Result?.UserId, `is`("User ID"))
    }

    @Test
    fun getSuccessFromCaptchaModelInstance() {
        assertThat(signupCaptchaModel?.Result?.SignupRoleIntegrationResult?.success, `is`(true))
    }

    @Test
    fun getMessageFromCaptchaModelInstance() {
        assertThat(
            signupCaptchaModel?.Result?.SignupRoleIntegrationResult?.Message,
            `is`("Signup role Message")
        )
    }
}