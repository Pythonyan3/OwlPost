package com.example.owlpost

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.example.owlpost.databinding.ActivityMainBinding
import com.example.owlpost.fragments.MailboxFragment
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawer: Drawer
    private lateinit var header: AccountHeader
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        initFields()
        initViews()
    }

    private fun initFields() {
        toolbar = binding.mainToolbar
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MailboxFragment())
        createHeader()
        createDrawer()
    }

    private fun createHeader() {
        header = AccountHeaderBuilder()
            .withActivity(this)
            .withHeaderBackground(R.drawable.drawer_header)
            .addProfiles(
                ProfileDrawerItem().withName("email")
            )
            .build()
    }

    private fun createDrawer() {
        drawer = DrawerBuilder()
            .withActivity(this)
            .withToolbar(toolbar)
            .withActionBarDrawerToggle(true)
            .withSelectedItem(-1)
            .withAccountHeader(header)
            .addDrawerItems(
                PrimaryDrawerItem().withIdentifier(1)
                    .withIconTintingEnabled(true)
                    .withName(R.string.inbox_item)
                    .withSelectable(true)
                    .withIcon(R.drawable.ic_mail),
                PrimaryDrawerItem().withIdentifier(2)
                    .withIconTintingEnabled(true)
                    .withName(R.string.sent_item)
                    .withSelectable(true)
                    .withIcon(R.drawable.ic_send),
                PrimaryDrawerItem().withIdentifier(3)
                    .withIconTintingEnabled(true)
                    .withName(R.string.drafts_item)
                    .withSelectable(true)
                    .withIcon(R.drawable.ic_drafts),
                PrimaryDrawerItem().withIdentifier(4)
                    .withIconTintingEnabled(true)
                    .withName(R.string.trash_item)
                    .withSelectable(true)
                    .withIcon(R.drawable.ic_trash),
                DividerDrawerItem(),
                PrimaryDrawerItem().withIdentifier(5)
                    .withIconTintingEnabled(true)
                    .withName(R.string.settings_item)
                    .withSelectable(true)
                    .withIcon(R.drawable.ic_settings),
            ).build()
    }
}