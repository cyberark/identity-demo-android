package com.cyberark.identity.builder

import android.util.Log
import com.cyberark.identity.util.PKCEHelper
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.*

class CyberarkAccountBuilder(
    val domainURL: String?,
    val clientId: String?,
    val appId: String?,
    val responseType: String?,
    val state: String?,
    val scope: String?,
    val redirectUri: String?
) {

    private val baseURL: HttpUrl?
    private var codeVerifier: String? = null
    private var codeChallenge: String? = null
    private val TAG: String? = CyberarkAccountBuilder::class.simpleName

    data class Builder(
        var domainURL: String? = null,
        var clientId: String? = null,
        var appId: String? = null,
        var responseType: String? = null,
        var state: String? = null,
        var scope: String? = null,
        var redirectUri: String? = null
    ) {

        fun domainURL(domainURL: String) = apply { this.domainURL = domainURL }
        fun clientId(clientId: String) = apply { this.clientId = clientId }
        fun appId(appId: String) = apply { this.appId = appId }
        fun responseType(responseType: String) = apply { this.responseType = responseType }
        fun state(state: String) = apply { this.state = state }
        fun scope(scope: String) = apply { this.scope = scope }
        fun redirectUri(redirectUri: String) = apply { this.redirectUri = redirectUri }
        fun build() = CyberarkAccountBuilder(
            domainURL,
            clientId,
            appId,
            responseType,
            state,
            scope,
            redirectUri
        )
    }

    companion object {
        const val KEY_RESPONSE_TYPE = "response_type"
        const val KEY_GRANT_TYPE = "grant_type"
        const val KEY_CODE = "code"
        const val KEY_REDIRECT_URI = "redirect_uri"
        const val KEY_CLIENT_ID = "client_id"
        const val KEY_SCOPE = "scope"
        const val KEY_CODE_VERIFIER = "code_verifier"
        const val KEY_CODE_CHALLENGE = "code_challenge"
        const val KEY_CODE_CHALLENGE_METHOD = "code_challenge_method"
        const val CODE_CHALLENGE_METHOD_VALUE = "S256"
    }

    val OAuthBaseURL: String
        get() = baseURL!!.newBuilder()
            .addPathSegment("oauth2")
            .addPathSegment("authorize")
            .addPathSegment(appId.toString())
            .addQueryParameter(KEY_RESPONSE_TYPE, responseType.toString())
            .addQueryParameter(KEY_CLIENT_ID, clientId.toString())
            .addQueryParameter(KEY_SCOPE, scope.toString())
            .addQueryParameter(KEY_CODE_CHALLENGE, getCodeChallenge)
            .addQueryParameter(KEY_CODE_CHALLENGE_METHOD, CODE_CHALLENGE_METHOD_VALUE)
            .addEncodedQueryParameter(KEY_REDIRECT_URI, redirectUri.toString())
            .build()
            .toString()

    val getRedirectURL: String
        get() = redirectUri.toString()

    val getClientId: String
        get() = clientId.toString()

    private fun checkValidUrl(url: String?): HttpUrl? {
        if (url == null) {
            return null
        }
        val normalizedUrl = url.toLowerCase(Locale.ROOT)
        require(!normalizedUrl.startsWith("http://")) { "Invalid domain url: '$url'." }
        val safeUrl =
            if (normalizedUrl.startsWith("https://")) normalizedUrl else "https://$normalizedUrl"
        return safeUrl.toHttpUrlOrNull()
    }

    init {
        baseURL = checkValidUrl(domainURL)
        requireNotNull(baseURL) { String.format("Invalid domain url: '%s'", domainURL) }

        val pkceHelper = PKCEHelper()
        val codeVerifier: String = pkceHelper.generateCodeVerifier()
        val codeChallenge: String = pkceHelper.generateCodeChallenge(codeVerifier)

        //TODO.. for testing only added this log and should be removed later
        Log.i(TAG, "codeVerifier :: " + codeVerifier)
        Log.i(TAG, "codeChallenge :: " + codeChallenge)

        codeVerifier.also { this.codeVerifier = it }
        codeChallenge.also { this.codeChallenge = it }
    }

    private val getCodeChallenge: String
        get() = codeChallenge.toString()

    val getCodeVerifier: String
        get() = codeVerifier.toString()
}