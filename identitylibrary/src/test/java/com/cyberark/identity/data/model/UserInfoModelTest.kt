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
@PrepareForTest(UserInfoModel::class)
class UserInfoModelTest {

    private var userInfoModel: UserInfoModel? = null

    @Before
    fun setUp() {
        userInfoModel = UserInfoModel(
            "12538745",
            "Acme1",
            "Acme2",
            "aa@aa.com",
            "Acme3",
            "Acme4",
            "Acme5",
            true
        )
    }

    @Test
    fun shouldReturnNameIfMissingOtherDetails() {
        val name = "mockName"
        val userInfoModel = UserInfoModel(
            null,
            null,
            name,
            null,
            null,
            null,
            null,
            null
        )
      assertThat(userInfoModel.name, `is`("mockName"))
    }

    @Test
    fun shouldGetNullIdIfNameMissing() {
        val userInfoModel = UserInfoModel(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
        assertThat(userInfoModel.name, `is`(IsNull.nullValue()))
    }

    @Test
    fun shouldReturnEmailIfMissingOtherDetails() {
        val email = "aa@aa.com"
        val userInfoModel = UserInfoModel(
            null,
            null,
            null,
            email,
            null,
            null,
            null,
            null
        )
        assertThat(userInfoModel.email, `is`("aa@aa.com"))
    }

    @Test
    fun shouldGetNullIdIfEmailMissing() {
        val userInfoModel = UserInfoModel(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
        assertThat(userInfoModel.email, `is`(IsNull.nullValue()))
    }

    @Test
    fun getAuthTime() {
        assertThat(userInfoModel?.auth_time, `is`("12538745"))
    }

    @Test
    fun getGivenName() {
        assertThat(userInfoModel?.given_name, `is`("Acme1"))
    }

    @Test
    fun getName() {
        assertThat(userInfoModel?.name, `is`("Acme2"))
    }

    @Test
    fun getEmail() {
        assertThat(userInfoModel?.email, `is`("aa@aa.com"))
    }

    @Test
    fun getFamilyName() {
        assertThat(userInfoModel?.family_name, `is`("Acme3"))
    }

    @Test
    fun getPreferredUserName() {
        assertThat(userInfoModel?.preferred_username, `is`("Acme4"))
    }

    @Test
    fun getUniqueName() {
        assertThat(userInfoModel?.unique_name, `is`("Acme5"))
    }

    @Test
    fun getEmailVerified() {
        assertThat(userInfoModel?.email_verified, `is`(true))
    }
}