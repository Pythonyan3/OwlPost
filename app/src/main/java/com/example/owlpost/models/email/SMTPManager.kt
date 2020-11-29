package com.example.owlpost.models.email


import com.example.owlpost.models.SendAttachments
import com.example.owlpost.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.mail.*
import javax.mail.Message
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

const val SMTP_PORT = 465


class SMTPManager(private val user: User){

    suspend fun sendMessage(message: MimeMessage){
        withContext(Dispatchers.IO){
            Transport.send(message)
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

    fun getSendExchangeRequestMessage(email: String, publicEncryptionKey: String, publicSignKey: String): MimeMessage {
        val mimeMessage = MimeMessage(getSession())
        mimeMessage.setFrom(InternetAddress(user.email))
        mimeMessage.setRecipient(Message.RecipientType.TO, InternetAddress(email))
        mimeMessage.subject = EXCHANGE_REQUEST_SUBJECT
        mimeMessage.setHeader(EXCHANGE_REQUEST_HEADER, "")
        mimeMessage.setHeader(ENCRYPTION_KEY_EXCHANGE_HEADER_NAME, publicEncryptionKey)
        mimeMessage.setHeader(SIGNATURE_KEY_EXCHANGE_HEADER_NAME, publicSignKey)
        mimeMessage.setContent(MimeMultipart())
        return mimeMessage
    }

    fun getSendExchangeResponseMessage(email: String, publicEncryptionKey: String, publicSignKey: String): MimeMessage {
        val mimeMessage = MimeMessage(getSession())
        mimeMessage.setFrom(InternetAddress(user.email))
        mimeMessage.setRecipient(Message.RecipientType.TO, InternetAddress(email))
        mimeMessage.subject = EXCHANGE_RESPONSE_SUBJECT
        mimeMessage.setHeader(EXCHANGE_RESPONSE_HEADER, "")
        mimeMessage.setHeader(ENCRYPTION_KEY_EXCHANGE_HEADER_NAME, publicEncryptionKey)
        mimeMessage.setHeader(SIGNATURE_KEY_EXCHANGE_HEADER_NAME, publicSignKey)
        mimeMessage.setContent(MimeMultipart())
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