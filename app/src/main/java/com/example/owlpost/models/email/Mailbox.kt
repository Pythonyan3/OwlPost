package com.example.owlpost.models.email

import android.content.Context
import com.example.owlpost.R
import com.example.owlpost.models.MailboxFolderException
import com.example.owlpost.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import javax.mail.Session
import javax.mail.internet.MimeMessage
import kotlin.collections.ArrayList

class Mailbox(
    val context: Context,
    user: User
) {
    private var managerIMAP: IMAPManager = IMAPManager(user)
    private var path = "${context.getExternalFilesDir(null)}/$user.email"
    var currentFolderName: String = "Inbox"

    suspend fun getFolders(): Array<EmailFolder> {
        val folderNames = readFolderNames()
        return if (folderNames == null) {
            val folders = managerIMAP.folders()
            writeFolders(folders)
            folders
        } else {
            val folders = ArrayList<EmailFolder>()
            folderNames.forEach { folderName: String ->
                folders.add(EmailFolder(path, folderName))
            }
            folders.toTypedArray()
        }
    }

    suspend fun getMessages(offset: Int = 0, messageCount: Int = 10): Array<OwlMessage> {
        val file = File("$path/$currentFolderName")
        val folder = EmailFolder(path, currentFolderName)

        if (offset >= folder.totalCount || folder.totalCount == 0)
            return arrayOf()
        var uids = file.list { _, name -> name != "index" }
            ?: throw MailboxFolderException(context.getString(R.string.read_message_error))
        if (offset >= uids.size) {
            val start = if (offset + messageCount < folder.totalCount) folder.totalCount - (offset + messageCount) else 1
            val end = folder.totalCount - offset
            managerIMAP.loadMessages(currentFolderName, start, end, path)
        }
        uids = file.list { _, name -> name != "index" }
            ?: throw MailboxFolderException(context.getString(R.string.read_message_error))
        val messageUIDs = Array(uids.size) {i -> uids[i].toLong()}
        messageUIDs.sortDescending()
        val end = if (messageUIDs.size > offset + messageCount) offset + messageCount else messageUIDs.size
        val neededUIDs = messageUIDs.slice(offset until end).toTypedArray()
        return readMessages(neededUIDs)
    }

    suspend fun resetMailbox(): Boolean {
        var result = false
        withContext(Dispatchers.IO) {
            result = File(path).deleteRecursively()
        }
        return result
    }

    private fun readMessages(messageUIDs: Array<Long>): Array<OwlMessage>{
        return Array(messageUIDs.size) {i ->
            OwlMessage(path, currentFolderName, messageUIDs[i])
        }
    }

    private fun readFolderNames(): Array<String>? {
        val file = File(path)
        return file.list()
    }

    private fun writeFolders(folders: Array<EmailFolder>) {
        folders.forEach {
            it.writeTo(path)
        }
    }
}