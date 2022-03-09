/*
 * Copyright (c) 2021 CyberArk Software Ltd. All rights reserved.
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

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Notification data model
 *
 * @property Title: notification title
 * @property AppIconUrl: application iocn url
 * @property IpAddress: IP Address
 * @property ChallengeAnswer: notification challenge answer
 * @property Location: location
 * @property Message: notification body message
 * @property CollapseId: collapse id
 * @property TargetAuthUser: target user
 * @property LoginType: login type
 * @property ExpiryDate: expiry date
 * @property AppName: application name
 * @property AuthRequestDate: authentication requested date
 * @property CommandUuid: command uuid
 * @property CountryCode: country code
 * @property CountryName: country name
 *
 */
@Parcelize
data class NotificationDataModel(
    val Title: String?,
    val AppIconUrl: String?,
    val IpAddress: String?,
    val ChallengeAnswer: String,
    val Location: String?,
    val Message: String?,
    val CollapseId: String?,
    val TargetAuthUser: String?,
    val LoginType: String?,
    val ExpiryDate: String?,
    val AppName: String?,
    val AuthRequestDate: String?,
    val CommandUuid: String?,
    val CountryCode: String?,
    val CountryName: String?
) : Parcelable
