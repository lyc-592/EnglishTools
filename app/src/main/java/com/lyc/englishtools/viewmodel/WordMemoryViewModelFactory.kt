package com.lyc.englishtools.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lyc.englishtools.ui.study.memory.WordMemoryViewModel

class WordMemoryViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordMemoryViewModel::class.java)) {
            return WordMemoryViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}