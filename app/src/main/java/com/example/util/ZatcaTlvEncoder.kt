package com.example.util

import android.util.Base64
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.Locale

object ZatcaTlvEncoder {

    private fun getTlvField(tag: Int, value: String): ByteArray {
        val valueBytes = value.toByteArray(StandardCharsets.UTF_8)
        val bos = ByteArrayOutputStream()
        bos.write(tag)
        bos.write(valueBytes.size)
        bos.write(valueBytes)
        return bos.toByteArray()
    }

    fun generateTlvBase64(
        sellerName: String,
        vatNumber: String,
        timestampIso: String,
        totalWithVat: Double,
        vatTotal: Double
    ): String {
        return try {
            val totalStr = String.format(Locale.US, "%.2f", totalWithVat)
            val vatStr = String.format(Locale.US, "%.2f", vatTotal)

            val bos = ByteArrayOutputStream()
            bos.write(getTlvField(1, sellerName))
            bos.write(getTlvField(2, vatNumber))
            bos.write(getTlvField(3, timestampIso))
            bos.write(getTlvField(4, totalStr))
            bos.write(getTlvField(5, vatStr))

            Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            ""
        }
    }
}
