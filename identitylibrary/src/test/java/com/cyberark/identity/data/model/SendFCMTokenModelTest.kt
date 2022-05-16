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