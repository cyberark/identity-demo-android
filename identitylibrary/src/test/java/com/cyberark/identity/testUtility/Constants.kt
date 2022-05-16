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

package com.cyberark.identity.testUtility

internal object Constants {
    const val systemURL = "https://aaj7617.my.dev.idaptive.app"
    const val domainURL = "acme2.my.dev.idaptive.app"
    const val clientID = "axis"
    const val appId = "testoauth10"
    const val responseType = "code"
    const val accessToken = "sampleaccessToken"
    const val scope = "All"
    const val redirectURL = "demo://acme2.my.dev.idaptive.app/android/com.cyberark.mfa/callback"

    const val deviceName = "deviceName"

    fun appendHTTPs(url:String) :String {
        return "https://"+url
    }

//    fun setupAccount(): CyberArkAccountBuilder {
//        val cyberArkAccountBuilder = CyberArkAccountBuilder.Builder()
//            .systemURL(systemURL)
//            .domainURL(domainURL)
//            .clientId(clientID)
//            .appId(appId)
//            .responseType(responseType)
//            .scope(scope)
//            .redirectUri(redirectURL)
//            .build()
//        // Print authorize URL
//        return cyberArkAccountBuilder
//    }
}