package com.example.owlpost.ui

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.owlpost.AddEmailActivity
import com.example.owlpost.R
import com.example.owlpost.fragments.SettingsFragment
import com.example.owlpost.models.Settings
import com.example.owlpost.models.SettingsException
import com.example.owlpost.models.User
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile

class MailDrawer(private val activity: AppCompatActivity, private val toolbar: Toolbar){
    private lateinit var drawer: Drawer
    private lateinit var header: AccountHeader
    private var settings: Settings = Settings(activity)
    private val icons = arrayOf(
        ResourcesCompat.getDrawable(activity.resources, R.drawable.ic_user_icon_1, null),
        ResourcesCompat.getDrawable(activity.resources, R.drawable.ic_user_icon_2, null),
        ResourcesCompat.getDrawable(activity.resources, R.drawable.ic_user_icon_3, null),
        ResourcesCompat.getDrawable(activity.resources, R.drawable.ic_user_icon_4, null),
        ResourcesCompat.getDrawable(activity.resources, R.drawable.ic_user_icon_5, null)
    )

    fun createDrawer(){
        buildHeader()
        buildDrawer()
        refreshHeader()
    }

    fun disableDrawer(){
        drawer.actionBarDrawerToggle?.isDrawerIndicatorEnabled = false
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val drawerLayout = drawer.drawerLayout
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        toolbar.setNavigationOnClickListener {
            activity.supportFragmentManager.popBackStack()
        }
    }

    fun enableDrawer(){
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        drawer.actionBarDrawerToggle?.isDrawerIndicatorEnabled = true
        val drawerLayout = drawer.drawerLayout
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        toolbar.setNavigationOnClickListener {
            drawer.openDrawer()
        }
    }

    fun refreshTitle(){
        val drawerItem = drawer.getDrawerItem(drawer.currentSelection) as PrimaryDrawerItem
        val titleRes = drawerItem.name?.textRes
        if (titleRes != null)
            toolbar.title = activity.getString(titleRes)
    }

    fun refreshHeader(){
        try {
            val activeUser = settings.getActiveUser()
            val users = settings.usersList()
            header.clear()
            for (i in users.indices){
                header.addProfiles(ProfileDrawerItem()
                    .withIdentifier(i.toLong())
                    .withName(users.elementAt(i))
                    .withIcon(icons[i % icons.size])
                )
            }
            header.setActiveProfile(users.indexOf(activeUser.email).toLong())
        }
        catch (e: SettingsException){
            startAddEmailActivity()
        }
    }

    private fun buildHeader() {
        header = AccountHeaderBuilder()
            .withActivity(activity)
            .withHeaderBackground(R.drawable.drawer_header)
            .withOnAccountHeaderListener(object: AccountHeader.OnAccountHeaderListener{
                override fun onProfileChanged(
                    view: View?,
                    profile: IProfile<*>,
                    current: Boolean
                ): Boolean {
                    val email = profile.name?.text as String
                    settings.setActiveUser(email)
                    return false
                }

            })
            .build()
    }


    private fun buildDrawer() {
        drawer = DrawerBuilder()
            .withActivity(activity)
            .withToolbar(toolbar)
            .withActionBarDrawerToggle(true)
            .withSelectedItem(0)
            .withAccountHeader(header)
            .addDrawerItems(
                PrimaryDrawerItem().withIdentifier(0)
                    .withIconTintingEnabled(true)
                    .withName(R.string.inbox_item)
                    .withIcon(R.drawable.ic_mail),
                PrimaryDrawerItem().withIdentifier(1)
                    .withIconTintingEnabled(true)
                    .withName(R.string.sent_item)
                    .withIcon(R.drawable.ic_send),
                PrimaryDrawerItem().withIdentifier(2)
                    .withIconTintingEnabled(true)
                    .withName(R.string.drafts_item)
                    .withIcon(R.drawable.ic_drafts),
                PrimaryDrawerItem().withIdentifier(3)
                    .withIconTintingEnabled(true)
                    .withName(R.string.trash_item)
                    .withIcon(R.drawable.ic_trash),
                DividerDrawerItem(),
                PrimaryDrawerItem().withIdentifier(5)
                    .withIconTintingEnabled(true)
                    .withName(R.string.new_email_item)
                    .withSelectable(false)
                    .withIcon(R.drawable.ic_add_user),
                PrimaryDrawerItem().withIdentifier(6)
                    .withIconTintingEnabled(true)
                    .withName(R.string.remove_item)
                    .withSelectable(false)
                    .withIcon(R.drawable.ic_remove_user),
                PrimaryDrawerItem().withIdentifier(7)
                    .withIconTintingEnabled(true)
                    .withName(R.string.settings_item)
                    .withSelectable(false)
                    .withIcon(R.drawable.ic_settings)
            ).withOnDrawerItemClickListener(object: Drawer.OnDrawerItemClickListener{
                override fun onItemClick(
                    view: View?,
                    position: Int,
                    drawerItem: IDrawerItem<*>
                ): Boolean {
                    when (drawerItem.identifier.toInt()){
                        in 0..3 -> {
                            // Mailboxes item click
                            this@MailDrawer.refreshTitle()
                        }
                        5 -> {
                            // Add email item click
                            startAddEmailActivity()
                        }
                        6 -> {
                            val profile = header.activeProfile
                            val email = profile?.name?.text as String
                            settings.removeUser(email)
                            header.removeProfile(profile)
                            refreshHeader()
                        }
                        7 ->  {
                            // Settings item click
                            activity.supportFragmentManager.beginTransaction()
                                .addToBackStack(null)
                                .replace(R.id.fragment_container, SettingsFragment(this@MailDrawer))
                                .commit()
                            toolbar.title = activity.getString(R.string.settings_item)
                        }
                    }
                    return false
                }
            })
            .build()
    }

    private fun startAddEmailActivity(){
        val intent = Intent(activity, AddEmailActivity::class.java)
        activity.startActivityForResult(intent, ADD_EMAIL_REQUEST_CODE)
    }
}