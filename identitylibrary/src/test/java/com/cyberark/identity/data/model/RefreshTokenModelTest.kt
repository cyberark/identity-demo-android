package com.cyberark.identity.data.model

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(RefreshTokenModel::class)
class RefreshTokenModelTest {

    private var refreshTokenModel: RefreshTokenModel? = null

    @Before
    fun setUp() {
        refreshTokenModel = RefreshTokenModel(
            "2Fgh92345677F7734567",
            "Authorization Token",
            569234.0,
            "All"
        )
    }

    @Test
    fun getAccessToken() {
        assertThat(refreshTokenModel?.access_token, `is`("2Fgh92345677F7734567"))
    }

    @Test
    fun getTokenType() {
        assertThat(refreshTokenModel?.token_type, `is`("Authorization Token"))
    }

    @Test
    fun getExpiresIn() {
        assertThat(refreshTokenModel?.expires_in, `is`(569234.0))
    }

    @Test
    fun getScope() {
        assertThat(refreshTokenModel?.scope, `is`("All"))
    }
}