package com.lyc.englishtools.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lyc.englishtools.ui.study.vocabulary.MyVocabularyViewModel

class MyVocabularyViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyVocabularyViewModel::class.java)) {
            return MyVocabularyViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}