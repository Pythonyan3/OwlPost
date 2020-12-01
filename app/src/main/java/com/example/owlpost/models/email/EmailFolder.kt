package com.example.owlpost.models.email

import com.example.owlpost.models.MailboxFolderException
import java.io.File

class EmailFolder {
    val folderName: String
    val totalCount: Int
    val unreadCount: Int

    constructor(_folderName: String, _totalCount: Int, _unreadCount: Int) {
        folderName = _folderName
        totalCount = _totalCount
        unreadCount = _unreadCount
    }

    constructor(_path: String, _folderName: String) {
        val file = File("$_path/$_folderName/index")
        val reader = file.bufferedReader()
        val info = reader.readLine().split("/")
        folderName = _folderName
        totalCount = info[0].toInt()
        unreadCount = info[1].toInt()
    }

    suspend fun getMessages(
        managerIMAP: IMAPManager,
        path: String,
        offset: Int = 0,
        msgCount: Int = 10
    ): Array<OwlMessage> {

        if (offset >= totalCount || totalCount == 0)
            return arrayOf()
        var uids = getMessagesUIDs(path)
        if (offset >= uids.size) {
            val end = totalCount - offset
            val start = if (end - (msgCount - 1) > 0) end - (msgCount - 1) else 1
            managerIMAP.loadMessages(folderName, start, end, path)
        }
        uids = getMessagesUIDs(path)
        val messageUIDs = Array(uids.size) { i -> uids[i].toLong() }
        messageUIDs.sortDescending()
        val end = if (messageUIDs.size > offset + msgCount) offset + msgCount else messageUIDs.size
        val neededUIDs = messageUIDs.slice(offset until end).toTypedArray()
        return readMessages(neededUIDs, path)
    }

    private fun readMessages(messageUIDs: Array<Long>, path: String): Array<OwlMessage> {
        return Array(messageUIDs.size) { i ->
            OwlMessage(path, folderName, messageUIDs[i])
        }
    }

    private fun getMessagesUIDs(path: String): Array<out String> {
        val file = File("$path/${folderName}")
        return file.list { _, name -> name != "index" }
            ?: throw MailboxFolderException("Cannot read list of message from storage")
    }

    suspend fun sync(managerIMAP: IMAPManager, path: String): Boolean {
        val uids = getMessagesUIDs(path)
        val messagesUIDs = Array(uids.size) { i -> uids[i].toLong() }.toLongArray()
        messagesUIDs.sortDescending()
        return managerIMAP.syncMessages(messagesUIDs, this, path)
    }

    fun writeTo(path: String) {
        File("$path/${folderName}").mkdirs()
        val file = File("$path/${folderName}/index")
        val writer = file.bufferedWriter()
        writer.write("${totalCount}/${unreadCount}")
        writer.close()
    }

    fun removeMessage(uid: Long, path: String){
        File("$path/$folderName/$uid").delete()
    }

    override fun equals(other: Any?): Boolean {
        if (other is EmailFolder)
            return folderName.equals(other.folderName, ignoreCase = true)
                    && totalCount == other.totalCount && unreadCount == other.unreadCount
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = folderName.hashCode()
        result = 31 * result + totalCount
        result = 31 * result + unreadCount
        return result
    }
}