package com.lyc.englishtools.ui.youdao

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lyc.englishtools.R
import com.lyc.englishtools.databinding.FragmentTextTranslationBinding
import com.lyc.englishtools.model.youdao.YoudaoResponse
import com.lyc.englishtools.network.youdao.YoudaoApi
import com.lyc.englishtools.utils.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TextTranslationFragment : Fragment() {

    private var _binding: FragmentTextTranslationBinding? = null
    private val binding get() = _binding!!

    private var fromLang = "auto"
    private var toLang = "zh-CHS"

    private val appKey = "73dc135642f6a0e1"
    private val appSecret = "Jxuz7Njt9tVoh2qMnNw7YJnsaZngi9CY"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTextTranslationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置返回按钮点击事件
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 设置语言选择器
        setupLanguageSpinner()

        // 设置翻译按钮
        binding.btnTranslate.setOnClickListener {
            val text = binding.etInput.text.toString().trim()
            if (text.isNotEmpty()) {
                translateText(text)
            } else {
                Toast.makeText(requireContext(), R.string.input_hint, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupLanguageSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.language_options,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerLanguage.adapter = adapter

        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> { // 自动检测
                        fromLang = "auto"
                        toLang = "zh-CHS"
                    }
                    1 -> { // 英文 → 中文
                        fromLang = "en"
                        toLang = "zh-CHS"
                    }
                    2 -> { // 中文 → 英文
                        fromLang = "zh-CHS"
                        toLang = "en"
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun translateText(text: String) {
        binding.tvResult.text = getString(R.string.translating)

        val salt = System.currentTimeMillis().toString()
        val curtime = (System.currentTimeMillis() / 1000).toString()
        val sign = RetrofitClient.generateSign(appKey, text, salt, curtime, appSecret)

        RetrofitClient.youdaoApi.translateText(
            text = text,
            from = fromLang,
            to = toLang,
            appKey = appKey,
            salt = salt,
            sign = sign,
            signType = "v3",
            curtime = curtime
        ).enqueue(object : Callback<YoudaoResponse> {
            override fun onResponse(call: Call<YoudaoResponse>, response: Response<YoudaoResponse>) {
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    handleTranslationResult(body)
                } else {
                    binding.tvResult.text = getString(R.string.translation_error, response.code().toString())
                }
            }

            override fun onFailure(call: Call<YoudaoResponse>, t: Throwable) {
                binding.tvResult.text = getString(R.string.network_error)
            }
        })
    }

    private fun handleTranslationResult(response: YoudaoResponse) {
        val result = StringBuilder()

        if (response.errorCode == "0") {
            // 主要翻译结果
            response.translation?.let { translations ->
                result.append("翻译结果:\n")
                translations.forEach { result.append("• $it\n") }
            }

            // 基本释义
            response.basic?.explains?.let { explains ->
                result.append("\n基本释义:\n")
                explains.forEach { result.append("• $it\n") }
            }

            // 网络释义
            response.web?.let { webList ->
                result.append("\n网络释义:\n")
                webList.forEach { web ->
                    web.key?.let { key ->
                        web.value?.let { values ->
                            result.append("$key: ${values.joinToString()}\n")
                        }
                    }
                }
            }
        } else {
            result.append(getString(R.string.translation_error, response.errorCode))
        }

        binding.tvResult.text = result.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}