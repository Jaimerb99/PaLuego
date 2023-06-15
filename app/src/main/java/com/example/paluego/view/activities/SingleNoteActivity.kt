package com.example.paluego.view.activities

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.paluego.databinding.ActivitySingleNoteBinding
import com.example.paluego.model.AppPreferences
import com.example.paluego.model.Constant
import com.example.paluego.model.Constant.REQUEST_PERMISSION_RECORD_AUDIO
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class SingleNoteActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySingleNoteBinding.inflate(layoutInflater)
    }


    private lateinit var handler: Handler
    private lateinit var autoSaveRunnable: Runnable
    private val db = FirebaseFirestore.getInstance()

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
//                toggleRecording()
            }
        }

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
    }

    override fun onPause() {
        super.onPause()
        // Pause the callback when the activity is on resume
        handler.removeCallbacks(autoSaveRunnable)
    }

    private fun saveChanges() {
        db.collection(Constant.COLLECTION_USER).document(AppPreferences.getString(this, "id", "")).set{

        }
    }

    /*private fun toggleRecording() {
        if (::mediaRecorder.isInitialized) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        outputFile =
            File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "audio.3gp")

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

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaRecorder.isInitialized) {
            mediaRecorder.release()
        }
    }*/


}
