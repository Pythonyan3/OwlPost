package com.example.owlpost.models

import android.content.Context
import android.content.SharedPreferences
import com.example.owlpost.R

private const val USERS_LIST_STORAGE_NAME = "users_info"
private const val USERS_LIST_STRING_KEY = "users_list"
private const val ACTIVE_USER_STRING_KEY = "active_user"
private const val USER_PASSWORD_STRING_KEY = "password"
private const val PUBLIC_ENCRYPT_STRING_KEY = "publicEncryptKey"
private const val PRIVATE_ENCRYPT_STRING_KEY = "privateEncryptKey"
private const val PUBLIC_SIGN_STRING_KEY = "publicSignKey"
private const val PRIVATE_SIGN_STRING_KEY = "privateSignKey"

class Settings(private val context: Context) {
    private var usersInfoSettings: SharedPreferences =
        context.getSharedPreferences(USERS_LIST_STORAGE_NAME, Context.MODE_PRIVATE)

    fun usersList(): MutableSet<String> {
        val resultSet = mutableSetOf<String>()
        val usersList = usersInfoSettings.getStringSet(USERS_LIST_STRING_KEY, setOf()) ?: mutableSetOf()
        usersList.forEach {
            resultSet.add(it)
        }
        return resultSet
    }

    fun setActiveUser(userEmail: String) {
        val editor = usersInfoSettings.edit()
        editor.putString(ACTIVE_USER_STRING_KEY, userEmail)
        editor.apply()
    }

    private fun resetActiveUser(){
        val editor = usersInfoSettings.edit()
        editor.remove(ACTIVE_USER_STRING_KEY)
        editor.apply()
    }

    fun getActiveUser(): User {
        var email = usersInfoSettings.getString(ACTIVE_USER_STRING_KEY, null)
        val users = usersList()
        if (email == null && users.isEmpty())
            throw SettingsException(context.getString(R.string.active_user_doesnt_exist))
        else if (email == null){
            email = users.elementAt(0)
        }
        val settings = context.getSharedPreferences(email, Context.MODE_PRIVATE)
        val password = settings.getString(USER_PASSWORD_STRING_KEY, null)
        setActiveUser(email)
        return User(email, password!!)
    }

    fun isExist(user: User): Boolean{
        return usersList().contains(user.email)
    }

    fun addUser(user: User) {
        val usersList = usersList()
        if (usersList.contains(user.email))
            throw SettingsException(context.getString(R.string.email_already_exists))

        // write user email to list of all emails
        usersList.add(user.email)
        writeUsersList(usersList)

        // write user's password and new key pairs to own settings file
        val newUserSettings = context.getSharedPreferences(user.email, Context.MODE_PRIVATE)
        val userEditor = newUserSettings.edit()
        userEditor.putString(USER_PASSWORD_STRING_KEY, user.password)
        // check if already have keys
        if (newUserSettings.getString(PUBLIC_ENCRYPT_STRING_KEY, null) == null){
            val cipher = CipherWrapper()
            // encryption key pair
            var keyPair = cipher.generateKeysPair()
            var base64PublicKey = cipher.encodeKey(keyPair.public)
            var base64PrivateKey = cipher.encodeKey(keyPair.private)
            userEditor.putString(PUBLIC_ENCRYPT_STRING_KEY, base64PublicKey)
            userEditor.putString(PRIVATE_ENCRYPT_STRING_KEY, base64PrivateKey)
            // signature key pair
            keyPair = cipher.generateKeysPair()
            base64PublicKey = cipher.encodeKey(keyPair.public)
            base64PrivateKey = cipher.encodeKey(keyPair.private)
            userEditor.putString(PUBLIC_SIGN_STRING_KEY, base64PublicKey)
            userEditor.putString(PRIVATE_SIGN_STRING_KEY, base64PrivateKey)
        }
        userEditor.apply()
    }

    fun removeUser(email: String, reset: Boolean = false){
        // Remove from users list
        val activeUser = getActiveUser()
        if (activeUser.email == email)
            resetActiveUser()
        val users = usersList()
        users.remove(email)
        writeUsersList(users)
        // Remove user password
        val newUserSettings = context.getSharedPreferences(email, Context.MODE_PRIVATE)
        val userEditor = newUserSettings.edit()
        userEditor.remove(USER_PASSWORD_STRING_KEY)
        // reset user's keys
        if (reset){
            userEditor.remove(PUBLIC_ENCRYPT_STRING_KEY)
            userEditor.remove(PRIVATE_ENCRYPT_STRING_KEY)
            userEditor.remove(PUBLIC_SIGN_STRING_KEY)
            userEditor.remove(PRIVATE_SIGN_STRING_KEY)
        }
        userEditor.apply()
    }

    private fun writeUsersList(usersList: MutableSet<String>){
        val infoEditor = usersInfoSettings.edit()
        infoEditor.putStringSet(USERS_LIST_STRING_KEY, usersList)
        infoEditor.apply()
    }
}