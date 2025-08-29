package com.lyc.englishtools.ui.auth

import android.content.Context
import androidx.core.content.edit

object AuthManager {

    private const val PREFS_AUTH = "auth_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USERNAME = "username"

    // 当前登录状态（内存缓存）
    private var currentUsername: String? = null
    private var isLoggedIn = false

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_AUTH, Context.MODE_PRIVATE)
        isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        currentUsername = prefs.getString(KEY_USERNAME, null)
    }

    fun isLoggedIn(): Boolean = isLoggedIn

    fun getCurrentUsername(): String? = currentUsername

    fun login(context: Context, username: String) {
        isLoggedIn = true
        currentUsername = username

        // 持久化到 SharedPreferences
        val prefs = context.getSharedPreferences(PREFS_AUTH, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USERNAME, username)
        }
    }

    fun logout(context: Context) {
        isLoggedIn = false
        currentUsername = null

        // 清除持久化状态
        val prefs = context.getSharedPreferences(PREFS_AUTH, Context.MODE_PRIVATE)
        prefs.edit {
            remove(KEY_IS_LOGGED_IN)
            remove(KEY_USERNAME)
        }
    }
}