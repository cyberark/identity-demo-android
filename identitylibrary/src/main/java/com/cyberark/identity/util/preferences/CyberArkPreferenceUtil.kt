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

package com.cyberark.identity.util.preferences

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.preference.PreferenceManager
import java.lang.ref.WeakReference

/**
 * CyberArk shared preference util class
 *
 */
object CyberArkPreferenceUtil {
    private var mContext: WeakReference<Context>? = null
    fun init(context: Context) {
        mContext = WeakReference(context.applicationContext)
    }

    private val pref: SharedPreferences
        get() {
            checkNotNull(mContext) { "Please init the Utils with an application context." }
            return PreferenceManager.getDefaultSharedPreferences(mContext!!.get()!!)
        }
    val all: Map<String, *>
        get() = pref.all


    fun getString(key: String?, defValue: String?): String? {
        return pref.getString(key, defValue)
    }

    fun getStringSet(key: String?, defValues: Set<String?>?): Set<String>? {
        return pref.getStringSet(key, defValues)
    }

    fun getInt(key: String?, defValue: Int): Int {
        return pref.getInt(key, defValue)
    }

    fun getLong(key: String?, defValue: Long): Long {
        return pref.getLong(key, defValue)
    }

    fun getFloat(key: String?, defValue: Float): Float {
        return pref.getFloat(key, defValue)
    }

    fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return pref.getBoolean(key, defValue)
    }

    operator fun contains(key: String?): Boolean {
        return pref.contains(key)
    }

    val editor: SharedPreferences.Editor
        get() = pref.edit()

    fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener?) {
        pref.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener?) {
        pref.unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun putString(key: String?, value: String?): Boolean {
        return editor.putString(key, value).commit()
    }

    fun putStringSet(key: String?, values: Set<String?>?): Boolean {
        return editor.putStringSet(key, values).commit()
    }

    fun putInt(key: String?, value: Int): Boolean {
        return editor.putInt(key, value).commit()
    }

    fun putLong(key: String?, value: Long): Boolean {
        return editor.putLong(key, value).commit()
    }

    fun putFloat(key: String?, value: Float): Boolean {
        return editor.putFloat(key, value).commit()
    }

    fun putBoolean(key: String?, value: Boolean): Boolean {
        return editor.putBoolean(key, value).commit()
    }

    fun remove(key: String?): Boolean {
        return editor.remove(key).commit()
    }

    fun clear(): Boolean {
        return editor.clear().commit()
    }

    fun commit(): Boolean {
        return editor.commit()
    }

    fun apply() {
        editor.apply()
    }
}