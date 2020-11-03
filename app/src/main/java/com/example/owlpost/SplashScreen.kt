package com.example.owlpost

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.owlpost.models.Settings
import com.example.owlpost.models.SettingsException

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Handler(Looper.getMainLooper()).postDelayed({
                routeToAppropriatePage()
        }, 1000)
    }

    private fun routeToAppropriatePage() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}