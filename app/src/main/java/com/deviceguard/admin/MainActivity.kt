package com.deviceguard.admin

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.view.Gravity
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Build UI safely in code to prevent layout inflation crashes
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setBackgroundColor(Color.parseColor("#1A1A1A")) // Dark background
        layout.gravity = Gravity.CENTER
        layout.setPadding(50, 50, 50, 50)

        val title = TextView(this)
        title.text = "AIRAVAT C2 CLIENT"
        title.textSize = 24f
        title.setTextColor(Color.GREEN)
        title.gravity = Gravity.CENTER

        val status = TextView(this)
        status.text = "Status: Payload Initialized Successfully."
        status.textSize = 16f
        status.setTextColor(Color.WHITE)
        status.gravity = Gravity.CENTER
        status.setPadding(0, 30, 0, 0)

        layout.addView(title)
        layout.addView(status)
        
        setContentView(layout)
        
        // Log to Android system so we know it launched
        Log.d("AIRAVAT_C2", "App launched successfully.")
    }
}
