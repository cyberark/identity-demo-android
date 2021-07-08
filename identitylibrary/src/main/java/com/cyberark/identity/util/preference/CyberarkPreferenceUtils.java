package com.cyberark.identity.util.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;
import java.util.Set;

public class CyberarkPreferenceUtils {

    private static Context mContext;

    public static void init(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    private static SharedPreferences getPref() {
        if (mContext == null) {
            throw new IllegalStateException("Please init the Utils with an application context.");
        }
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public static Map<String, ?> getAll() {
        return getPref().getAll();
    }

    public static String getString(String key, @Nullable String defValue) {
        return getPref().getString(key, defValue);
    }

    public static Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        return getPref().getStringSet(key, defValues);
    }

    public static int getInt(String key, int defValue) {
        return getPref().getInt(key, defValue);
    }

    public static long getLong(String key, long defValue) {
        return getPref().getLong(key, defValue);
    }

    public static float getFloat(String key, float defValue) {
        return getPref().getFloat(key, defValue);
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return getPref().getBoolean(key, defValue);
    }

    public static boolean contains(String key) {
        return getPref().contains(key);
    }

    public static SharedPreferences.Editor getEditor() {
        return getPref().edit();
    }

    public static void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getPref().registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getPref().unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static boolean putString(String key, @Nullable String value) {
        return getEditor().putString(key, value).commit();
    }

    public static boolean putStringSet(String key, @Nullable Set<String> values) {
        return getEditor().putStringSet(key, values).commit();
    }

    public static boolean putInt(String key, int value) {
        return getEditor().putInt(key, value).commit();
    }

    public static boolean putLong(String key, long value) {
        return getEditor().putLong(key, value).commit();
    }

    public static boolean putFloat(String key, float value) {
        return getEditor().putFloat(key, value).commit();
    }

    public static boolean putBoolean(String key, boolean value) {
        return getEditor().putBoolean(key, value).commit();
    }

    public static boolean remove(String key) {
        return getEditor().remove(key).commit();
    }

    public static boolean clear() {
        return getEditor().clear().commit();
    }

    public static boolean commit() {
        return getEditor().commit();
    }

    public static void apply() {
        getEditor().apply();
    }
}

