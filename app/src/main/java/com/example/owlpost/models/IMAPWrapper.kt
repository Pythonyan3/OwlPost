package com.example.owlpost.models

import java.util.*
import javax.mail.*
import kotlin.collections.ArrayList


const val PROTOCOL = "imap"
const val STORE_PROTOCOL = "imaps"
const val IMAP_PORT = 993

class IMAPWrapper(var email: String, var password: String){
    val emailHost: String
    get() = "$PROTOCOL.${email.substring(email.indexOf("@") + 1)}"


    fun folders(): Array<String> {
        val resultFolders = ArrayList<String>()
        val store = getStore()
        store.connect(emailHost, email, password)

        val folders = store.defaultFolder.list("*")
        for (folder in folders) {
            if (folder.type and Folder.HOLDS_MESSAGES != 0) {
                println(folder.fullName)
                resultFolders.add(folder.fullName)
            }
        }
        store.close()
        return resultFolders.toTypedArray()
    }

    fun getStore(): Store{
        val properties = getProperties()
        val session = Session.getDefaultInstance(properties)
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