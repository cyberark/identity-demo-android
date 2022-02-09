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

package com.cyberark.identity.activity.view

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.cyberark.identity.R
import com.cyberark.identity.util.widget.WidgetConstants

class CyberArkMFAWidgetFragment : Fragment(R.layout.fragment_mfa_widget) {

    private lateinit var progressBar: ProgressBar
    private lateinit var widgetWebView: MFAWidgetWebView
    private lateinit var mCallback: LoginSuccessListener

    /**
     * Define Login success listener
     */
    interface LoginSuccessListener {
        fun onLoginSuccess()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallback = if (context is LoginSuccessListener) {
            context
        } else {
            throw RuntimeException(
                "$context must implement LoginSuccessListener"
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get Widget URL
        val widgetURL = requireArguments().getString(WidgetConstants.WIDGET_URL, "")

        // Invoke UI
        progressBar = view.findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        widgetWebView= view.findViewById(R.id.webView)
        widgetWebView.addJavascriptInterface(MFAWidgetInterface(), "Android")

        // Load mfa widget url
        widgetWebView.loadUrl(widgetURL)
        // Set web view client
        widgetWebView.webViewClient = MFAWidgetWebViewClient()
    }

    /**
     * Instantiate MFA widget interface and handle login success
     */
    inner class MFAWidgetInterface() {

        @JavascriptInterface
        fun loginSuccessHandler() {
            Log.i("CyberArkMFAWidgetFragment", "loginSuccessHandler()")
            mCallback.onLoginSuccess()
        }
    }

    inner class MFAWidgetWebViewClient() : WebViewClient() {

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
        }

        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            view!!.postDelayed({
                progressBar.visibility = View.GONE
            }, 2000)
        }
    }

    private fun clearCookies() {
        WebStorage.getInstance().deleteAllData()
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.removeSessionCookies(null)
        cookieManager.flush()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearCookies()
    }
}