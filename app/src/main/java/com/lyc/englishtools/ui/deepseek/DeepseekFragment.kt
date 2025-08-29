package com.lyc.englishtools.ui.deepseek

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lyc.englishtools.databinding.FragmentDeepseekBinding
import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class DeepseekFragment : Fragment() {

    private var _binding: FragmentDeepseekBinding? = null
    private val binding get() = _binding!!

    // 使用ViewModel保持状态
    private val viewModel: DeepseekViewModel by viewModels {
        DeepseekViewModelFactory(requireActivity().application)
    }

    // UI 控件
    private lateinit var etUserInput: EditText
    private lateinit var tvContent: TextView
    private lateinit var btnSend: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeepseekBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化UI控件
        etUserInput = binding.etUserInput
        tvContent = binding.tvContent
        btnSend = binding.btnSend
        progressBar = binding.progressBar

        // 恢复状态
        etUserInput.setText(viewModel.userInput)
        tvContent.text = viewModel.aiResponse

        // 如果之前有请求正在进行，显示进度条
        if (viewModel.isLoading) {
            progressBar.visibility = View.VISIBLE
            btnSend.isEnabled = false
        }

        // 设置发送按钮点击事件
        btnSend.setOnClickListener {
            onSendClicked()
        }

        // 监听输入变化，保存状态
        etUserInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.userInput = s.toString()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // 当返回到此Fragment时，检查结果是否已经完成
        if (viewModel.isResponseReady) {
            tvContent.text = viewModel.aiResponse
        }
    }

    private fun onSendClicked() {
        val content = etUserInput.text.toString().trim()
        if (content.isEmpty()) {
            Toast.makeText(requireContext(), "请输入问题", Toast.LENGTH_SHORT).show()
            return
        }

        // 显示加载状态
        progressBar.visibility = View.VISIBLE
        btnSend.isEnabled = false
        tvContent.text = "思考中..."

        // 保存当前用户输入
        viewModel.userInput = content

        // 启动请求
        viewModel.sendRequest(content, object : DeepseekCallback {
            @SuppressLint("SetTextI18n")
            override fun onSuccess(response: String) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnSend.isEnabled = true
                    tvContent.text = "问: $content\n\n答: $response"
                    // 清空输入框
                    etUserInput.text.clear()
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onFailure(error: String) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnSend.isEnabled = true
                    tvContent.text = "请求失败: $error"
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// 回调接口
interface DeepseekCallback {
    fun onSuccess(response: String)
    fun onFailure(error: String)
}

// ViewModel 保留状态
class DeepseekViewModel : ViewModel() {
    var userInput: String = ""
    var aiResponse: String = "等待提问..."
    var isLoading: Boolean = false
    var isResponseReady: Boolean = false

    private val apiKey = "sk-54ac76a7b9bd445490c07a6a6f595bd3"
    private val apiUrl = "https://api.deepseek.com/chat/completions"
    private val mediaType = "application/json".toMediaType()

    // 用于后台处理
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun sendRequest(content: String, callback: DeepseekCallback) {
        isLoading = true
        isResponseReady = false

        scope.launch {
            try {
                // 构建请求JSON
                val jsonPayload = buildRequestJson(content)
                Log.d("DeepSeek", "Request JSON: $jsonPayload")

                // 创建请求
                val request = Request.Builder()
                    .url(apiUrl)
                    .post(RequestBody.create(mediaType, jsonPayload))
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .build()

                // 发送请求并获取响应
                val client = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(50, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build()

                val response = client.newCall(request).execute()

                // 检查响应是否成功
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "未知错误"
                    throw IOException("API请求失败: ${response.code} - $errorBody")
                }

                // 解析响应内容
                val responseBody = response.body?.string() ?: throw IOException("空响应")
                val result = parseApiResponse(responseBody)

                // 保存结果
                aiResponse = "问: $content\n\n答: $result"
                isResponseReady = true

                // 回调成功
                callback.onSuccess(result)
            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is IOException -> "网络错误: ${e.message}"
                    else -> "请求失败: ${e.message}"
                }
                aiResponse = errorMsg
                callback.onFailure(errorMsg)
            } finally {
                isLoading = false
            }
        }
    }

    private fun buildRequestJson(content: String): String {
        return JsonObject().apply {
            // 创建消息数组
            val messagesArray = JsonArray()
            messagesArray.add(JsonObject().apply {
                addProperty("role", "user")
                addProperty("content", content)
            })

            // 添加所有属性
            add("messages", messagesArray)
            addProperty("model", "deepseek-chat")
            addProperty("frequency_penalty", 0)
            addProperty("max_tokens", 2048)
            addProperty("presence_penalty", 0)

            // 添加响应格式
            add("response_format", JsonObject().apply {
                addProperty("type", "text")
            })

            // 其他固定属性
            add("stop", JsonNull.INSTANCE)
            addProperty("stream", false)
            add("stream_options", JsonNull.INSTANCE)
            addProperty("temperature", 1)
            addProperty("top_p", 1)
            add("tools", JsonNull.INSTANCE)
            addProperty("tool_choice", "none")
            addProperty("logprobs", false)
            add("top_logprobs", JsonNull.INSTANCE)
        }.toString()
    }

    private fun parseApiResponse(jsonString: String): String {
        try {
            // 解析JSON
            val jsonElement = com.google.gson.JsonParser.parseString(jsonString)

            // 检查错误响应
            if (jsonElement.isJsonObject) {
                val jsonObject = jsonElement.asJsonObject

                // 如果有错误字段
                if (jsonObject.has("error")) {
                    val error = jsonObject.getAsJsonObject("error")
                    val message = error.get("message")?.asString ?: "未知错误"
                    val type = error.get("type")?.asString ?: "API错误"
                    return "$type: $message"
                }

                // 获取响应内容
                val choices = jsonObject.getAsJsonArray("choices") ?: throw Exception("无choices字段")
                if (choices.size() == 0) throw Exception("choices为空")

                val firstChoice = choices[0].asJsonObject
                val message = firstChoice.getAsJsonObject("message") ?: throw Exception("无message字段")
                val content = message.get("content")?.asString ?: throw Exception("无content字段")

                return content
            }
        } catch (e: Exception) {
            return "解析响应失败: ${e.message}"
        }

        return "无效的API响应"
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}

// ViewModel 工厂类
class DeepseekViewModelFactory(private val application: android.app.Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeepseekViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeepseekViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}