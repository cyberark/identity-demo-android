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