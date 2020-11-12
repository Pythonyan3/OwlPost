package com.example.owlpost

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.example.owlpost.databinding.ActivityMainBinding
import com.example.owlpost.fragments.MailboxFragment
import com.example.owlpost.models.email.Mailbox
import com.example.owlpost.models.Settings
import com.example.owlpost.models.SettingsException
import com.example.owlpost.models.data.User
import com.example.owlpost.ui.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.mail.MessagingException


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toolbar: Toolbar
    lateinit var settings: Settings
    lateinit var drawer: MailDrawer
    lateinit var activeUser: User
    lateinit var mailbox: Mailbox

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
            .replace(R.id.fragment_container, MailboxFragment())
            .commit()
    }

    fun updateActiveUser(){
        try {
            activeUser = settings.getActiveUser()
            mailbox = Mailbox(this, activeUser.email, activeUser.password)
            CoroutineScope(Dispatchers.Main).launch {
                drawer.updateDrawerData(activeUser, settings.usersList(), mailbox.getFolders())
            }
        }
        catch (e: MessagingException){
            shortToast(getString(R.string.internet_connection))
        }
        catch (e: SettingsException){
            startAddEmailActivity()
        }
    }

    fun loadMessages(){
        CoroutineScope(Dispatchers.Main).launch {
            mailbox.getMessages(0)
        }
    }

    fun startAddEmailActivity(){
        val intent = Intent(this, AddEmailActivity::class.java)
        this@MainActivity.startActivityForResult(intent, ADD_EMAIL_REQUEST_CODE)
    }
}