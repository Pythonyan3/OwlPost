package com.example.owlpost

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
    }

    private fun initViews(){
        login_button.setOnClickListener{
            if (isValidEmail(user_email.text.toString())){

                startActivity(Intent(this, SendMail::class.java))
                this.finish()
            }
            else
                Toast.makeText(this@LoginActivity, "Адрес или пароль некорректны!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return if (isEmpty(email)) false else android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}