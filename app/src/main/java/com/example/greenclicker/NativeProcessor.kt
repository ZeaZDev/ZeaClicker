package com.example.greenclicker

import android.graphics.Bitmap
import java.nio.ByteBuffer

object NativeProcessor {
    init {
        System.loadLibrary("native-lib")
    }

    external fun findGreen(rgba: ByteArray, width: Int, height: Int, step: Int, hMin: Int, hMax: Int, sMin: Int, vMin: Int): IntArray

    // helper to convert bitmap to RGBA bytearray
    fun bitmapToRGBA(bmp: Bitmap): ByteArray {
        val w = bmp.width
        val h = bmp.height
        val buf = ByteBuffer.allocate(w * h * 4)
        val pixels = IntArray(w * h)
        bmp.getPixels(pixels, 0, w, 0, 0, w, h)
        for (i in 0 until w*h) {
            val p = pixels[i]
            buf.put((p shr 16 and 0xFF).toByte())
            buf.put((p shr 8 and 0xFF).toByte())
            buf.put((p and 0xFF).toByte())
            buf.put((p shr 24 and 0xFF).toByte())
        }
        return buf.array()
    }
}
