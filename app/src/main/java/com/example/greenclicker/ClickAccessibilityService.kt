package com.example.greenclicker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log

class ClickAccessibilityService : AccessibilityService() {

    companion object {
        var instance: ClickAccessibilityService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i("GreenClicker", "AccessibilityService connected")
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) { }

    override fun onInterrupt() { }

    fun performClickAt(x: Int, y: Int) {
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())
        val stroke = GestureDescription.StrokeDescription(path, 0, 80)
        val gd = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gd, object: GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                Log.i("GreenClicker", "Click at $x,$y completed")
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                Log.i("GreenClicker", "Click cancelled")
            }
        }, null)
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }
}
