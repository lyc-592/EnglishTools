package com.lyc.englishtools.ui.youdao

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lyc.englishtools.R
import com.lyc.englishtools.databinding.FragmentImageTranslationBinding
import com.lyc.englishtools.model.youdao.OcrTranslationResponse
import com.lyc.englishtools.network.youdao.YoudaoOcrApi
import com.lyc.englishtools.utils.RetrofitClient
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*

class ImageTranslationFragment : Fragment() {

    private var _binding: FragmentImageTranslationBinding? = null
    private val binding get() = _binding!!

    private var fromLang = "auto"
    private var toLang = "zh-CHS"

    private val appKey = "73dc135642f6a0e1"
    private val appSecret = "Jxuz7Njt9tVoh2qMnNw7YJnsaZngi9CY"

    private var selectedImageUri: Uri? = null
    private var imageBase64: String? = null

    // 图片选择器回调
    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                imageBase64 = getBase64FromUri(uri)
                binding.ivPreview.setImageURI(uri)
                binding.btnTranslate.isEnabled = true
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageTranslationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置返回按钮
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 设置语言选择器
        setupLanguageSpinner()

        // 设置选择图片按钮
        binding.btnSelectImage.setOnClickListener {
            openImagePicker()
        }

        // 设置翻译按钮
        binding.btnTranslate.setOnClickListener {
            imageBase64?.let { base64 ->
                if (base64.length > 0) {
                    translateImage(base64)
                } else {
                    Toast.makeText(requireContext(), "请先选择图片", Toast.LENGTH_SHORT).show()
                }
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

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png", "image/bmp"))
        }
        getContent.launch(intent)
    }

    private fun getBase64FromUri(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = requireActivity().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // 压缩图片到合适大小 (最大2MB)
            val compressed = compressBitmap(bitmap, 80, 2000) // 80%质量，最大2000KB

            val byteArrayOutputStream = ByteArrayOutputStream()
            compressed?.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val imageBytes = byteArrayOutputStream.toByteArray()

            Base64.encodeToString(imageBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("ImageTranslation", "Error getting image", e)
            null
        }
    }

    private fun compressBitmap(bitmap: Bitmap, quality: Int, maxSizeKb: Int): Bitmap {
        var currentQuality = quality
        val stream = ByteArrayOutputStream()

        do {
            stream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, currentQuality, stream)
            currentQuality -= 10
        } while (stream.size() > maxSizeKb * 1024 && currentQuality > 10)

        return BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
    }

    private fun translateImage(imageBase64: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvResult.text = ""

        val salt = UUID.randomUUID().toString()
        val curtime = (System.currentTimeMillis() / 1000).toString()

        // 根据API要求生成input值
        val input = RetrofitClient.generateInputForImage(imageBase64)
        val sign = RetrofitClient.generateSign(appKey, input, salt, curtime, appSecret)

        // 创建请求体
        val requestBodyMap = mapOf(
            "type" to RequestBody.create(null, "1"),
            "q" to RequestBody.create(null, imageBase64),
            "from" to RequestBody.create(null, fromLang),
            "to" to RequestBody.create(null, toLang),
            "appKey" to RequestBody.create(null, appKey),
            "salt" to RequestBody.create(null, salt),
            "sign" to RequestBody.create(null, sign),
            "signType" to RequestBody.create(null, "v3"),
            "curtime" to RequestBody.create(null, curtime),
            "docType" to RequestBody.create(null, "json"),
            "render" to RequestBody.create(null, "0")
        )

        // 创建Multipart请求
        val request = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .apply {
                requestBodyMap.forEach { (key, value) ->
                    addFormDataPart(key, null, value)
                }
            }
            .build()

        // 发送请求
        RetrofitClient.youdaoOcrApi.ocrTranslate(
            type = requestBodyMap["type"]!!,
            q = requestBodyMap["q"]!!,
            from = requestBodyMap["from"]!!,
            to = requestBodyMap["to"]!!,
            appKey = requestBodyMap["appKey"]!!,
            salt = requestBodyMap["salt"]!!,
            sign = requestBodyMap["sign"]!!,
            signType = requestBodyMap["signType"]!!,
            curtime = requestBodyMap["curtime"]!!
        ).enqueue(object : Callback<OcrTranslationResponse> {
            override fun onResponse(call: Call<OcrTranslationResponse>, response: Response<OcrTranslationResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    handleTranslationResult(response.body()!!)
                } else {
                    binding.tvResult.text = "翻译失败: 状态码 ${response.code()}"
                }
            }

            override fun onFailure(call: Call<OcrTranslationResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                binding.tvResult.text = "网络错误: ${t.message}"
                Log.e("ImageTranslation", "API call failed", t)
            }
        })
    }

    private fun handleTranslationResult(response: OcrTranslationResponse) {
        if (response.errorCode == "0") {
            val result = StringBuilder()
            result.append("源语言: ${response.lanFrom}\n")
            result.append("目标语言: ${response.lanTo}\n\n")

            response.resRegions?.forEachIndexed { index, region ->
                result.append("区域 ${index + 1}:\n")
                result.append("原文: ${region.context}\n")
                result.append("翻译: ${region.tranContent}\n\n")
            }

            binding.tvResult.text = result.toString()
        } else {
            binding.tvResult.text = "翻译错误: 错误码 ${response.errorCode}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}