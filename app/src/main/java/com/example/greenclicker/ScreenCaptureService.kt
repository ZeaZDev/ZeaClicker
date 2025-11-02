package com.example.greenclicker

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import kotlin.concurrent.thread

class ScreenCaptureService : Service() {

    companion object {
        private var active = false
        fun setActive(v: Boolean) { active = v }
    }

    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = NotificationHelper.createNotificationChannel(this)
        val notif = NotificationCompat.Builder(this, channelId)
            .setContentTitle("GreenClicker - Capture")
            .setContentText("Capturing screen")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build()
        startForeground(1338, notif)

        val resultCode = intent?.getIntExtra("resultCode", -1) ?: -1
        val data = intent?.getParcelableExtra<Intent>("data")

        if (resultCode != -1 && data != null) {
            val mgr = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = mgr.getMediaProjection(resultCode, data)
            startCapture()
        }

        return START_STICKY
    }

    private fun startCapture() {
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val metrics = DisplayMetrics()
        display.getRealMetrics(metrics)
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        mediaProjection?.createVirtualDisplay(
            "capture",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader!!.surface, null, null
        )

        handlerThread = HandlerThread("CaptureThread")
        handlerThread!!.start()
        handler = Handler(handlerThread!!.looper)

        thread {
            while (true) {
                if (active) {
                    val image = imageReader?.acquireLatestImage()
                    if (image != null) {
                        val plane = image.planes[0]
                        val buffer = plane.buffer
                        val pixelStride = plane.pixelStride
                        val rowStride = plane.rowStride
                        val rowPadding = rowStride - pixelStride * width
                        val bmp = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
                        bmp.copyPixelsFromBuffer(buffer)
                        val realBmp = Bitmap.createBitmap(bmp, 0, 0, width, height)
                        image.close()

                        val prefs = getSharedPreferences("gc_prefs", MODE_PRIVATE)
                        val step = prefs.getInt("step", 6)
                        val hMin = prefs.getInt("hMin", 40)
                        val hMax = prefs.getInt("hMax", 90)
                        val sMin = prefs.getInt("sMin", 80)
                        val vMin = prefs.getInt("vMin", 80)
                        val roi = prefs.getString("roi", "0,0,0,0") ?: "0,0,0,0"
                        val scale = prefs.getInt("scale", 4).coerceAtLeast(1)

                        // downscale for processing
                        val procW = width / scale
                        val procH = height / scale
                        val small = Bitmap.createScaledBitmap(realBmp, procW, procH, true)

                        val pSmall = ImageProcessorHSV.findFirstGreenPixel(small, maxOf(1, step/scale), hMin, hMax, sMin, vMin, roi)
                        small.recycle()
                        realBmp.recycle()
                        bmp.recycle()

                        if (pSmall != null) {
                            // map back to full resolution
                            val mappedX = pSmall.first * scale
                            val mappedY = pSmall.second * scale
                            DebugOverlayManager.setPoint(mappedX, mappedY)
                            ClickAccessibilityService.instance?.performClickAt(mappedX, mappedY)
                            Thread.sleep(200)
                        } else {
                            Thread.sleep(60)
                        }
                    } else {
                        Thread.sleep(50)
                    }
                } else {
                    Thread.sleep(200)
                }
            }
        }
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        super.onDestroy()
        handlerThread?.quitSafely()
        mediaProjection?.stop()
    }
}
