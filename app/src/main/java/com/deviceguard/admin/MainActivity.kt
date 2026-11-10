package com.deviceguard.admin

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create a safe UI programmatically to prevent crashes
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 50, 50, 50)

        val title = TextView(this)
        title.text = "AIRAVAT C2 PAYLOAD"
        title.textSize = 24f
        
        val status = TextView(this)
        status.text = "Status: Initializing..."
        status.textSize = 16f

        layout.addView(title)
        layout.addView(status)
        
        // Set the layout as the screen
        setContentView(layout)

        // Try to connect to Firebase safely
        try {
            val db = FirebaseFirestore.getInstance()
            
            // Test connection by reading a document
            db.collection("victims").document("test_device").get()
                .addOnSuccessListener { document ->
                    status.text = "Status: Connected to Firebase!"
                    Log.d("AIRAVAT_C2", "Successfully connected to Firestore")
                }
                .addOnFailureListener { exception ->
                    status.text = "Status: Firebase Error\n${exception.message}"
                    Log.e("AIRAVAT_C2", "Firebase error", exception)
                }
                
        } catch (e: Exception) {
            status.text = "Status: Firebase Init Failed\n${e.message}"
            Log.e("AIRAVAT_C2", "Exception", e)
        }
    }
}
