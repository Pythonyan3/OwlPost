package com.example.owlpost.models

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import com.example.owlpost.R
import com.example.owlpost.models.cryptography.*
import java.io.FileNotFoundException
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
                val manager = KeysManager()
                // encryption key pair
                val encryptKeyPair = manager.generateKeysPair("RSA")
                val signKeyPair = manager.generateKeysPair("RSA")
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

    fun getPublicKey(email: String, keyWorkType: Int): PublicKey{
        val manager = KeysManager()
        val base64Key = readKeyString(email, keyWorkType or PUBLIC_KEY)
        return manager.publicKeyBase64StringDecode(base64Key, ASYMMETRIC_ENCRYPT_ALGORITHM)
    }

    fun getPrivateKey(email: String, keyWorkType: Int): PrivateKey {
        val manager = KeysManager()
        val base64Key = readKeyString(email, keyWorkType or PRIVATE_KEY)
        return manager.privateKeyBase64StringDecode(base64Key, ASYMMETRIC_ENCRYPT_ALGORITHM)
    }

    private fun putKeys(email: String, encryptKey: PublicKey, signKey: PublicKey){
        val manager = KeysManager()
        val base64EncryptKey = manager.encodeKeyToBase64String(encryptKey)
        val base64SignKey = manager.encodeKeyToBase64String(signKey)
        putKeyStrings(email, base64EncryptKey, base64SignKey, PUBLIC_KEY)
    }

    private fun putKeys(email: String, encryptKey: PrivateKey, signKey: PrivateKey){
        val manager = KeysManager()
        val base64EncryptKey = manager.encodeKeyToBase64String(encryptKey)
        val base64SignKey = manager.encodeKeyToBase64String(signKey)
        putKeyStrings(email, base64EncryptKey, base64SignKey, PRIVATE_KEY)
    }

    private fun putKeyStrings(email: String, base64EncryptKey: String, base64SignKey: String, accessKeyType: Int){
        val newUserSettings = context.getSharedPreferences(email, Context.MODE_PRIVATE)
        newUserSettings.edit(commit = true){
            when(accessKeyType){
                PUBLIC_KEY -> {
                    putString(PUBLIC_ENCRYPT_STRING_KEY, base64EncryptKey)
                    putString(PUBLIC_SIGN_STRING_KEY, base64SignKey)
                }
                PRIVATE_KEY -> {
                    putString(PRIVATE_ENCRYPT_STRING_KEY, base64EncryptKey)
                    putString(PRIVATE_SIGN_STRING_KEY, base64SignKey)
                }
                else ->
                    throw SettingsException(context.getString(R.string.access_key_type, accessKeyType))
            }
        }
    }

    private fun readKeyString(email: String, keyType: Int): String {
        val newUserSettings = context.getSharedPreferences(email, Context.MODE_PRIVATE)
        return when (keyType) {
            PUBLIC_KEY or ENCRYPT_KEY ->
                newUserSettings.getString(PUBLIC_ENCRYPT_STRING_KEY, null)
                    ?: throw SettingsException(context.getString(R.string.key_exist))
            PUBLIC_KEY or SIGN_KEY ->
                newUserSettings.getString(PUBLIC_SIGN_STRING_KEY, null)
                    ?: throw SettingsException(context.getString(R.string.key_exist))
            PRIVATE_KEY or ENCRYPT_KEY ->
                newUserSettings.getString(PRIVATE_ENCRYPT_STRING_KEY, null)
                    ?: throw SettingsException(context.getString(R.string.key_exist))
            PRIVATE_KEY or SIGN_KEY ->
                newUserSettings.getString(PRIVATE_SIGN_STRING_KEY, null)
                    ?: throw SettingsException(context.getString(R.string.key_exist))
            else ->
                throw SettingsException(context.getString(R.string.key_type, keyType))
        }
    }

    fun writeKeysToFile(email:String, uri: Uri, accessKeyType: Int){
        val base64EncryptKey = readKeyString(email, accessKeyType or ENCRYPT_KEY).toByteArray()
        val base64SignKey = readKeyString(email, accessKeyType or SIGN_KEY).toByteArray()

        val encryptSize = ByteArray(2)
        val signSize = ByteArray(2)

        encryptSize[0] = (base64EncryptKey.size.shr(8)).toByte()
        encryptSize[1] = base64EncryptKey.size.toByte()
        signSize[0] = (base64SignKey.size.shr(8)).toByte()
        signSize[1] = base64SignKey.size.toByte()

        val fos = context.contentResolver.openOutputStream(uri) ?: throw FileNotFoundException("")
        fos.write(encryptSize + base64EncryptKey + signSize + base64SignKey)
        fos.close()
    }

    fun readKeysFromFile(email: String, uri: Uri, accessKeyType: Int){
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
        val manager = KeysManager()
        val base64EncryptKey = String(encryptBytes)
        val base64SignKey = String(signBytes)
        when (accessKeyType){
            PUBLIC_KEY -> {
                val encryptKey = manager.publicKeyBase64StringDecode(base64EncryptKey, ASYMMETRIC_ENCRYPT_ALGORITHM)
                val signKey = manager.publicKeyBase64StringDecode(base64SignKey, ASYMMETRIC_ENCRYPT_ALGORITHM)
                putKeys(email, encryptKey, signKey)
            }
            PRIVATE_KEY -> {
                val encryptKey = manager.privateKeyBase64StringDecode(base64EncryptKey, ASYMMETRIC_ENCRYPT_ALGORITHM)
                val signKey = manager.privateKeyBase64StringDecode(base64SignKey, ASYMMETRIC_ENCRYPT_ALGORITHM)
                putKeys(email, encryptKey, signKey)
            }
            else ->
                throw SettingsException(context.getString(R.string.access_key_type, accessKeyType))
        }

    }

    private fun writeUsersList(usersList: MutableSet<String>){
        usersInfoSettings.edit(commit = true){
            putStringSet(USERS_LIST_STRING_KEY, usersList)
        }
    }
}