package com.edutechconnect.app

import android.os.Bundle
import android.app.AlertDialog
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val status = findViewById<TextView>(R.id.status)
        val user = auth.currentUser

        if (user == null) {
            status.text = "Not signed in. Connect Firebase and create an account to begin."
        } else {
            status.text = "Signed in as ${user.email ?: user.uid}"
        }

        findViewById<Button>(R.id.subjectsButton).setOnClickListener {
            showSubjects()
        }
        findViewById<Button>(R.id.chatButton).setOnClickListener {
            showChat()
        }
        findViewById<Button>(R.id.adminButton).setOnClickListener {
            showAdmin()
        }
        findViewById<Button>(R.id.profileButton).setOnClickListener {
            showProfile()
        }
    }

    private fun showSubjects() {
        val subjects = listOf(
            "Technical Drawing", "Mathematics", "English Language",
            "Physics", "Chemistry", "Biology", "Economics",
            "Government", "Geography"
        )
        AlertDialog.Builder(this)
            .setTitle("Subjects")
            .setItems(subjects.toTypedArray()) { _, which ->
                val subject = subjects[which]
                db.collection("questions")
                    .whereEqualTo("subject", subject)
                    .get()
                    .addOnSuccessListener { snap ->
                        val text = if (snap.isEmpty) "No questions have been published yet." else
                            snap.documents.joinToString("\n\n") {
                                "Q: ${it.getString("question") ?: ""}\nA: ${it.getString("answer") ?: "Answer pending"}"
                            }
                        AlertDialog.Builder(this).setTitle(subject).setMessage(text)
                            .setPositiveButton("Close", null).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Unable to load questions: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }.show()
    }

    private fun showChat() {
        val input = EditText(this)
        input.hint = "Type a message"
        AlertDialog.Builder(this)
            .setTitle("Real-Time Chat")
            .setMessage("Messages are stored in Firestore. Add a proper chat screen in the next module for private and group conversations.")
            .setView(input)
            .setPositiveButton("Send") { _, _ ->
                val user = auth.currentUser
                if (user == null) {
                    Toast.makeText(this, "Sign in first.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                db.collection("messages").add(
                    mapOf(
                        "senderId" to user.uid,
                        "senderName" to (user.email ?: "Student"),
                        "text" to input.text.toString(),
                        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                )
            }.setNegativeButton("Close", null).show()
    }

    private fun showAdmin() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Admin must sign in first.", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection("admins").document(user.uid).get().addOnSuccessListener {
            if (!it.exists()) {
                Toast.makeText(this, "This account is not an authorized admin.", Toast.LENGTH_LONG).show()
                return@addOnSuccessListener
            }
            val view = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 8, 32, 8)
            }
            val subject = EditText(this); subject.hint = "Subject"
            val question = EditText(this); question.hint = "Question"
            val answer = EditText(this); answer.hint = "Answer / Solution"
            view.addView(subject); view.addView(question); view.addView(answer)
            AlertDialog.Builder(this).setTitle("Publish Question & Answer").setView(view)
                .setPositiveButton("Publish") { _, _ ->
                    db.collection("questions").add(
                        mapOf(
                            "subject" to subject.text.toString().trim(),
                            "question" to question.text.toString().trim(),
                            "answer" to answer.text.toString().trim(),
                            "createdBy" to user.uid,
                            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                        )
                    )
                }.setNegativeButton("Cancel", null).show()
        }
    }

    private fun showProfile() {
        val user = auth.currentUser
        AlertDialog.Builder(this)
            .setTitle("My Profile")
            .setMessage(if (user == null) "Not signed in." else "Email: ${user.email}\nUser ID: ${user.uid}")
            .setPositiveButton("Close", null).show()
    }
}
