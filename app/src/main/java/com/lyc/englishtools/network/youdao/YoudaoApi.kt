package com.lyc.englishtools.network.youdao

import com.lyc.englishtools.model.youdao.YoudaoResponse
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface YoudaoApi {
    @POST("api")
    fun translateText(
        @Query("q") text: String,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("appKey") appKey: String,
        @Query("salt") salt: String,
        @Query("sign") sign: String,
        @Query("signType") signType: String,
        @Query("curtime") curtime: String
    ): Call<YoudaoResponse>
}