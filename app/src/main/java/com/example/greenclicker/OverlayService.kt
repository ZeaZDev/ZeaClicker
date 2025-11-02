package com.example.greenclicker

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.ImageButton
import androidx.core.app.NotificationCompat

class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var running = false

    companion object {
        private var instance: OverlayService? = null
        fun requestROISelection(ctx: Context) {
            instance?.startROISelection()
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        startForegroundIfNeeded()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        addFloatingButton()
        DebugOverlayManager.init(this)
    }

    private fun startForegroundIfNeeded() {
        val channelId = NotificationHelper.createNotificationChannel(this)
        val notif = NotificationCompat.Builder(this, channelId)
            .setContentTitle("GreenClicker")
            .setContentText("Overlay running")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()
        startForeground(1337, notif)
    }

    private fun addFloatingButton() {
        floatingView = layoutInflater.inflate(R.layout.float_button_layout, null)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 300

        windowManager?.addView(floatingView, params)

        val btn = floatingView!!.findViewById<ImageButton>(R.id.btnFloat)
        btn.setOnClickListener {
            running = !running
            if (running) {
                btn.setImageResource(android.R.drawable.ic_media_pause)
                ScreenCaptureService.setActive(true)
            } else {
                btn.setImageResource(android.R.drawable.ic_media_play)
                ScreenCaptureService.setActive(false)
            }
        }
    }

    fun startROISelection() {
        // add a full-screen overlay that captures touch to select rectangle
        val selector = ROISelectionOverlay(this) { left, top, right, bottom ->
            val prefs = getSharedPreferences("gc_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("roi", "$left,$top,$right,$bottom").apply()
            DebugOverlayManager.showMessage(this, "ROI saved: $left,$top,$right,$bottom")
        }
        selector.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        try { windowManager?.removeView(floatingView) } catch (e: Exception) {}
        DebugOverlayManager.release()
        instance = null
    }

    override fun onBind(intent: Intent?) = null
}
