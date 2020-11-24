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

class SMTPManager(private val user: User){
    private val SMTP_PORT = 465

    suspend fun sendMessage(
        recipient: String,
        subject: String,
        attachments: SendAttachments,
        plainText: String,
        html: String = ""
    ){
        val session = getSession()
        val message = MimeMessage(session)

        // init primary message fields
        message.setFrom(InternetAddress(user.email))
        message.setRecipient(Message.RecipientType.TO, InternetAddress(recipient))
        message.subject = subject

        // create multipart
        val multiPart = MimeMultipart("alternative")

        // create plain text body part
        // if html given create html text body part and set alternative subtype
        val plainTextBodyPart = getPlainTextBodyPart(plainText)
        multiPart.addBodyPart(plainTextBodyPart)
        val htmlBodyPart = getHTMLBodyPart(html)
        multiPart.addBodyPart(htmlBodyPart)

        //TODO digital signature
        withContext(Dispatchers.IO) {
            for (i in 0 until attachments.size) {
                val attachmentBodyPart = getAttachmentBodyPart(attachments[i])
                multiPart.addBodyPart(attachmentBodyPart)
            }
            message.setContent(multiPart)
            Transport.send(message)
        }
    }

    private fun getPlainTextBodyPart(text: String): MimeBodyPart {
        val bodyPart = MimeBodyPart()
        bodyPart.setContent(text, "text/plain; charset=utf-8")
        bodyPart.setHeader("Content-Transfer-Encoding", "base64")
        return bodyPart
    }

    private fun getHTMLBodyPart(text: String): MimeBodyPart {
        val bodyPart = MimeBodyPart()
        bodyPart.setContent(text, "text/html; charset=utf-8")
        bodyPart.setHeader("Content-Transfer-Encoding", "base64")
        return bodyPart
    }

    private fun getAttachmentBodyPart(uri: UriAttachment): MimeBodyPart {
        val bytes: ByteArray
        val inputStream = uri.getInputStream()
        bytes = inputStream?.readBytes() as ByteArray
        inputStream.close()
        val attachmentBodyPart = MimeBodyPart()
        val bytesDataSource = ByteArrayDataSource(bytes, uri.getType())
        attachmentBodyPart.dataHandler = DataHandler(bytesDataSource)
        attachmentBodyPart.fileName = uri.filename
        return attachmentBodyPart
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