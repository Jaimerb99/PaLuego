package com.example.paluego.view.activities

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.paluego.R
import com.example.paluego.databinding.ActivitySingleNoteBinding
import com.example.paluego.model.AppPreferences
import com.example.paluego.model.Constant
import com.example.paluego.model.Constant.AUDIO
import com.example.paluego.model.Constant.COLLECTION_NOTES
import com.example.paluego.model.Constant.COLLECTION_USER
import com.example.paluego.model.Constant.CONTENT
import com.example.paluego.model.Constant.DESCRIPTION
import com.example.paluego.model.Constant.EMAIL_KEY
import com.example.paluego.model.Constant.ID
import com.example.paluego.model.Constant.REQUEST_PERMISSION_RECORD_AUDIO
import com.example.paluego.model.Constant.TITLE
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.io.File

class SingleNoteActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySingleNoteBinding.inflate(layoutInflater)
    }

    private lateinit var handler: Handler
    private lateinit var autoSaveRunnable: Runnable
    private val db = FirebaseFirestore.getInstance()
    private lateinit var noteId: String

    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.editTextNoteTitle.setText(intent.getStringExtra(TITLE) ?: "")
        binding.editTextNoteContent.setText(intent.getStringExtra(DESCRIPTION) ?: "")

        noteId = giveId()

        // Check if an audio file exists for the current note
        val audioDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val audioFileName = "$noteId.mp3"
        val audioFile = File(audioDir, audioFileName)
        isRecording = false

        // Set the appropriate icon based on the recording state
        val iconResource = if (isRecording) R.drawable.ic_stop_recording else R.drawable.ic_voice_record_24
        binding.btnRecord.setImageResource(iconResource)

        handler = Handler()
        autoSaveRunnable = Runnable {
            saveChanges()
            handler.postDelayed(autoSaveRunnable, Constant.AUTOSAVE_INTERVAL)
        }

        binding.btnRecord.setOnClickListener {
            if (isRecording && audioFilePath != null) {
                stopRecording()
                binding.btnRecord.setImageResource(R.drawable.ic_voice_record_24) // Set the start recording icon
            } else {
                if (checkRecordAudioPermission()) {
                    // Hay que sustituirlo por el trigger que deje activar el grabado de nota despues de tocar una segunda vez
                    if(audioFilePath == null) binding.btnRecord.setImageResource(R.drawable.ic_stop_recording) // Set the stop recording icon
                    startRecording()
                } else {
                    requestRecordAudioPermission()
                }
            }
        }

        binding.btnBack.setOnClickListener {
            saveChanges()
            finish()
        }
    }


    override fun onResume() {
        super.onResume()
        binding.editTextNoteTitle.clearFocus()
        binding.editTextNoteContent.clearFocus()
        handler.postDelayed(autoSaveRunnable, Constant.AUTOSAVE_INTERVAL)
        saveChanges()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(autoSaveRunnable)
        saveChanges()
    }

    private fun saveChanges() {
        val title = binding.editTextNoteTitle.text.toString()
        val content = binding.editTextNoteContent.text.toString()

        val noteData = hashMapOf(
            COLLECTION_USER to AppPreferences.getString(baseContext, EMAIL_KEY, ""),
            ID to noteId,
            TITLE to title,
            CONTENT to content,
            AUDIO to audioFilePath
        )
        //To avoid saving blank notes
        if(title.isNotEmpty() && content.isNotEmpty()){
            db.collection(COLLECTION_NOTES).document(noteId)
                .set(noteData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("Firestore", "Datos de la nota guardados exitosamente")
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error al guardar los datos de la nota", exception)
                }
        }

    }

    private fun startRecording() {
        if (audioFilePath != null) {
            Toast.makeText(this, getString(R.string.file_already_exists), Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val audioDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            val audioFileName = "$noteId.mp3"
            val audioFile = File(audioDir, audioFileName)
            audioFilePath = audioFile.absolutePath

            mediaRecorder = MediaRecorder()
            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder?.setAudioEncodingBitRate(128000)
            mediaRecorder?.setAudioSamplingRate(44100)
            mediaRecorder?.setOutputFile(audioFilePath)
            mediaRecorder?.prepare()
            mediaRecorder?.start()

            isRecording = true
            Toast.makeText(this, getString(R.string.recording_started), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("SingleNoteActivity", "Error al iniciar la grabación", e)
        }
    }

    private fun stopRecording() {
        if (!isRecording) {
            Toast.makeText(this, getString(R.string.no_recording_in_progress), Toast.LENGTH_SHORT).show()
            return
        }

        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null

            isRecording = false
            Toast.makeText(this, getString(R.string.record_stopped), Toast.LENGTH_SHORT).show()

            if (audioFilePath != null) {
                saveChanges()
            }
        } catch (e: Exception) {
            Log.e("SingleNoteActivity", "Error al detener la grabación", e)
        }
    }

    private fun checkRecordAudioPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestRecordAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_PERMISSION_RECORD_AUDIO
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording()
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun giveId(): String {
        val intentId = intent.getStringExtra(ID)
        return if (intentId != null) {
            intentId
        } else {
            val collectionRef = db.collection(COLLECTION_NOTES)
            val documentRef = collectionRef.document()
            documentRef.id
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        saveChanges()
    }
}
