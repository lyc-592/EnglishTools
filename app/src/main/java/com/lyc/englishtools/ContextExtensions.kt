package com.lyc.englishtools

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.lyc.englishtools.viewmodel.ViewModelFactory

fun Context.getApp(): EnglishToolsApp = applicationContext as EnglishToolsApp

fun <T : ViewModel> ViewModelStoreOwner.getViewModel(
    clazz: Class<T>,
    application: Application
): T {
    return ViewModelProvider(this, ViewModelFactory(application)).get(clazz)
}