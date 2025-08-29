package com.lyc.englishtools.utils

import android.content.Context
import com.lyc.englishtools.ui.auth.AuthManager

object PrefUtils {
    private const val PREF_NAME = "user_prefs"

    fun getMyVocabulary(context: Context): Set<String> {
        val userId = AuthManager.getCurrentUsername() ?: return emptySet()
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet("vocabulary_$userId", emptySet()) ?: emptySet()
    }

    fun addToVocabulary(context: Context, wordId: Int) {
        val userId = AuthManager.getCurrentUsername() ?: return
        val vocabulary = getMyVocabulary(context).toMutableSet()
        vocabulary.add(wordId.toString())
        saveVocabulary(context, userId, vocabulary)
    }

    fun removeFromVocabulary(context: Context, wordId: Int) {
        val userId = AuthManager.getCurrentUsername() ?: return
        val vocabulary = getMyVocabulary(context).toMutableSet()
        vocabulary.remove(wordId.toString())
        saveVocabulary(context, userId, vocabulary)
    }

    fun clearVocabulary(context: Context) {
        val userId = AuthManager.getCurrentUsername() ?: return
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove("vocabulary_$userId").apply()
    }

    private fun saveVocabulary(context: Context, userId: String, vocabulary: Set<String>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet("vocabulary_$userId", vocabulary).apply()
    }
}