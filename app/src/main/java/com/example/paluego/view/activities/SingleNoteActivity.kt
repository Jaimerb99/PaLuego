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
        db.collection(COLLECTION_NOTES).document(noteId).set(
            hashMapOf("user" to AppPreferences.getString(baseContext, "email", ""),
            "title" to binding.editTextNoteTitle.text.toString(),
            "content" to binding.editTextNoteContent.text.toString(),
            //"audio" to outputFile
        ), SetOptions.merge()  //In every case data is not overwritten, is updated and merged
        ).addOnSuccessListener {
            Log.d("JRB", "Datos bien guardados")
        }.addOnFailureListener { Log.d("JRB", "Datos mal guardados") }
    }


 /*   private fun existingIndex(): Boolean {
        var exist = false
        db.collection(COLLECTION_NOTES).document(AppPreferences.getString(this, "email", "error")).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists() && documentSnapshot.data.isNullOrEmpty()) {
                    //The document is empty
                    exist = false
                    Log.d("JRB", "El documento está vacío")
                } else {
                    exist = true
                    // The document has fields
                    Log.d("JRB", "El documento contiene campos")
                }
            }
            .addOnFailureListener { exception ->
                //Probably the document doesn't exist
                exist = false
                Log.d("JRB", "Error al obtener el documento: ${exception.message}")
            }
        return exist
    }*/

    private fun giveId(): String {
        val collectionRef = db.collection("test") // Fake collection in order to take a unique id
        val documentRef = collectionRef.document()
        return  documentRef.id
    }


    /*private fun toggleRecording() {
        if (::mediaRecorder.isInitialized) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        outputFile =  File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "audio.3gp")

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(outputFile.absolutePath)

            try {
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder.apply {
            stop()
            reset()
            release()
        }

        saveAudioToFirebaseStorage()
        mediaRecorder = MediaRecorder()
    }

    private fun saveAudioToFirebaseStorage() {
        val storageRef = FirebaseStorage.getInstance().reference
        val audioRef = storageRef.child("audios/${outputFile.name}")

        audioRef.putFile(outputFile.toUri())
            .addOnSuccessListener {
                Log.d("JRB", "++++++++++++++++++++++++++++++++")
                outputFile.delete()
            }
            .addOnFailureListener {
                Log.d("JRB", "--------------------------------")
            }
    }
*/
    override fun onDestroy() {
        super.onDestroy()
        saveChanges()
        if (::mediaRecorder.isInitialized) {
            mediaRecorder.release()
        }
    }


}
