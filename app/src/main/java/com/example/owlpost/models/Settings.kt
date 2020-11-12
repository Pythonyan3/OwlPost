package com.example.owlpost.models

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import com.example.owlpost.R
import com.example.owlpost.models.data.User
import java.security.PrivateKey
import java.security.PublicKey

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
        usersInfoSettings.edit(commit = true){
            putString(ACTIVE_USER_STRING_KEY, userEmail)
        }
    }

    private fun resetActiveUser(){
        usersInfoSettings.edit(commit = true){
            remove(ACTIVE_USER_STRING_KEY)
        }
    }

    fun getActiveUser(): User {
        val password: String?
        var email: String?
        val users = usersList()
        with(usersInfoSettings) {
            email = getString(ACTIVE_USER_STRING_KEY, null)
        }
        if (email == null && users.isNotEmpty())
            email = users.elementAt(0)
        with(context.getSharedPreferences(email, Context.MODE_PRIVATE)){
            password = getString(USER_PASSWORD_STRING_KEY, null)
        }
        if (email == null || password == null){
            throw SettingsException(context.getString(R.string.active_user_doesnt_exist))
        }
        setActiveUser(email.toString())
        return User(email.toString(), password)
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
        newUserSettings.edit(commit = true){
            putString(USER_PASSWORD_STRING_KEY, user.password)
            if (newUserSettings.getString(PUBLIC_ENCRYPT_STRING_KEY, null) == null){
                val cipher = CryptoManager()
                // encryption key pair
                val encryptKeyPair = cipher.generateKeysPair()
                val signKeyPair = cipher.generateKeysPair()
                putKeys(user.email, encryptKeyPair.public, signKeyPair.public)
                putKeys(user.email, encryptKeyPair.private, signKeyPair.private)
            }
        }
    }

    fun removeActiveUser(resetUserData: Boolean = false){
        // Remove from users list
        val usersList = usersList()
        val activeUser = getActiveUser()
        usersList.remove(activeUser.email)
        writeUsersList(usersList)
        // Remove user password
        val newUserSettings = context.getSharedPreferences(activeUser.email, Context.MODE_PRIVATE)
        newUserSettings.edit(commit = true){
            if (resetUserData)
                clear()
            else
                remove(USER_PASSWORD_STRING_KEY)
        }
        // clear active user record
        resetActiveUser()
    }

    fun getPublicKey(email: String, stringKey: String): PublicKey{
        val crypto = CryptoManager()
        val newUserSettings = context.getSharedPreferences(email, Context.MODE_PRIVATE)
        val keyString = newUserSettings.getString(stringKey, null) ?: throw SettingsException("")
        return crypto.publicKeyBase64StringDecode(keyString)
    }

    fun getPrivateKey(email: String, stringKey: String): PrivateKey {
        val crypto = CryptoManager()
        val newUserSettings = context.getSharedPreferences(email, Context.MODE_PRIVATE)
        val keyString = newUserSettings.getString(stringKey, null) ?: throw SettingsException("")
        return crypto.privateKeyBase64StringDecode(keyString)
    }

    fun writePublicKeysToFile(email:String, uri: Uri){
        val crypto = CryptoManager()
        val encryptKey = getPublicKey(email, PUBLIC_ENCRYPT_STRING_KEY)
        val signKey = getPublicKey(email, PUBLIC_SIGN_STRING_KEY)
        val encryptSpec = crypto.encodeKeyToSpec(encryptKey)
        val signSpec = crypto.encodeKeyToSpec(signKey)
        val encryptSize = ByteArray(2)
        val signSize = ByteArray(2)

        encryptSize[0] = (encryptSpec.encoded.size.shr(8)).toByte()
        encryptSize[1] = encryptSpec.encoded.size.toByte()
        signSize[0] = (signSpec.encoded.size.shr(8)).toByte()
        signSize[1] = signSpec.encoded.size.toByte()

        val fos = context.contentResolver.openOutputStream(uri) ?: throw SettingsException("")
        fos.write(encryptSize + encryptSpec.encoded + signSize + signSpec.encoded)
        fos.close()
    }

    fun writePrivateKeysToFile(email:String, uri: Uri){
        val crypto = CryptoManager()
        val encryptKey = getPrivateKey(email, PRIVATE_ENCRYPT_STRING_KEY)
        val signKey = getPrivateKey(email, PRIVATE_SIGN_STRING_KEY)
        val encryptSpec = crypto.encodeKeyToSpec(encryptKey)
        val signSpec = crypto.encodeKeyToSpec(signKey)
        val encryptSize = ByteArray(2)
        val signSize = ByteArray(2)

        encryptSize[0] = (encryptSpec.encoded.size.shr(8)).toByte()
        encryptSize[1] = encryptSpec.encoded.size.toByte()
        signSize[0] = (signSpec.encoded.size.shr(8)).toByte()
        signSize[1] = signSpec.encoded.size.toByte()

        val fos = context.contentResolver.openOutputStream(uri) ?: throw SettingsException("")
        fos.write(encryptSize + encryptSpec.encoded + signSize + signSpec.encoded)
        fos.close()
    }

    fun readKeysFromFile(email: String, uri: Uri, isPrivate: Boolean = false){
        val fis = context.contentResolver.openInputStream(uri) ?: throw SettingsException("")
        val sizeArray = ByteArray(2)

        fis.read(sizeArray)
        val encryptSize = (sizeArray[0].toUByte().toInt().shl( 8)) or (sizeArray[1].toUByte().toInt())
        val encryptBytes = ByteArray(encryptSize)
        fis.read(encryptBytes)

        fis.read(sizeArray)
        val signSize = (sizeArray[0].toUByte().toInt().shl( 8)) or (sizeArray[1].toUByte().toInt())
        val signBytes = ByteArray(signSize)
        fis.read(signBytes)
        fis.close()
        // put keys in to settings
        val crypto = CryptoManager()
        if (isPrivate){
            val encryptKey = crypto.privateKeySpecDecode(encryptBytes)
            val signKey = crypto.privateKeySpecDecode(signBytes)
            putKeys(email, encryptKey, signKey)
        }
        else{
            val encryptKey = crypto.publicKeySpecDecode(encryptBytes)
            val signKey = crypto.publicKeySpecDecode(signBytes)
            putKeys(email, encryptKey, signKey)
        }
    }

    private fun putKeys(email: String, encryptKey: PublicKey, signKey: PublicKey){
        val crypto = CryptoManager()
        val newUserSettings = context.getSharedPreferences(email, Context.MODE_PRIVATE)
        newUserSettings.edit(commit = true){
            val base64EncryptKey = crypto.encodeKeyToBase64String(encryptKey)
            val base64SignKey = crypto.encodeKeyToBase64String(signKey)
            putString(PUBLIC_ENCRYPT_STRING_KEY, base64EncryptKey)
            putString(PUBLIC_SIGN_STRING_KEY, base64SignKey)
        }
    }

    private fun putKeys(email: String, encryptKey: PrivateKey, signKey: PrivateKey){
        val crypto = CryptoManager()
        val newUserSettings = context.getSharedPreferences(email, Context.MODE_PRIVATE)
        newUserSettings.edit(commit = true){
            val base64EncryptKey = crypto.encodeKeyToBase64String(encryptKey)
            val base64SignKey = crypto.encodeKeyToBase64String(signKey)
            putString(PRIVATE_ENCRYPT_STRING_KEY, base64EncryptKey)
            putString(PRIVATE_SIGN_STRING_KEY, base64SignKey)
        }
    }

    private fun writeUsersList(usersList: MutableSet<String>){
        usersInfoSettings.edit(commit = true){
            putStringSet(USERS_LIST_STRING_KEY, usersList)
        }
    }
}