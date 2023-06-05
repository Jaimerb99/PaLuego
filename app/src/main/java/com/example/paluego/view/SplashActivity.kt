package com.example.paluego.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.paluego.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //TODO Autologin logic and waiting time if its necessary

        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO) {
                Thread.sleep(3000)
                val intento = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intento)

                //must be here to control the back button and not showing it again
                finish()
            }
        }

    }
}