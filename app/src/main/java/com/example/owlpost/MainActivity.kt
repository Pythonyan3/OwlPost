package com.example.owlpost

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.example.owlpost.databinding.ActivityMainBinding
import com.example.owlpost.fragments.MailboxFragment
import com.example.owlpost.models.email.Mailbox
import com.example.owlpost.models.Settings
import com.example.owlpost.models.SettingsException
import com.example.owlpost.models.User
import com.example.owlpost.ui.*
import com.example.owlpost.ui.widgets.MailDrawer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_add_mail.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import javax.mail.MessagingException


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var activeUser: User
    lateinit var drawer: MailDrawer
    lateinit var mailbox: Mailbox
    lateinit var settings: Settings
    lateinit var toolbar: Toolbar
    lateinit var drawerUpdateJob: Job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initFields()
        initViews()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MailboxFragment(), "mailbox")
            .commit()
    }

    private fun initFields() {
        settings = Settings(this)
        toolbar = binding.mainToolbar
        drawer = MailDrawer(this, toolbar, settings)
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        drawer.createDrawer()
    }

    fun updateActiveUser(fireOnClick: Boolean = true) {
        activeUser = settings.getActiveUser()
        mailbox = Mailbox(this, activeUser)
        drawerUpdateJob = CoroutineScope(Dispatchers.Main).async {
            try{
                drawer.updateHeaderProfiles(activeUser, settings.usersList())
                drawer.updateDrawerFolderItems(mailbox.getFolders(), fireOnClick)
            }
            catch (e: MessagingException){
                drawer.clearDrawerFolderItems()
                Snackbar.make(
                    this@MainActivity.fragment_container,
                    getString(R.string.internet_connection),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    fun startAddEmailActivity() {
        val intent = Intent(this, AddEmailActivity::class.java)
        this.startActivityForResult(intent, ADD_EMAIL_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_EMAIL_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // onStart override do work
        } else if (requestCode == ADD_EMAIL_REQUEST_CODE && resultCode == Activity.RESULT_CANCELED) {
            if (settings.usersList().isEmpty())
                finish()
        } else if (requestCode == SEND_EMAIL_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            shortToast(getString(R.string.message_sent))
        }
    }
}