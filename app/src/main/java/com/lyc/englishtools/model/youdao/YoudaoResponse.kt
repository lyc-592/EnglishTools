package com.lyc.englishtools.model.youdao

import com.google.gson.annotations.SerializedName

data class YoudaoResponse(
    val errorCode: String,
    val query: String? = null,
    val translation: List<String>? = null,
    val basic: Basic? = null,
    val web: List<Web>? = null
)

data class Basic(
    val phonetic: String? = null,
    @SerializedName("uk-phonetic") val ukPhonetic: String? = null,
    @SerializedName("us-phonetic") val usPhonetic: String? = null,
    val explains: List<String>? = null
)

data class Web(
    val key: String? = null,
    val value: List<String>? = null
)