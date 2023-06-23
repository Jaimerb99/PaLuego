package com.example.paluego.view.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.paluego.R
import com.example.paluego.model.AppPreferences
import com.example.paluego.model.Constant.ID
import com.example.paluego.model.Constant.SPLASH_SLEEPING_TIME
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val auth = FirebaseAuth.getInstance()


        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO) {
                Thread.sleep(SPLASH_SLEEPING_TIME)

                //Only for testing (removes preferences and disable autologin)
                //AppPreferences.clearAllPreferences(this@SplashActivity)

                val userId = AppPreferences.getString(baseContext, ID, "")

                //Autologin
                if(AppPreferences.hasPreferences(baseContext) && userId == auth.currentUser!!.uid){
                    startActivity(Intent(this@SplashActivity, NotesActivity::class.java))
                }else{
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                }
                //must be here to control the back button and not showing it again
                finish()
            }
        }

    }
}