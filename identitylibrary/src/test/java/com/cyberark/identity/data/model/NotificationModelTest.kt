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
@PrepareForTest(NotificationDataModel::class)
class NotificationModelTest {

    private var notificationDataModel: NotificationDataModel? = null

    @Before
    fun setUp() {
        notificationDataModel = NotificationDataModel(
            "Title",
            "App Icon URL",
            "IP Address",
            "Challenge Answer",
            "Location",
            "Message",
            "Collapse ID",
            "Target Auth User",
            "Login Type",
            "Expiry Date",
            "Acme",
            "Auth Request Date",
            "Command UUID",
            "IN",
            "India"
        )
    }

    @Test
    fun shouldReturnTitleIfMissingOtherDetails() {
        val title = "Title"
        val notificationDataModel = NotificationDataModel(
            title,
            null,
            null,
            "Challenge Answer",
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
        assertThat(notificationDataModel.Title, `is`("Title"))
        assertThat(notificationDataModel.ChallengeAnswer, `is`("Challenge Answer"))
    }

    @Test
    fun shouldGetNullIfTitleMissing() {
        val notificationDataModel = NotificationDataModel(
            null,
            null,
            null,
            "Challenge Answer",
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
        assertThat(notificationDataModel.Title, `is`(IsNull.nullValue()))
    }

    @Test
    fun getTitle() {
        assertThat(notificationDataModel?.Title, `is`("Title"))
    }

    @Test
    fun getAppIconUrl() {
        assertThat(notificationDataModel?.AppIconUrl, `is`("App Icon URL"))
    }

    @Test
    fun getIpAddress() {
        assertThat(notificationDataModel?.IpAddress, `is`("IP Address"))
    }

    @Test
    fun getChallengeAnswer() {
        assertThat(notificationDataModel?.ChallengeAnswer, `is`("Challenge Answer"))
    }

    @Test
    fun getLocation() {
        assertThat(notificationDataModel?.Location, `is`("Location"))
    }

    @Test
    fun getMessage() {
        assertThat(notificationDataModel?.Message, `is`("Message"))
    }

    @Test
    fun getCollapseId() {
        assertThat(notificationDataModel?.CollapseId, `is`("Collapse ID"))
    }

    @Test
    fun getTargetAuthUser() {
        assertThat(notificationDataModel?.TargetAuthUser, `is`("Target Auth User"))
    }

    @Test
    fun getLoginType() {
        assertThat(notificationDataModel?.LoginType, `is`("Login Type"))
    }

    @Test
    fun getExpiryDate() {
        assertThat(notificationDataModel?.ExpiryDate, `is`("Expiry Date"))
    }

    @Test
    fun getAcme() {
        assertThat(notificationDataModel?.AppName, `is`("Acme"))
    }

    @Test
    fun getAuthRequestDate() {
        assertThat(notificationDataModel?.AuthRequestDate, `is`("Auth Request Date"))
    }

    @Test
    fun getCommandUUID() {
        assertThat(notificationDataModel?.CommandUuid, `is`("Command UUID"))
    }

    @Test
    fun getCountryCode() {
        assertThat(notificationDataModel?.CountryCode, `is`("IN"))
    }

    @Test
    fun getCountryName() {
        assertThat(notificationDataModel?.CountryName, `is`("India"))
    }
}