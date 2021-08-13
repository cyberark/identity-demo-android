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

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
class QRCodeLoginModel {
    var success = false

    @SerializedName("Result")
    var result: Result? = null

    @SerializedName("Message")
    var message: String? = null

    @SerializedName("MessageID")
    var messageID: String? = null

    @SerializedName("Exception")
    var exception: String? = null

    @SerializedName("ErrorID")
    var errorID: String? = null

    @SerializedName("ErrorCode")
    var errorCode: String? = null

    @SerializedName("IsSoftError")
    var isSoftError: Boolean? = null

    @SerializedName("InnerExceptions")
    var innerExceptions: String? = null

    @SerializedName("PlainResult")
    var plainResult: String? = null

    inner class Result {
        @SerializedName("AuthLevel")
        var authLevel: String? = null

        @SerializedName("DisplayName")
        var displayName: String? = null

        @SerializedName("Auth")
        var auth: String? = null

        @SerializedName("UserId")
        var userId: String? = null

        @SerializedName("EmailAddress")
        var emailAddress: String? = null

        @SerializedName("UserDirectory")
        var userDirectory: String? = null

        @SerializedName("PodFqdn")
        var podFqdn: String? = null

        @SerializedName("User")
        var user: String? = null

        @SerializedName("CustomerID")
        var customerID: String? = null

        @SerializedName("SystemID")
        var systemID: String? = null

        @SerializedName("SourceDsType")
        var sourceDsType: String? = null

        @SerializedName("Summary")
        var summary: String? = null
    }
}