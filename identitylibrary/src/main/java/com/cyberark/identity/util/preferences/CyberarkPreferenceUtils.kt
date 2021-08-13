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
 * Cyberark preference utils
 *
 * @constructor Create empty Cyberark preference utils
 */
object CyberarkPreferenceUtils {
    private var mContext: WeakReference<Context>? = null
    fun init(context: Context) {
        mContext = WeakReference(context.applicationContext)
    }

    private val pref: SharedPreferences
        get() {
            checkNotNull(mContext) { "Please init the Utils with an application context." }
            return PreferenceManager.getDefaultSharedPreferences(mContext!!.get())
        }
    val all: Map<String, *>
        get() = pref.all

    /**
     * Get string
     *
     * @param key
     * @param defValue
     * @return
     */
    fun getString(key: String?, defValue: String?): String? {
        return pref.getString(key, defValue)
    }

    /**
     * Get string set
     *
     * @param key
     * @param defValues
     * @return
     */
    fun getStringSet(key: String?, defValues: Set<String?>?): Set<String>? {
        return pref.getStringSet(key, defValues)
    }

    /**
     * Get int
     *
     * @param key
     * @param defValue
     * @return
     */
    fun getInt(key: String?, defValue: Int): Int {
        return pref.getInt(key, defValue)
    }

    /**
     * Get long
     *
     * @param key
     * @param defValue
     * @return
     */
    fun getLong(key: String?, defValue: Long): Long {
        return pref.getLong(key, defValue)
    }

    /**
     * Get float
     *
     * @param key
     * @param defValue
     * @return
     */
    fun getFloat(key: String?, defValue: Float): Float {
        return pref.getFloat(key, defValue)
    }

    /**
     * Get boolean
     *
     * @param key
     * @param defValue
     * @return
     */
    fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return pref.getBoolean(key, defValue)
    }

    /**
     * Contains
     *
     * @param key
     * @return
     */
    operator fun contains(key: String?): Boolean {
        return pref.contains(key)
    }

    /**
     * Editor
     */
    val editor: SharedPreferences.Editor
        get() = pref.edit()

    /**
     * Register on shared preference change listener
     *
     * @param listener
     */
    fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener?) {
        pref.registerOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Unregister on shared preference change listener
     *
     * @param listener
     */
    fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener?) {
        pref.unregisterOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Put string
     *
     * @param key
     * @param value
     * @return
     */
    fun putString(key: String?, value: String?): Boolean {
        return editor.putString(key, value).commit()
    }

    /**
     * Put string set
     *
     * @param key
     * @param values
     * @return
     */
    fun putStringSet(key: String?, values: Set<String?>?): Boolean {
        return editor.putStringSet(key, values).commit()
    }

    /**
     * Put int
     *
     * @param key
     * @param value
     * @return
     */
    fun putInt(key: String?, value: Int): Boolean {
        return editor.putInt(key, value).commit()
    }

    /**
     * Put long
     *
     * @param key
     * @param value
     * @return
     */
    fun putLong(key: String?, value: Long): Boolean {
        return editor.putLong(key, value).commit()
    }

    /**
     * Put float
     *
     * @param key
     * @param value
     * @return
     */
    fun putFloat(key: String?, value: Float): Boolean {
        return editor.putFloat(key, value).commit()
    }

    /**
     * Put boolean
     *
     * @param key
     * @param value
     * @return
     */
    fun putBoolean(key: String?, value: Boolean): Boolean {
        return editor.putBoolean(key, value).commit()
    }

    /**
     * Remove
     *
     * @param key
     * @return
     */
    fun remove(key: String?): Boolean {
        return editor.remove(key).commit()
    }

    /**
     * Clear
     *
     * @return
     */
    fun clear(): Boolean {
        return editor.clear().commit()
    }

    /**
     * Commit
     *
     * @return
     */
    fun commit(): Boolean {
        return editor.commit()
    }

    /**
     * Apply
     *
     */
    fun apply() {
        editor.apply()
    }
}