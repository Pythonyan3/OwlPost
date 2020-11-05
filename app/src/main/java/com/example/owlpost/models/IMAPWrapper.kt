package com.example.owlpost.models

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.mail.*
import kotlin.collections.ArrayList


const val PROTOCOL = "imap"
const val STORE_PROTOCOL = "imaps"
const val IMAP_PORT = 993
const val GMAIL_SUFFIX = "[Gmail]/"

class IMAPWrapper(var email: String, var password: String){
    val emailHost: String
    get() = "$PROTOCOL.${email.substring(email.indexOf("@") + 1)}"


    suspend fun folders(): Array<String> {
        val resultFolders = ArrayList<String>()
        val store = getStore()
        withContext(Dispatchers.IO){
            store.connect(emailHost, email, password)

            val folders = store.defaultFolder.list("*")
            for (folder in folders) {
                if (folder.type and Folder.HOLDS_MESSAGES != 0) {
                    val folderName = removeFolderSuffix(folder.fullName)
                    if (!isSubFolder(folderName))
                        resultFolders.add(folderName)
                }
            }
            store.close()
        }
        return resultFolders.toTypedArray()
    }

    private fun removeFolderSuffix(folderName: String): String {
        return if (folderName.contains(GMAIL_SUFFIX)){
            val pos = folderName.indexOf(GMAIL_SUFFIX)
            folderName.substring(pos + GMAIL_SUFFIX.length)
        } else
            folderName
    }

    private fun isSubFolder(folderName: String): Boolean{
        return folderName.contains("/")
    }

    fun getStore(): Store{
        val properties = getProperties()
        val session = Session.getInstance(properties)
        return session.store
    }

    private fun getProperties(): Properties {
        val properties = Properties()
        properties["mail.store.protocol"] = STORE_PROTOCOL
        properties["mail.${STORE_PROTOCOL}.ssl.enable"] = "true"
        properties["mail.$STORE_PROTOCOL.port"] = IMAP_PORT.toString()
        return properties
    }

    class EmailAuthenticator(private val login: String, private val password: String) : Authenticator() {
        public override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(login, password)
        }
    }
}