package com.example.greenclicker

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.PixelFormat
import android.view.View
import android.view.WindowManager
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.max

class ROISelectionOverlay(private val ctx: Context, private val onComplete: (Int,Int,Int,Int)->Unit) {
    private val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: OverlayView? = null
    private val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    )

    fun show() {
        overlayView = OverlayView(ctx)
        wm.addView(overlayView, params)
    }

    fun remove() {
        try { if (overlayView?.parent != null) wm.removeView(overlayView) } catch (e: Exception) {}
        overlayView = null
    }

    inner class OverlayView(c: Context) : View(c) {
        private var startX = 0f
        private var startY = 0f
        private var curX = 0f
        private var curY = 0f
        private val paint = Paint().apply {
            color = Color.argb(120, 0, 200, 0)
            style = Paint.Style.FILL
        }
        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x; startY = event.y
                    curX = startX; curY = startY
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    curX = event.x; curY = event.y
                    invalidate()
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    val left = min(startX, curX).toInt()
                    val top = min(startY, curY).toInt()
                    val right = max(startX, curX).toInt()
                    val bottom = max(startY, curY).toInt()
                    onComplete(left, top, right, bottom)
                    remove()
                    return true
                }
            }
            return super.onTouchEvent(event)
        }
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (abs(curX - startX) > 2 || abs(curY - startY) > 2) {
                val left = min(startX, curX)
                val top = min(startY, curY)
                val right = max(startX, curX)
                val bottom = max(startY, curY)
                canvas.drawRect(left, top, right, bottom, paint)
            }
            invalidate()
        }
    }
}
