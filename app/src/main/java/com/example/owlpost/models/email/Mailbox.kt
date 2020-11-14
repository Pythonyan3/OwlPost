package com.example.owlpost.models.email

import androidx.appcompat.app.AppCompatActivity
import com.example.owlpost.R
import com.example.owlpost.models.MailboxFolderException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FilenameFilter

class Mailbox(
    val activity: AppCompatActivity,
    email: String,
    password: String
){
    private val managerIMAP: IMAPManager = IMAPManager(email, password)
    private val path = "${activity.getExternalFilesDir(null)}/$email"
    lateinit var currentFolderName: String

    suspend fun getFolders(): Array<EmailFolder>{
        val folderNames = readFolderNames()
        return if (folderNames == null){
            val folders = managerIMAP.folders()
            writeFolders(folders)
            folders
        } else{
            val folders = ArrayList<EmailFolder>()
            folderNames.forEach { folderName: String ->
                folders.add(EmailFolder(path, folderName))
            }
            folders.toTypedArray()
        }
    }

    fun getMessages(offset: Int = 0, messageCount: Int = 10): Array<MessageItem>{
        val file = File("$path/$currentFolderName")
        val folder = EmailFolder(path, currentFolderName)

        if (offset > folder.totalCount || folder.totalCount == 0)
            return arrayOf()
        val messageUIDs = file.list { _, name -> name != "index" }
            ?: throw MailboxFolderException(activity.getString(R.string.read_message_error))

        if (messageUIDs.size < offset){
            println("read from storage")
        }
        else{
            println("load new from server")
        }
        return arrayOf()
    }

    suspend fun resetMailbox(): Boolean {
        var result = false
        withContext(Dispatchers.IO){
            result = File(path).deleteRecursively()
        }
        return result
    }

    private fun readFolderNames(): Array<String>? {
        val file = File(path)
        return file.list()
    }

    private fun writeFolders(folders: Array<EmailFolder>){
        folders.forEach {
            it.writeTo(path)
        }
    }
}