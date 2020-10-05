package com.example.owlpost

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.Toast
import androidx.core.text.toHtml
import com.example.owlpost.models.Settings
import com.example.owlpost.models.User
import kotlinx.android.synthetic.main.activity_send_mail.*

class SendMail : AppCompatActivity() {
    private val settings = Settings()
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_mail)

        initVariables()
        initViewsListeners()
    }

    private fun initVariables() {
        settings.init(this)
        currentUser = settings.getCurrentUser()
    }

    private fun initViewsListeners(){
        back_button.setOnClickListener {
            this.finish()
        }

        showFormatPanel.setOnClickListener {
            bottom_panel.visibility = View.VISIBLE
        }

        attach_button.setOnClickListener {
            Toast.makeText(this, "ATTACH FILE", Toast.LENGTH_SHORT).show()
        }

        send_button.setOnClickListener {
            Toast.makeText(this, "SENDING MAIL", Toast.LENGTH_SHORT).show()
        }

        close_btn.setOnClickListener {
            bottom_panel.visibility = View.GONE
        }
    }
}