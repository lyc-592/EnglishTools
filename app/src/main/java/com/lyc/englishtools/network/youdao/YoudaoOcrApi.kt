package com.lyc.englishtools.network.youdao

import com.lyc.englishtools.model.youdao.OcrTranslationResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface YoudaoOcrApi {
    @Multipart
    @POST("ocrtransapi")
    fun ocrTranslate(
        @Part("type") type: RequestBody,
        @Part("q") q: RequestBody, // Base64 encoded image
        @Part("from") from: RequestBody,
        @Part("to") to: RequestBody,
        @Part("appKey") appKey: RequestBody,
        @Part("salt") salt: RequestBody,
        @Part("sign") sign: RequestBody,
        @Part("signType") signType: RequestBody,
        @Part("curtime") curtime: RequestBody,
        @Part("docType") docType: RequestBody = RequestBody.create(null, "json"),
        @Part("render") render: RequestBody = RequestBody.create(null, "0")
    ): Call<OcrTranslationResponse>
}