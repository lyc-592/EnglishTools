package com.lyc.englishtools.utils

import com.lyc.englishtools.network.youdao.YoudaoApi
import com.lyc.englishtools.network.youdao.YoudaoOcrApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object RetrofitClient {
    private const val BASE_URL = "https://openapi.youdao.com/"
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val youdaoApi: YoudaoApi by lazy {
        retrofit.create(YoudaoApi::class.java)
    }

    val youdaoOcrApi: YoudaoOcrApi by lazy {
        retrofit.create(YoudaoOcrApi::class.java)
    }

    // 通用签名生成方法
    fun generateSign(appKey: String, input: String, salt: String, curtime: String, appSecret: String): String {
        val signStr = appKey + input + salt + curtime + appSecret
        return sha256(signStr)
    }

    private fun sha256(input: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(input.toByteArray(StandardCharsets.UTF_8))
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }

    // 生成输入值（图片翻译专用）
    fun generateInputForImage(base64: String): String {
        return if (base64.length > 20) {
            base64.substring(0, 10) + base64.length + base64.substring(base64.length - 10)
        } else {
            base64
        }
    }
}