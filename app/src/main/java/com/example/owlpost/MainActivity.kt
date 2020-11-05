package com.example.owlpost

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.example.owlpost.databinding.ActivityMainBinding
import com.example.owlpost.fragments.MailboxFragment
import com.example.owlpost.models.Settings
import com.example.owlpost.models.SettingsException
import com.example.owlpost.models.User
import com.example.owlpost.ui.ADD_EMAIL_REQUEST_CODE
import com.example.owlpost.ui.MailDrawer
import com.example.owlpost.ui.SEND_EMAIL_REQUEST_CODE
import com.example.owlpost.ui.shortToast
import com.google.android.material.internal.ContextUtils.getActivity


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawer: MailDrawer
    private lateinit var toolbar: Toolbar
    private lateinit var settings: Settings
    lateinit var activeUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initFields()
        initViews()
    }

    override fun onStart() {
        super.onStart()
        updateActiveUser()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_EMAIL_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // onStart override do work
        }
        else if (requestCode == ADD_EMAIL_REQUEST_CODE && resultCode == Activity.RESULT_CANCELED){
            if (settings.usersList().isEmpty())
                finish()
        }
        else if (requestCode == SEND_EMAIL_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            shortToast(getString(R.string.message_sent))
        }
    }

    private fun initFields() {
        settings = Settings(this)
        toolbar = binding.mainToolbar
        drawer = MailDrawer(this, toolbar, settings)
    }

    private fun initViews(){
        setSupportActionBar(toolbar)
        drawer.createDrawer()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MailboxFragment(drawer))
            .commit()
    }

    fun updateActiveUser(){
        try {
            activeUser = settings.getActiveUser()
            drawer.updateDrawerData()
        }
        catch (e: SettingsException){
            println("No active user!")
            startAddEmailActivity()
        }
    }

    fun startAddEmailActivity(){
        val intent = Intent(this, AddEmailActivity::class.java)
        this@MainActivity.startActivityForResult(intent, ADD_EMAIL_REQUEST_CODE)
    }
}