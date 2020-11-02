package com.example.owlpost

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.example.owlpost.databinding.ActivityMainBinding
import com.example.owlpost.fragments.MailboxFragment
import com.example.owlpost.fragments.SettingsFragment
import com.example.owlpost.ui.MailDrawer
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawer: MailDrawer
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initFields()
        initViews()
    }

    private fun initFields() {
        toolbar = binding.mainToolbar
        drawer = MailDrawer(this, toolbar)
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MailboxFragment(drawer))
            .commit()
        drawer.createDrawer()
    }
}