package com.example.owlpost.ui

import android.content.Intent
import android.content.res.Resources
import android.view.View
import android.widget.Toolbar
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.owlpost.LoginActivity
import com.example.owlpost.MainActivity
import com.example.owlpost.R
import com.example.owlpost.SendMailActivity
import com.example.owlpost.fragments.SettingsFragment
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem

class MailDrawer(private val activity: AppCompatActivity, private val toolbar: androidx.appcompat.widget.Toolbar){
    private lateinit var drawer: Drawer
    private lateinit var header: AccountHeader

    fun createDrawer(){
        buildHeader()
        buildDrawer()
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

    private fun buildHeader() {
        header = AccountHeaderBuilder()
            .withActivity(activity)
            .withHeaderBackground(R.drawable.drawer_header)
            .addProfiles(
                ProfileDrawerItem()
                    .withName("email1")
                    .withIcon(R.drawable.ic_user_icon_1),
                ProfileDrawerItem()
                    .withName("email2")
                    .withIcon(R.drawable.ic_user_icon_2),
                ProfileDrawerItem()
                    .withName("email3")
                    .withIcon(R.drawable.ic_user_icon_3),
                ProfileDrawerItem()
                    .withName("email4")
                    .withIcon(R.drawable.ic_user_icon_4),
                ProfileDrawerItem()
                    .withName("email5")
                    .withIcon(R.drawable.ic_user_icon_5)
            )
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
                    .withSelectable(true)
                    .withIcon(R.drawable.ic_mail),
                PrimaryDrawerItem().withIdentifier(1)
                    .withIconTintingEnabled(true)
                    .withName(R.string.sent_item)
                    .withSelectable(true)
                    .withIcon(R.drawable.ic_send),
                PrimaryDrawerItem().withIdentifier(2)
                    .withIconTintingEnabled(true)
                    .withName(R.string.drafts_item)
                    .withSelectable(true)
                    .withIcon(R.drawable.ic_drafts),
                PrimaryDrawerItem().withIdentifier(3)
                    .withIconTintingEnabled(true)
                    .withName(R.string.trash_item)
                    .withSelectable(true)
                    .withIcon(R.drawable.ic_trash),
                DividerDrawerItem(),
                PrimaryDrawerItem().withIdentifier(5)
                    .withIconTintingEnabled(true)
                    .withName(R.string.new_email_item)
                    .withSelectable(false)
                    .withIcon(R.drawable.ic_add_user),
                PrimaryDrawerItem().withIdentifier(6)
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
                            this@MailDrawer.refreshTitle()
                        }
                        5 -> {
                            activity.startActivity(Intent(activity, LoginActivity::class.java))
                        }
                        6 ->  {
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

}