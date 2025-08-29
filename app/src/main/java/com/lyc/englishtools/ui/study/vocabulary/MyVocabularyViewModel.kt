package com.lyc.englishtools.ui.study.vocabulary

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lyc.englishtools.data.WordDatabase
import com.lyc.englishtools.data.entities.WordEntity
import com.lyc.englishtools.utils.PrefUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyVocabularyViewModel(application: Application) : AndroidViewModel(application) {

    private val _vocabularyWords = MutableLiveData<List<WordEntity>>()
    val vocabularyWords: LiveData<List<WordEntity>> get() = _vocabularyWords

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // 保存当前加载的单词列表
    private var currentWords = listOf<WordEntity>()

    fun loadVocabulary(context: Context) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 获取用户单词本中的单词ID
                val wordIds = PrefUtils.getMyVocabulary(context).mapNotNull { it.toIntOrNull() }

                if (wordIds.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        _vocabularyWords.value = emptyList()
                        currentWords = emptyList()
                        _isLoading.value = false
                    }
                    return@launch
                }

                // 从数据库加载单词
                val database = WordDatabase.getDatabase(context)
                val words = database.wordDao().getWordsByIds(wordIds)

                withContext(Dispatchers.Main) {
                    _vocabularyWords.value = words
                    currentWords = words  // 保存当前单词列表
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _errorMessage.value = "加载单词本失败: ${e.message}"
                }
            }
        }
    }

    fun removeWord(context: Context, wordId: Int) {
        // 立即从当前列表中移除单词（UI更新）
        val newWords = currentWords.filter { it.id != wordId }
        _vocabularyWords.value = newWords
        currentWords = newWords

        // 从SharedPreferences中移除
        PrefUtils.removeFromVocabulary(context, wordId)

        // 在后台更新数据源
        viewModelScope.launch(Dispatchers.IO) {
            // 如果列表为空，清除数据
            if (newWords.isEmpty()) {
                PrefUtils.clearVocabulary(context)
            }
        }
    }
}