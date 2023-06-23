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
import androidx.core.net.toUri
import com.example.paluego.R
import com.example.paluego.databinding.ActivitySingleNoteBinding
import com.example.paluego.model.AppPreferences
import com.example.paluego.model.Constant
import com.example.paluego.model.Constant.COLLECTION_NOTES
import com.example.paluego.model.Constant.DESCRIPTION
import com.example.paluego.model.Constant.ID
import com.example.paluego.model.Constant.REQUEST_PERMISSION_RECORD_AUDIO
import com.example.paluego.model.Constant.TITLE
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class SingleNoteActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySingleNoteBinding.inflate(layoutInflater)
    }


    private lateinit var handler: Handler
    private lateinit var autoSaveRunnable: Runnable
    private val db = FirebaseFirestore.getInstance()
    private lateinit var noteId: String


    private lateinit var audioFileRef: StorageReference
    private val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.editTextNoteTitle.setText(intent.getStringExtra(TITLE) ?: "")
        binding.editTextNoteContent.setText(intent.getStringExtra(DESCRIPTION) ?: "")

        binding.btnRecord.setOnClickListener {
            if (isRecording) {
                stopRecording()
                isRecording = false
                saveChanges()
            } else {
                checkRecordAudioPermission()
                isRecording = true
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

        getAudioDownloadUrl()
    }


    override fun onResume() {
        super.onResume()
        binding.editTextNoteTitle.clearFocus()
        binding.editTextNoteContent.clearFocus()
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

        // Firebase storage reference
        val storageReference = FirebaseStorage.getInstance().reference

        // Verifica si hay un archivo de audio para guardar
        if (outputFile != null) {
            // Obtén la referencia del archivo de audio en Firestore
            val audioRef = storageReference.child("audio/$noteId.mp3")

            // uploads the file
            audioRef.putFile(outputFile!!.toUri())
                .addOnSuccessListener {
                    Log.d("Firestore", "Archivo de audio guardado exitosamente")

                    // Crea un mapa con los datos de la nota, incluyendo el enlace del archivo de audio
                    val noteData = hashMapOf(
                        "user" to AppPreferences.getString(baseContext, "email", ""),
                        "id" to noteId,
                        "title" to title,
                        "content" to content,
                        //"audio" to audioRef.toString()
                    )

                    // Actualiza los datos de la nota en Firestore
                    db.collection(COLLECTION_NOTES).document(noteId)
                        .set(noteData, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d("Firestore", "Datos de la nota guardados exitosamente")
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Firestore", "Error al guardar los datos de la nota", exception)
                        }
                }
                .addOnFailureListener { exception ->
                    // Ocurrió un error al guardar el archivo de audio en Firestore
                    Log.e("Firestore", "Error al guardar el archivo de audio", exception)
                }
        } else {
            // No hay un archivo de audio para guardar

            // Crea un mapa con los datos de la nota, asignando un valor nulo o una cadena vacía al campo de audio
            val noteData = hashMapOf(
                "user" to AppPreferences.getString(baseContext, "email", ""),
                "id" to noteId,
                "title" to title,
                "content" to content,
                "audio" to null
            )

            // Actualiza los datos de la nota en Firestore
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


    private fun getAudioDownloadUrl() {
        val storageReference = FirebaseStorage.getInstance().reference

        audioFileRef = storageReference.child("audio/$noteId.mp3")

        audioFileRef.downloadUrl
            .addOnSuccessListener { uri ->
                val audioUrl = uri.toString()
                binding.btnPlay.setImageResource(R.drawable.ic_play_button_24)
                binding.btnPlay.isEnabled = true
                Log.d("JRB", "Audio download URL: $audioUrl")

            }
            .addOnFailureListener { exception ->
                binding.btnPlay.setImageResource(R.drawable.ic_gray_play_button_24)
                binding.btnPlay.isEnabled = false
                Log.e("JRB", "Failed to retrieve audio download URL", exception)
            }
    }

    private fun checkRecordAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, RECORD_AUDIO_PERMISSION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(RECORD_AUDIO_PERMISSION),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        } else {
            startRecording()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startRecording() {
        val outputDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        outputFile = File.createTempFile("recording", ".mp3", outputDir)

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(44100)
            setAudioChannels(1)
            setAudioEncodingBitRate(192000)
            setOutputFile(outputFile?.absolutePath)
            prepare()
            start()
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }




    private fun giveId(): String {
        val intentId = intent.getStringExtra(ID)
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
    }


}