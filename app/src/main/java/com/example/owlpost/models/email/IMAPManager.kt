package com.example.owlpost.models.email

import com.example.owlpost.models.User
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

class IMAPManager(user: User){
    private val emailHost = "$PROTOCOL.${user.email.substring(user.email.indexOf("@") + 1)}"
    private val store = getStoreInstance()
    init {
        store.connect(emailHost, user.email, user.password)
    }

    suspend fun getFolders(): Array<EmailFolder> {
        val resultFolders = ArrayList<EmailFolder>()
        withContext(Dispatchers.IO){
            val folders = store.defaultFolder.list("*")
            for (folder in folders) {
                if (folder.type and Folder.HOLDS_MESSAGES != 0) {
                    val folderName = removeRootFolder(folder.fullName)
                    if (!isSubFolder(folderName)) {
                        val totalCount = folder.messageCount
                        val unreadCount = folder.unreadMessageCount
                        resultFolders.add(EmailFolder(folderName, totalCount, unreadCount))
                    }
                }
            }
        }
        return resultFolders.toTypedArray()
    }

    suspend fun loadMessages(folderName: String, start: Int, end: Int, path: String){
        withContext(Dispatchers.IO){
            val folder = store.getFolder(addRootFolder(folderName))
            folder.open(Folder.READ_ONLY)
            val messages = folder.getMessages(start, end)
            val uidFolder = folder as UIDFolder
            val result = Array(messages.size) {i ->
                OwlMessage(path, uidFolder.getUID(messages[i]), folderName, messages[i] as MimeMessage)
            }
            result.forEach {
                it.writeTo()
            }
            folder.close(false)
        }
    }

    suspend fun syncMessages(uids: LongArray, _folder: EmailFolder, path: String): Boolean{
        var result = false
        withContext(Dispatchers.IO){
            val folder = store.getFolder(addRootFolder(_folder.folderName))
            folder.open(Folder.READ_ONLY)
            val serverFolder = EmailFolder(
                removeRootFolder(folder.fullName),
                folder.messageCount,
                folder.unreadMessageCount
            )
            // sync folder info (msg count, unread msg count)
            val storageFolder = EmailFolder(path, _folder.folderName)
            if (serverFolder != storageFolder)
                serverFolder.writeTo(path)
            val uidFolder = folder as UIDFolder

            // sync deleted message
            val messages = uidFolder.getMessagesByUID(uids)
            for (i in messages.indices){
                if (messages[i] == null){
                    _folder.removeMessage(uids[i], path)
                    result = true
                }
                else{
                    val storeMsg = OwlMessage(path, _folder.folderName, uids[i])
                    if (messages[i].flags != storeMsg.flags){
                        storeMsg.flags = messages[i].flags
                        storeMsg.saveFlags()
                        result = true
                    }
                }
            }
            // sync new messages
            if (uids.isNotEmpty()){
                val lastMessage = folder.getMessage(folder.messageCount)
                val lastMsgUID = uidFolder.getUID(lastMessage)
                if (lastMsgUID > uids[0]){
                    uidFolder.getMessagesByUID(uids[0] + 1, lastMsgUID).forEach { newMessage ->
                        OwlMessage(
                            path,
                            uidFolder.getUID(newMessage),
                            _folder.folderName,
                            newMessage as MimeMessage
                        ).writeTo()
                    }
                    result = true
                }
            }
            folder.close(false)
        }
        return result
    }

    suspend fun markMessageSeen(uid: Long, folderName: String) {
        withContext(Dispatchers.IO){
            val folder = store.getFolder(addRootFolder(folderName))
            folder.open(Folder.READ_WRITE)
            val uidFolder = folder as UIDFolder
            val message = uidFolder.getMessageByUID(uid)
            if (message != null)
                folder.setFlags(arrayOf(message), Flags(Flags.Flag.SEEN), true)
            folder.close(true)
        }
    }

    fun close(){
        store.close()
    }

    private fun addRootFolder(folderName: String): String{
        return if (emailHost.contains("gmail") && !folderName.toLowerCase(Locale.ROOT).contains("inbox"))
            "[Gmail]/$folderName"
        else
            folderName
    }

    private fun removeRootFolder(folderName: String): String {
        return if (folderName.contains(GMAIL_ROOT_FOLDER)){
            val pos = folderName.indexOf(GMAIL_ROOT_FOLDER)
            folderName.substring(pos + GMAIL_ROOT_FOLDER.length)
        } else
            folderName
    }

    private fun isSubFolder(folderName: String): Boolean{
        return folderName.contains("/")
    }

    private fun getStoreInstance(): Store{
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