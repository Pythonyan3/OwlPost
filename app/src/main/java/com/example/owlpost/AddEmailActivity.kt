package com.example.owlpost

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.owlpost.models.Settings
import com.example.owlpost.models.SettingsException
import com.example.owlpost.models.User
import com.example.owlpost.ui.shortToast
import kotlinx.android.synthetic.main.activity_login.*

class AddEmailActivity : AppCompatActivity() {
    private lateinit var setting: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initFields()
        initViews()
    }

    private fun initFields() {
        setting = Settings(this)
    }

    private fun initViews(){
        login_button.setOnClickListener{
            val email = user_email.text.toString()
            val password = user_password.text.toString()

            if (email.isEmpty() || password.isEmpty())
                shortToast(getString(R.string.empty_fields))
            else if (!isValidEmail(email)){
                shortToast(getString(R.string.incorrect_email))
            }
            else {
                try {
                    setting.addUser(User(email, password))
                    setResult(RESULT_OK)
                    this.finish()
                }
                catch (e: SettingsException) {
                    shortToast(getString(R.string.email_already_exists))
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}