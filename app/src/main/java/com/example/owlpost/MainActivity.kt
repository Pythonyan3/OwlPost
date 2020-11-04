package com.example.owlpost

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.example.owlpost.databinding.ActivityMainBinding
import com.example.owlpost.fragments.MailboxFragment
import com.example.owlpost.models.Settings
import com.example.owlpost.ui.ADD_EMAIL_REQUEST_CODE
import com.example.owlpost.ui.MailDrawer


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawer: MailDrawer
    private lateinit var toolbar: Toolbar
    private lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initFields()
        initViews()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_EMAIL_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            drawer.refreshHeader()
        }
        else if (requestCode == ADD_EMAIL_REQUEST_CODE && resultCode == Activity.RESULT_CANCELED){
            if (settings.usersList().isEmpty())
                finish()
        }
    }

    private fun initFields() {
        settings = Settings(this)
        toolbar = binding.mainToolbar
        drawer = MailDrawer(this, toolbar)
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        drawer.createDrawer()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MailboxFragment(drawer))
            .commit()
    }
}