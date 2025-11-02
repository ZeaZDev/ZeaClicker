package com.example.greenclicker

import android.content.Context
import android.graphics.*
import android.view.PixelFormat
import android.view.View
import android.view.WindowManager
import android.widget.TextView

object DebugOverlayManager {
    private var windowManager: WindowManager? = null
    private var circleView: CircleView? = null
    private var messageView: TextView? = null
    private var params: WindowManager.LayoutParams? = null
    private var visible = false

    fun init(ctx: Context) {
        if (windowManager != null) return
        windowManager = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        circleView = CircleView(ctx)
        messageView = TextView(ctx)
        messageView!!.setBackgroundColor(Color.argb(180, 0, 0, 0))
        messageView!!.setTextColor(Color.WHITE)
        messageView!!.textSize = 12f
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        messageView!!.setPadding(8,8,8,8)
    }

    fun setVisible(ctx: Context, v: Boolean) {
        visible = v
        try {
            if (v) {
                windowManager?.addView(circleView, params)
                windowManager?.addView(messageView, params)
            } else {
                if (circleView?.parent != null) windowManager?.removeView(circleView)
                if (messageView?.parent != null) windowManager?.removeView(messageView)
            }
        } catch (e: Exception) {}
    }

    fun setPoint(x: Int, y: Int) {
        if (!visible) return
        circleView?.setPoint(x, y)
    }

    fun showMessage(ctx: Context, text: String) {
        try {
            messageView?.text = text
        } catch (e: Exception) {}
    }

    fun release() {
        try {
            if (circleView?.parent != null) windowManager?.removeView(circleView)
            if (messageView?.parent != null) windowManager?.removeView(messageView)
        } catch (e: Exception) {}
        windowManager = null
        circleView = null
        messageView = null
    }

    private class CircleView(ctx: Context) : View(ctx) {
        private var px = -1f
        private var py = -1f
        private val paint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = Color.GREEN
            isAntiAlias = true
        }
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (px >= 0 && py >= 0) {
                canvas.drawCircle(px, py, 40f, paint)
            }
            invalidate()
        }
        fun setPoint(x: Int, y: Int) {
            px = x.toFloat()
            py = y.toFloat()
        }
    }
}
