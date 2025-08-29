package com.lyc.englishtools.ui.study.memory

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lyc.englishtools.ui.auth.AuthManager
import com.lyc.englishtools.data.WordDatabase
import com.lyc.englishtools.data.entities.Bookmark
import com.lyc.englishtools.data.entities.WordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WordMemoryViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentWord = MutableLiveData<WordEntity?>()
    val currentWord: LiveData<WordEntity?> get() = _currentWord

    private val _showChinese = MutableLiveData(false)
    val showChinese: LiveData<Boolean> get() = _showChinese

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private var words = listOf<WordEntity>()
    private var currentPosition = 0

    private lateinit var userId: String

    fun loadWords(context: Context) {
        _isLoading.value = true
        _errorMessage.value = null

        userId = AuthManager.getCurrentUsername() ?: "default"

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 从数据库加载所有单词
                val database = WordDatabase.getDatabase(context)
                words = database.wordDao().getAllWords()

                if (words.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        _isLoading.value = false
                        _errorMessage.value = "数据库中没有单词，请导入单词数据"
                    }
                    return@launch
                }

                // 加载书签
                val bookmark = database.wordDao().getBookmark(userId)
                currentPosition = bookmark?.position ?: 0

                // 检查位置是否有效
                if (currentPosition < 0) currentPosition = 0
                if (currentPosition >= words.size) currentPosition = words.size - 1

                // 更新UI
                withContext(Dispatchers.Main) {
                    _currentWord.value = words[currentPosition]
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _errorMessage.value = "加载单词失败: ${e.message}"
                }
            }
        }
    }

    fun toggleChinese() {
        _showChinese.value = !(_showChinese.value ?: false)
    }

    fun prevWord() {
        if (currentPosition > 0) {
            currentPosition--
            _currentWord.value = words[currentPosition]
            _showChinese.value = false
        }
    }

    fun nextWord() {
        if (currentPosition < words.size - 1) {
            currentPosition++
            _currentWord.value = words[currentPosition]
            _showChinese.value = false
        }
    }

    fun saveBookmark(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val database = WordDatabase.getDatabase(context)
                database.wordDao().insertBookmark(Bookmark(userId, currentPosition))
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
}