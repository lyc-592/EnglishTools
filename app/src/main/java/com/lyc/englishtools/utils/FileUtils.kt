package com.lyc.englishtools.utils

import android.content.Context
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object FileUtils {

    fun getFile(context: Context, fileName: String): File {
        return File(context.filesDir, fileName)
    }

    fun saveStreamToFile(context: Context, fileName: String, inputStream: InputStream) {
        val file = getFile(context, fileName)

        // 使用缓冲流提高性能
        try {
            inputStream.use { input ->
                val outputStream = file.outputStream()
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    fun deleteAvatarFile(context: Context, fileName: String) {
        val file = getFile(context, fileName)
        if (file.exists()) {
            file.delete()
        }
    }
}


