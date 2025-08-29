package com.lyc.englishtools.model.youdao

data class OcrTranslationResponse(
    val orientation: String?,
    val lanFrom: String?,
    val textAngle: String?,
    val errorCode: String,
    val lanTo: String?,
    val resRegions: List<OcrTranslationRegion>?
)

data class OcrTranslationRegion(
    val boundingBox: String?,
    val linesCount: Int,
    val lineheight: Int,
    val context: String,
    val linespace: Int,
    val tranContent: String
)