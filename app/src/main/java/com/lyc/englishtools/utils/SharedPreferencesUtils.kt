package com.lyc.englishtools.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesUtils {
    private const val PREFS_NAME = "EnglishToolsPrefs"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveString(context: Context, key: String, value: String) {
        getSharedPreferences(context).edit().putString(key, value).apply()
    }

    fun getString(context: Context, key: String): String? {
        return getSharedPreferences(context).getString(key, null)
    }

    fun saveInt(context: Context, key: String, value: Int) {
        getSharedPreferences(context).edit().putInt(key, value).apply()
    }

    fun getInt(context: Context, key: String, defaultValue: Int = 0): Int {
        return getSharedPreferences(context).getInt(key, defaultValue)
    }

    fun saveBoolean(context: Context, key: String, value: Boolean) {
        getSharedPreferences(context).edit().putBoolean(key, value).apply()
    }

    fun getBoolean(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        return getSharedPreferences(context).getBoolean(key, defaultValue)
    }
}