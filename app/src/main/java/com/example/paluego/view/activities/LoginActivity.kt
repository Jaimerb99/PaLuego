package com.example.paluego.view.activities


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.paluego.R
import com.example.paluego.databinding.ActivityLoginBinding
import com.example.paluego.model.AppPreferences
import com.example.paluego.model.Constant.EMAIL_KEY
import com.example.paluego.model.Constant.ID
import com.google.firebase.auth.FirebaseAuth



class LoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnLogIn.setOnClickListener {
            if( binding.editTextTextPersonNameL.text.toString().trim().isNotEmpty() && binding.editTextTextPasswordL.text.toString().trim().isNotEmpty()  ){
                login()
            }else{
                if (binding.editTextTextPersonNameL.text.toString().isEmpty()) binding.editTextTextPersonNameL.error = getString(R.string.required_name)
                if(binding.editTextTextPasswordL.text.toString().isEmpty()) binding.editTextTextPasswordL.error = getString(R.string.needed_password)
            }
        }

        binding.signUpText.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }
    }

    private fun login() {

        val auth = FirebaseAuth.getInstance()
        binding.progressBar.visibility = View.VISIBLE
        auth.signInWithEmailAndPassword(binding.editTextTextPersonNameL.text.toString(), binding.editTextTextPasswordL.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("JRB", "signInWithEmail:success")
                    val user = auth.currentUser
                    AppPreferences.setString(baseContext, ID, user!!.uid)
                    AppPreferences.setString(baseContext, EMAIL_KEY, binding.editTextTextPersonNameL.text.toString())
                    startActivity(Intent(this@LoginActivity, NotesActivity::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("JRB", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext,getString(R.string.authentication_fail), Toast.LENGTH_SHORT).show()
                }
                binding.progressBar.visibility = View.GONE
            }


    }

}