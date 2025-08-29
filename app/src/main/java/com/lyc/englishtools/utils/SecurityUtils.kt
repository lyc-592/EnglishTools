package com.lyc.englishtools.utils

import java.security.MessageDigest
import java.security.SecureRandom

class SecurityUtils {

    // 生成16字节随机盐值
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return bytesToHex(salt)
    }

    // SHA-256加盐哈希
    fun hashWithSalt(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val saltedPassword = "$password$salt".toByteArray()
        val hashBytes = digest.digest(saltedPassword)
        return bytesToHex(hashBytes)
    }

    // 字节数组转十六进制字符串
    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }
}