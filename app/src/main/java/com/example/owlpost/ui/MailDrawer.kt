package com.example.owlpost.ui

import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.owlpost.MainActivity
import com.example.owlpost.R
import com.example.owlpost.fragments.SettingsFragment
import com.example.owlpost.models.IMAPWrapper
import com.example.owlpost.models.Settings
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import kotlinx.coroutines.*

class MailDrawer(private val activity: MainActivity, private val toolbar: Toolbar, private val settings: Settings){
    private lateinit var drawer: Drawer
    private lateinit var header: AccountHeader
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

    fun updateDrawerData(){
        updateHeaderUsersProfiles()
        updateDrawerFolderItems()
    }

    fun updateTitle(){
        val drawerItem = drawer.getDrawerItem(drawer.currentSelection) as PrimaryDrawerItem
        val titleRes = drawerItem.name?.text
        if (titleRes != null)
            toolbar.title = titleRes
    }

    private fun updateHeaderUsersProfiles(){
        val activeUser = activity.activeUser
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

    private fun updateDrawerFolderItems(){
        val activeUser = activity.activeUser
        clearDrawerFolderItems()
        CoroutineScope(Dispatchers.Main).launch{
            val imap = IMAPWrapper(activeUser.email, activeUser.password)
            val folders = imap.folders()
            for (i in folders.indices){
                val drawerItem = PrimaryDrawerItem().withIdentifier(i.toLong())
                    .withSelectable(true)
                    .withName(folders[i].toLowerCase().capitalize())
                drawer.addItemAtPosition(drawerItem, i+1)
            }
            drawer.setSelection(0)
        }
    }

    private fun clearDrawerFolderItems(){
        val items = drawer.drawerItems.toTypedArray()
        for (item in items)
            if (item is PrimaryDrawerItem)
                drawer.removeItem(item.identifier)
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
                    activity.updateActiveUser()
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
            .withSelectedItem(-1)
            .withAccountHeader(header)
            .addDrawerItems(
                DividerDrawerItem().withIdentifier(100),
                SecondaryDrawerItem().withIdentifier(101)
                    .withIconTintingEnabled(true)
                    .withName(R.string.new_email_item)
                    .withSelectable(false)
                    .withIcon(R.drawable.ic_add_user),
                SecondaryDrawerItem().withIdentifier(102)
                    .withIconTintingEnabled(true)
                    .withName(R.string.remove_item)
                    .withSelectable(false)
                    .withIcon(R.drawable.ic_remove_user),
                SecondaryDrawerItem().withIdentifier(103)
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
                    if (drawerItem is PrimaryDrawerItem){
                        this@MailDrawer.updateTitle()
                    }
                    else if (drawerItem is SecondaryDrawerItem){
                        when (drawerItem.identifier.toInt()) {
                            101 -> {
                                // Add email item click
                                activity.startAddEmailActivity()
                            }
                            102 -> {
                                val profile = header.activeProfile
                                if (profile != null){
                                    header.removeProfile(profile)
                                    settings.removeActiveUser()
                                    activity.updateActiveUser()
                                }
                            }
                            103 ->  {
                                // Settings item click
                                activity.supportFragmentManager.beginTransaction()
                                    .addToBackStack(null)
                                    .replace(R.id.fragment_container, SettingsFragment(this@MailDrawer))
                                    .commit()
                                toolbar.title = activity.getString(R.string.settings_item)
                            }
                        }
                    }
                    return false
                }
            })
            .build()
    }
}