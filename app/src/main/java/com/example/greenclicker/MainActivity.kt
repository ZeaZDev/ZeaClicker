package com.example.greenclicker

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQ_MEDIA_PROJ = 1001
    }

    private lateinit var mpm: MediaProjectionManager
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mpm = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val btnReq = findViewById<Button>(R.id.btnRequestProjection)
        val btnStart = findViewById<Button>(R.id.btnStartOverlay)
        tvStatus = findViewById(R.id.tvStatus)

        // settings fields
        val etHueMin = findViewById<EditText>(R.id.etHueMin)
        val etHueMax = findViewById<EditText>(R.id.etHueMax)
        val etSatMin = findViewById<EditText>(R.id.etSatMin)
        val etValMin = findViewById<EditText>(R.id.etValMin)
        val etStep = findViewById<EditText>(R.id.etStep)
        val etScale = findViewById<EditText>(R.id.etScale)
        val etROI = findViewById<EditText>(R.id.etROI)
        val btnSave = findViewById<Button>(R.id.btnSaveSettings)
        val btnSelectROI = findViewById<Button>(R.id.btnSelectROI)
        val btnToggleDebug = findViewById<Button>(R.id.btnToggleDebug)

        val prefs = getSharedPreferences("gc_prefs", MODE_PRIVATE)
        etHueMin.setText(prefs.getInt("hMin", 40).toString())
        etHueMax.setText(prefs.getInt("hMax", 90).toString())
        etSatMin.setText(prefs.getInt("sMin", 80).toString())
        etValMin.setText(prefs.getInt("vMin", 80).toString())
        etStep.setText(prefs.getInt("step", 6).toString())
        etScale.setText(prefs.getInt("scale", 4).toString())
        etROI.setText(prefs.getString("roi", "0,0,0,0"))

        btnSave.setOnClickListener {
            val hMin = etHueMin.text.toString().toIntOrNull() ?: 40
            val hMax = etHueMax.text.toString().toIntOrNull() ?: 90
            val sMin = etSatMin.text.toString().toIntOrNull() ?: 80
            val vMin = etValMin.text.toString().toIntOrNull() ?: 80
            val step = etStep.text.toString().toIntOrNull() ?: 6
            val scale = etScale.text.toString().toIntOrNull() ?: 4
            val roi = etROI.text.toString()
            prefs.edit()
                .putInt("hMin", hMin)
                .putInt("hMax", hMax)
                .putInt("sMin", sMin)
                .putInt("vMin", vMin)
                .putInt("step", step)
                .putInt("scale", scale)
                .putString("roi", roi)
                .apply()
            tvStatus.text = "Settings saved"
        }

        btnReq.setOnClickListener {
            startActivityForResult(mpm.createScreenCaptureIntent(), REQ_MEDIA_PROJ)
        }

        btnStart.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                tvStatus.text = "Status: Please grant overlay permission"
                return@setOnClickListener
            }
            tvStatus.text = "Status: Please enable Accessibility service for GreenClicker in Settings -> Accessibility"
            val intent = Intent(this, OverlayService::class.java)
            startService(intent)
            tvStatus.text = "Status: Overlay service started. Grant media projection first (tap Request Screen Capture Permission)."
        }

        btnSelectROI.setOnClickListener {
            // Start ROI overlay selection in OverlayService
            OverlayService.requestROISelection(this)
            tvStatus.text = "Tap and drag on screen to select ROI"
        }

        btnToggleDebug.setOnClickListener {
            val cur = prefs.getBoolean("debug", false)
            prefs.edit().putBoolean("debug", !cur).apply()
            tvStatus.text = "Debug overlay: " + (!cur).toString()
            DebugOverlayManager.setVisible(this, !cur)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_MEDIA_PROJ) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val intent = Intent(this, ScreenCaptureService::class.java)
                intent.putExtra("resultCode", resultCode)
                intent.putExtra("data", data)
                startService(intent)
                tvStatus.text = "Status: ScreenCaptureService started"
            } else {
                tvStatus.text = "Status: MediaProjection permission denied"
            }
        }
    }
}
