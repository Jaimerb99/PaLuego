package com.example.paluego.view.activities

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.paluego.databinding.ActivitySingleNoteBinding
import com.example.paluego.model.AppPreferences
import com.example.paluego.model.Constant
import com.example.paluego.model.Constant.COLLECTION_NOTE
import com.example.paluego.model.Constant.COLLECTION_NOTES
import com.example.paluego.model.Constant.REQUEST_PERMISSION_RECORD_AUDIO
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException

class SingleNoteActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySingleNoteBinding.inflate(layoutInflater)
    }


    private lateinit var handler: Handler
    private lateinit var autoSaveRunnable: Runnable
    private val db = FirebaseFirestore.getInstance()
    private lateinit var noteId: String

    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var outputFile: File
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.editTextNoteTitle.setText(intent.getStringExtra("title") ?: "")
        binding.editTextNoteContent.setText(intent.getStringExtra("description") ?: "")

        binding.btnRecord.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_PERMISSION_RECORD_AUDIO
                )
            } else {
                //  toggleRecording()
            }
        }

        noteId = giveId()

        handler = Handler()
        autoSaveRunnable = Runnable {
            saveChanges()
            handler.postDelayed(autoSaveRunnable, Constant.AUTOSAVE_INTERVAL)
        }

        binding.btnBack.setOnClickListener{
            saveChanges()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Inits the count again
        handler.postDelayed(autoSaveRunnable, Constant.AUTOSAVE_INTERVAL)
        saveChanges()
    }

    override fun onPause() {
        super.onPause()
        // Pause the callback when the activity is on resume
        handler.removeCallbacks(autoSaveRunnable)
        saveChanges()
    }

    private fun saveChanges() {
        val title = binding.editTextNoteTitle.text.toString()
        val content = binding.editTextNoteContent.text.toString()
        //TODO implentar audios
        if (title.isNotEmpty() || content.isNotEmpty()) {
            db.collection(COLLECTION_NOTES).document(noteId).set(
                hashMapOf(
                    "user" to AppPreferences.getString(baseContext, "email", ""),
                    "id" to noteId,
                    "title" to binding.editTextNoteTitle.text.toString(),
                    "content" to binding.editTextNoteContent.text.toString(),
                    //"audio" to outputFile
                ),
                SetOptions.merge()  //In every case data is not overwritten, is updated and merged
            ).addOnSuccessListener {
                Log.d("JRB", "Datos bien guardados")
            }.addOnFailureListener { Log.d("JRB", "Datos mal guardados") }
        }
    }


    private fun giveId(): String {
        val intentId = intent.getStringExtra("id")
        return if (intentId != null) {
            // Only if the note exist
            intentId
        } else {
            // Si no hay un extra "id" en el intent, genera un nuevo ID
            val collectionRef = db.collection(COLLECTION_NOTES)
            val documentRef = collectionRef.document()
            documentRef.id
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        saveChanges()
        if (::mediaRecorder.isInitialized) {
            mediaRecorder.release()
        }
    }


}