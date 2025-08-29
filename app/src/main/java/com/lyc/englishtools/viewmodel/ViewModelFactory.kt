package com.lyc.englishtools.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lyc.englishtools.ui.study.memory.WordMemoryViewModel
import com.lyc.englishtools.ui.study.vocabulary.MyVocabularyViewModel

class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordMemoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WordMemoryViewModel(application) as T
        }
        if (modelClass.isAssignableFrom(MyVocabularyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyVocabularyViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}