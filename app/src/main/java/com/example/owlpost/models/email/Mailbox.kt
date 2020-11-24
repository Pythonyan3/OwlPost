package com.example.owlpost.models.email

import android.content.Context
import com.example.owlpost.R
import com.example.owlpost.models.MailboxFolderException
import com.example.owlpost.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.IndexOutOfBoundsException

class Mailbox(
    val context: Context,
    user: User
) {
    private var managerIMAP: IMAPManager = IMAPManager(user)
    private var path = "${context.getExternalFilesDir(null)}/${user.email}"
    private var selectedFolder: EmailFolder
    get() = EmailFolder(path, selectedFolderName)
    set(value) {}
    var selectedFolderName: String = "Inbox"

    suspend fun getFolders(): Array<EmailFolder> {
        var folders = readFolders()
        return if (folders == null) {
            folders = managerIMAP.getFolders()
            writeFolders(folders)
            folders
        } else {
            folders
        }
    }

    suspend fun getMessages(offset: Int = 0, msgCount: Int = 10): Array<OwlMessage> {
        return try{
            selectedFolder.getMessages(managerIMAP, path, offset, msgCount)
        } catch (e: IndexOutOfBoundsException){
            syncFolder()
            selectedFolder.getMessages(managerIMAP, path, offset, msgCount)
        }
    }

    suspend fun resetMailbox(): Boolean {
        var result = false
        withContext(Dispatchers.IO) {
            result = File(path).deleteRecursively()
        }
        return result
    }

    suspend fun syncFolder(): Boolean {
        return selectedFolder.sync(managerIMAP, path)
    }

    fun changeUser(user: User, resetCurrentFolderName: Boolean = false) {
        managerIMAP = IMAPManager(user)
        path = "${context.getExternalFilesDir(null)}/${user.email}"
        if (resetCurrentFolderName)
            selectedFolder = EmailFolder(path, "Inbox")
    }

    private fun readFolders(): Array<EmailFolder>? {
        val fileNames = File(path).list() ?: return null
        return Array(fileNames.size) { i ->
            EmailFolder(path, fileNames[i])
        }
    }

    private fun writeFolders(folders: Array<EmailFolder>) {
        folders.forEach {
            it.writeTo(path)
        }
    }
}