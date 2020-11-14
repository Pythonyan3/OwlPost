package com.example.owlpost.models.email

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.mail.*
import javax.mail.internet.MimeMessage
import kotlin.collections.ArrayList


const val PROTOCOL = "imap"
const val STORE_PROTOCOL = "imaps"
const val IMAP_PORT = 993
const val GMAIL_ROOT_FOLDER = "[Gmail]/"

class IMAPManager(var email: String, var password: String){
    val emailHost: String
    get() = "$PROTOCOL.${email.substring(email.indexOf("@") + 1)}"


    suspend fun folders(): Array<EmailFolder> {
        val resultFolders = ArrayList<EmailFolder>()
        val store = getStore()
        withContext(Dispatchers.IO){
            store.connect(emailHost, email, password)
            val folders = store.defaultFolder.list("*")
            for (folder in folders) {
                if (folder.type and Folder.HOLDS_MESSAGES != 0) {
                    val folderName = removeFolderSuffix(folder.fullName)
                    if (!isSubFolder(folderName)) {
                        val totalCount = folder.messageCount
                        val unreadCount = folder.unreadMessageCount
                        resultFolders.add(EmailFolder(folderName, totalCount, unreadCount))
                    }
                }
            }
            store.close()
        }
        return resultFolders.toTypedArray()
    }

    fun appendToFolder(msg: MimeMessage, folderName: String){
        val store = getStore()
        val sentFolder = store.getFolder(addFolderSuffix(folderName))
        sentFolder.open(Folder.READ_WRITE)
        sentFolder.appendMessages(arrayOf(msg))
        sentFolder.close(true)
        store.close()
    }

    private fun addFolderSuffix(folderName: String): String{
        return if (email.contains("gmail") && !folderName.toLowerCase(Locale.ROOT).contains("inbox"))
            "[Gmail]/$folderName"
        else
            folderName
    }

    private fun removeFolderSuffix(folderName: String): String {
        return if (folderName.contains(GMAIL_ROOT_FOLDER)){
            val pos = folderName.indexOf(GMAIL_ROOT_FOLDER)
            folderName.substring(pos + GMAIL_ROOT_FOLDER.length)
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
        properties["mail.$STORE_PROTOCOL.ssl.enable"] = "true"
        properties["mail.$STORE_PROTOCOL.port"] = IMAP_PORT.toString()
        return properties
    }

    class EmailAuthenticator(private val login: String, private val password: String) : Authenticator() {
        public override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(login, password)
        }
    }
}