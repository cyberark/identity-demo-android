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

package com.cyberark.identity.provider

import android.os.Bundle
import android.util.Log
import com.cyberark.identity.data.model.NotificationDataModel
import com.google.gson.Gson
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

internal class CyberArkProcessNotification {

    companion object {
        private const val TAG = "CyberArkProcessNotification"
        const val DATA_KEY = "data_key" // this is the key sent from Cloud
        const val DATA_VALUE = "data_value" // this is the value for the key
        const val NOTIFY_TIME = "notify_time" // this is the time when the cloud sends the push.
    }

    init {
        Log.i(TAG, "initialize CyberArkProcessNotification")
    }

    fun parseRemoteNotification(remoteMessageData: Map<String, String>): NotificationDataModel {
        val bundle = Bundle()
        bundle.putString(DATA_KEY, remoteMessageData[DATA_KEY])
        bundle.putString(DATA_VALUE, remoteMessageData[DATA_VALUE])
        bundle.putString(NOTIFY_TIME, remoteMessageData[NOTIFY_TIME])

        val decodedBundle = urlDecodeDataValue(bundle)
        val key = decodedBundle.getString(DATA_KEY)
        val notifyTime = decodedBundle.getString(NOTIFY_TIME)
        val value = decodedBundle.getString(DATA_VALUE)

        return Gson().fromJson(value.toString(), NotificationDataModel::class.java)
    }

    private fun urlDecodeDataValue(input: Bundle): Bundle {
        val out = Bundle(input)
        var value = out.getString(DATA_VALUE)
        if (value != null) {
            try {
                value = URLDecoder.decode(value, "UTF-8")
                out.putString(DATA_VALUE, value)
            } catch (e: UnsupportedEncodingException) {
                Log.e(TAG, "UrlDecodeDataValue Failed: ", e)
            }
        }
        return out
    }
}