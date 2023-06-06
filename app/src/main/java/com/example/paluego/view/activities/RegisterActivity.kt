package com.example.paluego.view.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.paluego.R
import com.example.paluego.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnSignUp.setOnClickListener {
            //TODO Check the fields and do the registry
        }

    }
}