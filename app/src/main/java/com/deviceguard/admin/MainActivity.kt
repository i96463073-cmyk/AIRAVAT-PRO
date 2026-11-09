package com.deviceguard.admin

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var commandListener: ListenerRegistration? = null
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }

        val title = TextView(this).apply {
            text = "DeviceGuard Client"
            textSize = 24f
            setPadding(0, 0, 0, 30)
        }

        val emailInput = EditText(this).apply { hint = "Client Email" }
        val passInput = EditText(this).apply { 
            hint = "Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD 
        }
        val loginBtn = Button(this).apply { text = "Sign In & Register Device" }
        
        statusText = TextView(this).apply {
            text = "Status: Not Signed In"
            textSize = 16f
            setPadding(0, 30, 0, 0)
        }

        layout.addView(title)
        layout.addView(emailInput)
        layout.addView(passInput)
        layout.addView(loginBtn)
        layout.addView(statusText)

        setContentView(layout)

        loginBtn.setOnClickListener {
            val email = emailInput.text.toString()
            val pass = passInput.text.toString()
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                loginAndRegister(email, pass)
            } else {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginAndRegister(email: String, pass: String) {
        statusText.text = "Status: Signing in..."
        CoroutineScope(Dispatchers.Main).launch {
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                val user = auth.currentUser
                if (user != null) {
                    statusText.text = "Status: Signed in as ${user.email}\nListening for commands..."
                    registerDevice(user.uid)
                    listenForCommands(user.uid)
                }
            } catch (e: Exception) {
                statusText.text = "Status: Error - ${e.message}"
            }
        }
    }

    private fun registerDevice(uid: String) {
        val deviceData = hashMapOf(
            "manufacturer" to Build.MANUFACTURER,
            "model" to Build.MODEL,
            "androidVersion" to Build.VERSION.RELEASE,
            "securityPatch" to Build.VERSION.SECURITY_PATCH,
            "connectionState" to "Online",
            "lastSeen" to System.currentTimeMillis()
        )
        db.collection("devices").document(uid).set(deviceData)
    }

    private fun listenForCommands(uid: String) {
        commandListener?.remove()
        commandListener = db.collection("devices").document(uid).collection("commands")
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val docId = change.document.id
                        val command = change.document.getString("command") ?: return@forEach
                        
                        showConsentDialog(uid, docId, command)
                    }
                }
            }
    }

    private fun showConsentDialog(uid: String, docId: String, command: String) {
        AlertDialog.Builder(this)
            .setTitle("Admin Request")
            .setMessage("The administrator is requesting: $command\n\nDo you approve?")
            .setPositiveButton("Approve") { _, _ ->
                executeCommand(uid, docId, command)
            }
            .setNegativeButton("Deny") { _, _ ->
                denyCommand(uid, docId)
            }
            .setCancelable(false)
            .show()
    }

    private fun executeCommand(uid: String, docId: String, command: String) {
        val result = when (command) {
            "DEVICE_INFO" -> {
                "Manufacturer: ${Build.MANUFACTURER}\nModel: ${Build.MODEL}\nAndroid: ${Build.VERSION.RELEASE}"
            }
            "STORAGE_REPORT" -> {
                val stat = StatFs(Environment.getDataDirectory().path)
                val freeBytes = stat.availableBlocksLong * stat.blockSizeLong
                val totalBytes = stat.blockCountLong * stat.blockSizeLong
                "Free: ${freeBytes / 1024 / 1024} MB\nTotal: ${totalBytes / 1024 / 1024} MB"
            }
            "SECURITY_POSTURE" -> {
                "Developer Options: ${if (Build.TYPE == "user") "Disabled" else "Enabled"}\nPatch Level: ${Build.VERSION.SECURITY_PATCH}"
            }
            else -> "Unknown Command"
        }

        val update = hashMapOf(
            "status" to "APPROVED",
            "result" to result,
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("devices").document(uid).collection("commands").document(docId).update(update)
        Toast.makeText(this, "Report sent to Admin", Toast.LENGTH_SHORT).show()
    }

    private fun denyCommand(uid: String, docId: String) {
        val update = hashMapOf(
            "status" to "DENIED",
            "result" to "User denied the request.",
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("devices").document(uid).collection("commands").document(docId).update(update)
        Toast.makeText(this, "Request denied", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        commandListener?.remove()
    }
}
