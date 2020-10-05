package com.example.owlpost.models

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.text.TextUtils.isEmpty


class Settings{
    private val USERS_STORAGE = "users_data"

    private lateinit var settings: SharedPreferences
    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context
        this.settings = context.getSharedPreferences(USERS_STORAGE, Context.MODE_PRIVATE)
    }

    fun usersList(): MutableSet<String>{
        return settings.getStringSet("users_list", setOf()) ?: mutableSetOf()
    }

    fun setCurrentUser(email: String){
        val editor = settings.edit()
        editor.putString("current_user", email)
        editor.apply()
    }

    fun isSetCurrentUser(): Boolean{
        return !isEmpty(settings.getString("current_user", ""))
    }

    fun getCurrentUser(): User{
        val email = settings.getString("current_user", "")?: ""
        val password = settings.getString(email, "")?: ""
        return User(email, password)
    }

    fun addUser(user: User){
        val users = usersList()
        val editor = settings.edit()
        users.add(user.email)
        editor.putStringSet("users_list", users)
        editor.putString(user.email, user.password)
        editor.apply()
    }
}