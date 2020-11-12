package com.example.owlpost.models.email

import androidx.appcompat.app.AppCompatActivity
import com.example.owlpost.R
import com.example.owlpost.models.MailboxFolderException
import com.example.owlpost.models.data.EmailFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class Mailbox(
    val activity: AppCompatActivity,
    private val email: String,
    private val password: String
){
    private val imapManager: IMAPManager = IMAPManager(email, password)
    private val path = "${activity.getExternalFilesDir(null)}/$email"
    lateinit var folderName: String

    suspend fun getFolders(): Array<EmailFolder>{
        val file = File(path)
        if (!file.exists()){
            println("load folders from server")
            writeFolders(imapManager.folders())
        }
        val list = file.list()
            ?: throw MailboxFolderException(activity.getString(R.string.cannot_read_folders))
        val foldersList = ArrayList<EmailFolder>()
        list.forEach {
            foldersList.add(getFolderInfo(it))
        }
        return foldersList.toTypedArray()
    }

    suspend fun getMessages(start: Int, msgCount: Int = 10){
        val end = start + msgCount
        val folder = getFolderInfo(folderName)
        val file = File("$path/$folderName")
        val localMsgsList = file.list()
            ?: throw MailboxFolderException(activity.getString(R.string.cannot_read_folders))
        when {
            start < localMsgsList.size-1 -> {
                println("storage")
            }
            start < folder.totalCount -> {
                println("server")
            }
            else -> {
                println("No message")
            }
        }
    }

    private fun getFolderInfo(folderName: String): EmailFolder {
        val file = File("$path/$folderName/index")
        val reader = file.bufferedReader()
        val info = reader.readLine().split("/")
        return EmailFolder(folderName, info[0].toInt(), info[1].toInt())
    }

    private fun writeFolders(folders: Array<EmailFolder>){
        folders.forEach {
            File("$path/${it.folderName}").mkdirs()
            val file = File("$path/${it.folderName}/index")
            val writer = file.bufferedWriter()
            writer.write("${it.totalCount}/${it.unreadCount}")
            writer.close()
        }
    }

    suspend fun deleteMailbox(): Boolean {
        var result = false
        withContext(Dispatchers.IO){
            result = File(path).deleteRecursively()
        }
        return result
    }
}