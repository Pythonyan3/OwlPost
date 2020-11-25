package com.example.owlpost.models.email


import com.example.owlpost.models.SendAttachments
import com.example.owlpost.models.UriAttachment
import com.example.owlpost.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.activation.DataHandler
import javax.mail.*
import javax.mail.Message
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource

const val SMTP_PORT = 465

class SMTPManager(private val user: User){

    suspend fun sendMessage(message: OwlMessage){
        withContext(Dispatchers.IO){
            Transport.send(message.message)
        }
    }

    fun getMimeMessage(
        recipient: String,
        subject: String,
        attachments: SendAttachments,
        plainText: String,
        html: String
    ): MimeMessage {
        val manager = MimeMessageManager()
        val mimeMessage = MimeMessage(getSession())
        manager.buildMimeMessage(mimeMessage, user.email, recipient, subject, attachments, plainText, html)
        return mimeMessage
    }

    private fun getSession(): Session {
        val properties = getProperties()
        val auth = EmailAuthenticator(user.email, user.password)
        return Session.getInstance(properties, auth)
    }

    private fun getProperties(): Properties {
        val properties = Properties()
        properties["mail.smtp.host"] = getEmailHost(user.email)
        properties["mail.smtp.starttls.enable"] = "true"
        properties["mail.smtp.ssl.enable"] = "true"
        properties["mail.smtp.port"] = SMTP_PORT
        properties["mail.smtp.auth"] = "true"
        return properties
    }

    private fun getEmailHost(email: String): String{
        return "smtp.${email.substring(email.indexOf("@") + 1)}"
    }

    class EmailAuthenticator(private val email: String, private val password: String): Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(email, password)
        }
    }
}