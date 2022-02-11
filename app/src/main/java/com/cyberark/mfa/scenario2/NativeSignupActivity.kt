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

package com.cyberark.mfa.scenario2

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.cyberark.mfa.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafetyNet
import org.json.JSONObject

class NativeSignupActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val TAG = "NativeSignupActivity"
        const val SITE_KEY = "6LdBpmgeAAAAAEUY3bvq_9d8nyOfcYZsE_uxT8SY"
        const val SITE_SECRET_KEY = "6LdBpmgeAAAAALY0e7weXE0qh_LWWRynAoAaa0aT"
    }

    private lateinit var tvVerify: TextView
    private lateinit var btnverifyCaptcha: Button
    private lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_signup)

        tvVerify = findViewById(R.id.textView)
        btnverifyCaptcha = findViewById(R.id.button)
        btnverifyCaptcha.setOnClickListener(this)
        queue = Volley.newRequestQueue(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.button -> {
                SafetyNet.getClient(this).verifyWithRecaptcha(SITE_KEY)
                    .addOnSuccessListener(this) { response ->
                        // Indicates communication with reCAPTCHA service was
                        // successful.
                        val userResponseToken = response.tokenResult
                        Log.e("response", userResponseToken!!)
                        if (userResponseToken.isNotEmpty()) {
                            // Validate the user response token using the
                            // reCAPTCHA siteverify API.
                            handleVerify(response.tokenResult!!)
                        }
                    }
                    .addOnFailureListener(this) { e ->
                        if (e is ApiException) {
                            // An error occurred when communicating with the
                            // reCAPTCHA service. Refer to the status code to
                            // handle the error appropriately.
                            Log.d(
                                TAG,
                                ("Error message: " + CommonStatusCodes.getStatusCodeString(e.statusCode))
                            )
                        } else {
                            // A different, unknown type of error occurred.
                            Log.d(TAG, "Unknown type of error: " + e.message)
                        }
                    }
            }
        }
    }

    private fun handleVerify(responseToken: String) {
        //it is google recaptcha site verify server
        //you can place your server url
        val url = "https://www.google.com/recaptcha/api/siteverify"

        val params: MutableMap<String, String> = HashMap()
        params["secret"] = SITE_SECRET_KEY
        params["response"] = responseToken

        val request: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.getBoolean("success")) {
                        tvVerify.text = "You're not a Robot"
                    }
                } catch (ex: Exception) {
                    Log.d(TAG, "Error message: " + ex.message)
                }
            },
            Response.ErrorListener { error -> Log.d(TAG, "Error message: " + error.message) }) {

            override fun getParams(): MutableMap<String, String> {
                return params
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            50000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(request)
    }
}