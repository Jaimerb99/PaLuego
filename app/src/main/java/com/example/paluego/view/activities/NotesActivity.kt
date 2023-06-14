package com.example.paluego.view.activities

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.paluego.R
import com.example.paluego.databinding.ActivityNotesBinding
import com.example.paluego.model.AppPreferences

class NotesActivity : AppCompatActivity() {

    private val binding by lazy{
        ActivityNotesBinding.inflate(layoutInflater)
    }

    private var backPressedOnce = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnLogOut.setOnClickListener {
            showExitConfirmationDialog()
        }

        binding.btnAddNote.setOnClickListener{
            //TODO
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this@NotesActivity, SettingsActivity::class.java))
        }
    }



    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Exit")
        builder.setMessage("Are you sure you want to exit?")
        builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
            AppPreferences.clearAllPreferences(this)
            finish()
            startActivity(Intent(this@NotesActivity, LoginActivity::class.java))
        }
        builder.setNegativeButton("No") { dialogInterface: DialogInterface, i: Int ->
            // Descartar el diálogo o realizar cualquier acción necesaria
            Toast.makeText(this, "You clicked No", Toast.LENGTH_SHORT).show()
        }
        builder.setCancelable(false)
        builder.show()
    }

    override fun onBackPressed() {
        if (backPressedOnce) {
            finish()
        } else {
            backPressedOnce = true
            Toast.makeText(this, "Press back again to exit the app", Toast.LENGTH_SHORT).show()

            // Reset backPressedOnce after a delay
            Handler().postDelayed({ backPressedOnce = false }, 2000)
        }
    }
}