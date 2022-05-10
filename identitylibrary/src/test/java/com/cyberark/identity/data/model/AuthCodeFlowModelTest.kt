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