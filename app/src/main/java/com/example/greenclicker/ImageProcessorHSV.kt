package com.example.greenclicker

import android.graphics.Bitmap
import kotlin.math.max
import kotlin.math.min

object ImageProcessorHSV {

    fun findFirstGreenPixel(bmp: Bitmap, step: Int, hMin: Int, hMax: Int, sMin: Int, vMin: Int, roiString: String?): Pair<Int, Int>? {
        val w = bmp.width
        val h = bmp.height

        var left = 0
        var top = 0
        var right = w - 1
        var bottom = h - 1

        if (!roiString.isNullOrBlank()) {
            try {
                val parts = roiString.split(",").map{ it.trim().toIntOrNull() ?: 0 }
                if (parts.size == 4) {
                    if (parts[2] > 0 && parts[3] > 0) {
                        left = parts[0].coerceIn(0, w-1)
                        top = parts[1].coerceIn(0, h-1)
                        right = parts[2].coerceIn(left, w-1)
                        bottom = parts[3].coerceIn(top, h-1)
                    }
                }
            } catch (e: Exception) { }
        }

        val hmin = hMin.coerceIn(0,179)
        val hmax = hMax.coerceIn(0,179)
        val smin = sMin.coerceIn(0,255)
        val vmin = vMin.coerceIn(0,255)

        val stepUse = if (step <= 0) 1 else step

        for (y in top..bottom step stepUse) {
            for (x in left..right step stepUse) {
                val px = bmp.getPixel(x, y)
                val r = (px shr 16) and 0xff
                val g = (px shr 8) and 0xff
                val b = px and 0xff
                val hsv = FloatArray(3)
                rgbToHsv(r, g, b, hsv)
                val H = hsv[0].toInt()
                val S = (hsv[1]*255).toInt()
                val V = (hsv[2]*255).toInt()
                if (inHueRange(H, hmin, hmax) && S >= smin && V >= vmin) {
                    val refined = refineCenter(bmp, x, y, 6, hmin, hmax, smin, vmin)
                    if (refined != null) return refined
                }
            }
        }
        return null
    }

    private fun inHueRange(h: Int, hmin: Int, hmax: Int): Boolean {
        return if (hmin <= hmax) {
            h in hmin..hmax
        } else {
            h >= hmin || h <= hmax
        }
    }

    private fun refineCenter(bmp: Bitmap, startX: Int, startY: Int, radius: Int, hMin: Int, hMax: Int, sMin: Int, vMin: Int): Pair<Int, Int>? {
        val w = bmp.width
        val h = bmp.height
        var sumX = 0
        var sumY = 0
        var count = 0
        val left = (startX - radius).coerceAtLeast(0)
        val right = (startX + radius).coerceAtMost(w - 1)
        val top = (startY - radius).coerceAtLeast(0)
        val bottom = (startY + radius).coerceAtMost(h - 1)
        for (yy in top..bottom) {
            for (xx in left..right) {
                val px = bmp.getPixel(xx, yy)
                val r = (px shr 16) and 0xff
                val g = (px shr 8) and 0xff
                val b = px and 0xff
                val hsv = FloatArray(3)
                rgbToHsv(r, g, b, hsv)
                val H = hsv[0].toInt()
                val S = (hsv[1]*255).toInt()
                val V = (hsv[2]*255).toInt()
                if (inHueRange(H, hMin, hMax) && S >= sMin && V >= vMin) {
                    sumX += xx
                    sumY += yy
                    count++
                }
            }
        }
        return if (count > 0) Pair(sumX / count, sumY / count) else null
    }

    private fun rgbToHsv(r: Int, g: Int, b: Int, hsv: FloatArray) {
        val rf = r / 255.0f
        val gf = g / 255.0f
        val bf = b / 255.0f
        val max = max(rf, max(gf, bf))
        val minv = min(rf, min(gf, bf))
        val v = max
        val delta = max - minv
        val s = if (max == 0f) 0f else delta / max
        var h = 0f
        if (delta != 0f) {
            if (max == rf) {
                h = ((gf - bf) / delta) % 6f
            } else if (max == gf) {
                h = ((bf - rf) / delta) + 2f
            } else {
                h = ((rf - gf) / delta) + 4f
            }
            h *= 60f
            if (h < 0) h += 360f
        }
        hsv[0] = (h / 2f)
        hsv[1] = s
        hsv[2] = v
    }
}
