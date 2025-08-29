package com.lyc.englishtools

import android.app.Application
import android.content.Context
import android.util.Log
import com.lyc.englishtools.ui.auth.AuthManager
import com.lyc.englishtools.data.WordDatabase
import com.lyc.englishtools.data.entities.WordEntity
import com.lyc.englishtools.utils.PrefUtils
import com.opencsv.CSVReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class EnglishToolsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AuthManager.init(this)
        initWordDatabase(this)
    }

    private fun initWordDatabase(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("EnglishToolsApp", "开始初始化单词数据库...")

            try {
                val database = WordDatabase.getDatabase(context)
                val wordDao = database.wordDao()

                // 获取偏好设置
                val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val isInitializedKey = "database_initialized"
                val initializedBefore = sharedPrefs.getBoolean(isInitializedKey, false)

                if (initializedBefore) {
                    Log.d("EnglishToolsApp", "数据库已初始化过，跳过导入")
                    return@launch
                }

                // 检查当前数据是否只有默认单词
                val existingWords = wordDao.getAllWords()
                if (existingWords.isNotEmpty() &&
                    existingWords.size == 3 &&
                    existingWords.any { it.english == "apple" }) {
                    Log.d("EnglishToolsApp", "发现默认单词，准备清空数据库")

                    // 清空现有数据（单词和书签）
                    wordDao.deleteAllWords()

                    // 清除书签表
                    wordDao.deleteAllBookmarks()

                    // 清除单词本
                    PrefUtils.clearVocabulary(context)
                }

                // 尝试可能的文件名格式 - 确保使用大写的 .CSV 扩展名
                val possibleFileNames = listOf(
                    "words.CSV",  // 首选大写扩展名
                    "Words.CSV",  // 混合大小写
                    "WORDS.CSV",  // 全大写
                    "words.csv",  // 小写扩展名（备份）
                    "Words.csv",  // 混合大小写备份
                    "WORDS.csv",  // 全大写备份
                    "word_list.CSV" // 备选文件名
                )

                var csvInputStream: InputStream? = null
                var foundFileName: String? = null

                for (fileName in possibleFileNames) {
                    try {
                        csvInputStream = context.assets.open(fileName)
                        foundFileName = fileName
                        Log.d("EnglishToolsApp", "成功打开文件: $fileName")
                        break
                    } catch (e: Exception) {
                        Log.w("EnglishToolsApp", "无法打开文件: $fileName - ${e.message}")
                    }
                }

                if (csvInputStream == null) {
                    Log.e("EnglishToolsApp", "所有文件名尝试失败")
                    throw Exception("无法找到单词文件")
                }

                // 使用 OpenCSV 解析 CSV
                val reader = BufferedReader(InputStreamReader(csvInputStream, "UTF-8"))
                val csvReader = CSVReader(reader)
                val words = mutableListOf<WordEntity>()

                // 读取所有行并跳过第一行（标题行）
                var lineNumber = 0
                var nextLine: Array<String>? = csvReader.readNext()

                while (nextLine != null) {
                    // 跳过标题行 (lineNumber = 0)
                    if (lineNumber == 0) {
                        Log.d("EnglishToolsApp", "跳过标题行: ${nextLine.joinToString(",")}")
                        lineNumber++
                        nextLine = csvReader.readNext()
                        continue
                    }

                    // 确保至少有三列：序号,单词,释义
                    if (nextLine.size >= 3) {
                        val id = lineNumber - 1  // 因为我们已经跳过了标题行
                        val english = nextLine[1].trim()
                        val chinese = nextLine[2].trim()

                        // 跳过空行
                        if (english.isNotBlank() && chinese.isNotBlank()) {
                            words.add(WordEntity(id, english, chinese))
                        } else {
                            Log.w("EnglishToolsApp", "跳过空行: ${nextLine.joinToString(",")}")
                        }
                    } else {
                        Log.w("EnglishToolsApp", "跳过格式错误的行: ${nextLine.joinToString(",")}")
                    }

                    lineNumber++
                    nextLine = csvReader.readNext()
                }

                csvReader.close()

                if (words.isEmpty()) {
                    Log.e("EnglishToolsApp", "CSV文件解析后无有效数据")
                    throw Exception("解析后无有效单词数据")
                }

                Log.d("EnglishToolsApp", "从CSV读取到 ${words.size} 个单词")

                // 插入数据库
                wordDao.insertAll(words)
                Log.d("EnglishToolsApp", "成功导入 ${words.size} 个单词到数据库")

                // 标记为已初始化
                sharedPrefs.edit().putBoolean(isInitializedKey, true).apply()
                Log.d("EnglishToolsApp", "数据库初始化标记已设置")

            } catch (e: Exception) {
                Log.e("EnglishToolsApp", "导入CSV失败: ${e.message}", e)

                // 创建默认单词作为回退
                createDefaultWords(context)
            }
        }
    }

    private fun createDefaultWords(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = WordDatabase.getDatabase(context)
                val words = listOf(
                    WordEntity(0, "apple", "苹果"),
                    WordEntity(1, "banana", "香蕉"),
                    WordEntity(2, "computer", "电脑")
                )
                database.wordDao().insertAll(words)
                Log.d("EnglishToolsApp", "创建了默认单词列表")
            } catch (e: Exception) {
                Log.e("EnglishToolsApp", "创建默认单词失败: ${e.message}")
            }
        }
    }
}