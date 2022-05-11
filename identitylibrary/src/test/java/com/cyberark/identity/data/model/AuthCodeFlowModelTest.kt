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
import org.hamcrest.core.IsNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(AuthCodeFlowModel::class)
class AuthCodeFlowModelTest {

    private var authCodeFlowModel: AuthCodeFlowModel? = null

    @Before
    fun setUp() {
        authCodeFlowModel = AuthCodeFlowModel(
            "access token",
            "Token Type",
            "ID Token",
            "Refresh Token",
            "State",
            60.0,
            "All"
        )
    }

    @Test
    fun getAccessToken() {
        assertThat(authCodeFlowModel?.access_token, `is`("access token"))
    }

    @Test
    fun getTokenType() {
        assertThat(authCodeFlowModel?.token_type, `is`("Token Type"))
    }

    @Test
    fun getIdToken() {
        assertThat(authCodeFlowModel?.id_token, `is`("ID Token"))
    }

    @Test
    fun getRefreshToken() {
        assertThat(authCodeFlowModel?.refresh_token, `is`("Refresh Token"))
    }

    @Test
    fun getState() {
        assertThat(authCodeFlowModel?.state, `is`("State"))
    }

    @Test
    fun getExpiresIn() {
        assertThat(authCodeFlowModel?.expires_in, `is`(60.0))
    }

    @Test
    fun getScope() {
        assertThat(authCodeFlowModel?.scope, `is`("All"))
    }
}