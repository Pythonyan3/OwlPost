package com.example.owlpost

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.text.TextUtils.isEmpty
import com.example.owlpost.models.Settings

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Handler().postDelayed({
            routeToAppropriatePage()
        }, 1000)
    }

    private fun routeToAppropriatePage() {
        val settings = Settings()
        settings.init(this)
        if (settings.isSetCurrentUser()){
            startActivity(Intent(this, LoginActivity::class.java))
        }else{
            startActivity(Intent(this, SendMail::class.java))
        }
        finish()
    }
}