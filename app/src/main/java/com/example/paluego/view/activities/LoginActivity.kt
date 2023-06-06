package com.example.paluego.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.paluego.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnLogIn.setOnClickListener {
            if( binding.editTextTextPersonNameL.text.toString().trim().isNotEmpty() && binding.editTextTextPersonNameL.text.toString().trim().isNotEmpty()){
                //TODO AUTH
            }else{
                //TODO to be revised, password textfield doesnt work as a normal textfield
                if (binding.editTextTextPersonNameL.text.toString().isEmpty()) binding.editTextTextPersonNameL.error = "You must provide your name"
                if(binding.editTextTextPasswordL.text.toString().isNullOrEmpty()) binding.editTextTextPasswordL.error = "The password is needed to continue"
            }
        }

        binding.signUpText.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }
    }
}