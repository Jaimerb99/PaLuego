package com.example.paluego.view.activities


import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.paluego.R
import com.example.paluego.databinding.ActivityRegisterBinding
import com.example.paluego.model.AppPreferences
import com.example.paluego.model.Constant.BIRTH_DATE
import com.example.paluego.model.Constant.COLLECTION_USER
import com.example.paluego.model.Constant.EMAIL_KEY
import com.example.paluego.model.Constant.NAME
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)
    }

    private val db = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnSignUp.setOnClickListener {

            dismissKeyboard(it.windowToken)
            if( binding.editTextTextPersonName.text.toString().trim().isNotEmpty() && binding.editTextTextPersonName.text.toString().trim().isNotEmpty()){
                createUser()
            }else{
                if(binding.editTextTextPersonName.text.toString().isEmpty()) binding.editTextTextPersonName.error = getString(R.string.name_needed)
                if(binding.editTextTextPassword.text.toString().isNullOrEmpty()) binding.editTextTextPassword.error = getString(R.string.password_needed)
            }
        }

        binding.btnBack.setOnClickListener {finish()}

    }

    private fun dismissKeyboard(wToken: IBinder) {
       val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(wToken, 0)
    }

    private fun createUser() {
        val auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(binding.editTextTextEmailAddress.text.toString(), binding.editTextTextPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.successfully_registered), Toast.LENGTH_SHORT).show()
                    AppPreferences.setString(baseContext, "id", auth.uid!!)
                    saveUserOnFirebase()
                    finish()
                } else {
                    //Fields validation
                    if (task.exception is FirebaseAuthException) {
                        when ((task.exception as FirebaseAuthException).errorCode) {
                            "ERROR_WEAK_PASSWORD" -> {
                                binding.editTextTextPassword.error = getString(R.string.minimum_chars)
                            }
                            "ERROR_EMAIL_ALREADY_IN_USE" -> {
                                binding.editTextTextEmailAddress.error = getString(R.string.account_already_exists)
                            }
                            else -> {
                                showAlert()
                                Log.w("JRB", "createUserWithEmail:failure0", task.exception)
                            }
                        }
                    } else if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        showAlert()
                        Log.w("JRB", "createUserWithEmail:failure1", task.exception)
                    } else {
                        showAlert()
                        Log.w("JRB", "createUserWithEmail:failure2", task.exception)
                    }
                }
            }
    }

    private fun saveUserOnFirebase() {
        db.collection(COLLECTION_USER).document(binding.editTextTextEmailAddress.text.toString()).set(
            hashMapOf(NAME to binding.editTextTextPersonName.text.toString(),
                EMAIL_KEY to binding.editTextTextEmailAddress.text.toString(),
                BIRTH_DATE to binding.editTextDate.text.toString()
            )
        )
    }

    private fun showAlert() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.error))
            .setMessage(getString(R.string.auth_failed))
            .setPositiveButton(getString(R.string.accept), null)
            .show()
    }

    override fun onBackPressed() {
        finish()
    }
}