package com.example.paluego.view.activities

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.paluego.databinding.ActivitySingleNoteBinding
import com.example.paluego.model.Constant.REQUEST_PERMISSION_RECORD_AUDIO
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException

class SingleNoteActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySingleNoteBinding.inflate(layoutInflater)
    }

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
                toggleRecording()
            }
        }
    }

    private fun toggleRecording() {
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
                outputFile.delete() // Elimina el archivo local después de subirlo a Firebase Storage
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
    }


}
