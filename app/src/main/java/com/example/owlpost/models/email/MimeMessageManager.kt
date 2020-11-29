package com.example.owlpost.models.email

import com.example.owlpost.models.SendAttachments
import com.example.owlpost.models.UriAttachment
import javax.activation.DataHandler
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource
import kotlin.math.ceil

const val ENCRYPTION_HEADER_NAME = "X-Owl-Encryption"
const val SIGNATURE_HEADER_NAME = "X-Owl-Sign"
const val EXCHANGE_REQUEST_HEADER = "X-Owl-Exchange-Request"
const val EXCHANGE_RESPONSE_HEADER = "X-Owl-Exchange-Response"
const val ENCRYPTION_KEY_EXCHANGE_HEADER_NAME = "X-Owl-Encryption-Exchange"
const val SIGNATURE_KEY_EXCHANGE_HEADER_NAME = "X-Owl-Sign-Exchange"
const val EXCHANGE_REQUEST_SUBJECT = "OwlPost exchange request!"
const val EXCHANGE_RESPONSE_SUBJECT = "OwlPost exchange response!"

class MimeMessageManager {
    fun buildMimeMessage(
        message: MimeMessage,
        from: String,
        recipient: String,
        subject: String,
        attachments: SendAttachments,
        plainText: String,
        html: String
    ) {
        // init primary message fields
        message.setFrom(InternetAddress(from))
        message.setRecipient(Message.RecipientType.TO, InternetAddress(recipient))
        message.subject = subject
        val multiPart = MimeMultipart() // message multipart
        // set text into alternative multipart (plain and html)
        val textMultipart = MimeMultipart("alternative")
        val plainTextBodyPart = buildPlainTextBodyPart(plainText)
        textMultipart.addBodyPart(plainTextBodyPart)
        val htmlBodyPart = buildHTMLBodyPart(html)
        textMultipart.addBodyPart(htmlBodyPart)
        //add text into message multipart
        val textMultipartWrapper = MimeBodyPart()
        textMultipartWrapper.setContent(textMultipart)
        multiPart.addBodyPart(textMultipartWrapper)
        // add attachments
        for (i in 0 until attachments.size) {
            val attachmentBodyPart = buildAttachmentBodyPart(attachments[i])
            multiPart.addBodyPart(attachmentBodyPart)
        }
        message.setContent(multiPart)
    }

    private fun buildPlainTextBodyPart(text: String): MimeBodyPart {
        val bodyPart = MimeBodyPart()
        bodyPart.setContent(text, "text/plain; charset=utf-8")
        bodyPart.setHeader("Content-Transfer-Encoding", "base64")
        return bodyPart
    }

    private fun buildHTMLBodyPart(text: String): MimeBodyPart {
        val bodyPart = MimeBodyPart()
        bodyPart.setContent(text, "text/html; charset=utf-8")
        bodyPart.setHeader("Content-Transfer-Encoding", "base64")
        return bodyPart
    }

    private fun buildAttachmentBodyPart(uri: UriAttachment): MimeBodyPart {
        val fis = uri.getInputStream()
        val attachmentBodyPart = MimeBodyPart()
        val bytesDataSource = ByteArrayDataSource(fis?.readBytes(), uri.getType())
        fis?.close()
        attachmentBodyPart.dataHandler = DataHandler(bytesDataSource)
        attachmentBodyPart.fileName = uri.filename
        return attachmentBodyPart
    }

    fun parseText(message: MimeMessage, mimeType: String): String {
        var result = ""
        when {
            message.isMimeType(mimeType) ->
                result += message.content.toString()
            message.isMimeType("multipart/*") ->
                result += parseBodyPartsText(message.content as MimeMultipart, mimeType)
        }
        return result
    }

    private fun parseBodyPartsText(multipart: MimeMultipart, mimeType: String): String{
        var result = ""
        try{
            for (i in 0 until multipart.count){
                val bodyPart = multipart.getBodyPart(i)
                if (bodyPart.isMimeType(mimeType))
                    result += bodyPart.content.toString()
                else if (bodyPart.isMimeType("multipart/*"))
                    result += parseBodyPartsText(bodyPart.content as MimeMultipart, mimeType)
            }
        }
        catch (e: MessagingException) { /*Missing start bounds (no body parts)*/ }
        return result
    }

    fun parseTextParts(message: MimeMessage, mimeType: String): Array<Part> {
        val result = ArrayList<Part>()
        when {
            message.isMimeType(mimeType) ->{
                println(message.contentType)
                println("Само сообщение текст!!")
                result.add(message)
            }
            message.isMimeType("multipart/*") ->
                result.addAll(parseBodyPartTextParts(message.content as MimeMultipart, mimeType))
        }
        return result.toTypedArray()
    }

    private fun parseBodyPartTextParts(multipart: MimeMultipart, mimeType: String): Array<Part>{
        val result = ArrayList<Part>()
        try{
            for (i in 0 until multipart.count){
                val bodyPart = multipart.getBodyPart(i)
                if (bodyPart.isMimeType(mimeType))
                    result.add(bodyPart)
                else if (bodyPart.isMimeType("multipart/*"))
                    result.addAll(parseBodyPartTextParts(bodyPart.content as MimeMultipart, mimeType))
            }
        }
        catch (e: MessagingException) { /*Missing start bounds (no body parts)*/ }
        return result.toTypedArray()
    }

    fun parseAttachmentsParts(message: MimeMessage): Array<BodyPart>{
        val result = ArrayList<BodyPart>()
        if (message.isMimeType("multipart/*")){
            val multipart = message.content as MimeMultipart
            try{
                for (i in 0 until multipart.count){
                    val bodyPart = multipart.getBodyPart(i)
                    if (Part.ATTACHMENT.equals(bodyPart.disposition, true))
                        result.add(bodyPart)
                }
            }
            catch (e: MessagingException){ /*Missing start bounds (no body parts)*/ }
        }
        return result.toTypedArray()
    }

    fun getBytesToSign(message: MimeMessage): ByteArray {
        var byteToSign = ByteArray(0)
        var parts = parseTextParts(message, "text/plain")
        parts.forEach { part ->
            byteToSign += part.content.toString().toByteArray(Charsets.UTF_8)
        }
        parts = parseTextParts(message, "text/html")
        parts.forEach { part ->
            byteToSign += part.content.toString().toByteArray(Charsets.UTF_8)
        }
        parseAttachmentsParts(message).forEach { part ->
            byteToSign += part.inputStream.readBytes()
        }
        return byteToSign
    }

    fun parseFlagsToInt(flags: Flags): Int{
        var flagsBits = 0
        for ((flag, bits) in FLAGS_BITS){
            if (flags.contains(flag))
                flagsBits = flagsBits or bits
        }
        return flagsBits
    }

    fun parseIntToFlags(flagsBits: Int): Flags {
        val flags = Flags()
        for ((flag, bits) in FLAGS_BITS){
            if (flagsBits and bits != 0)
                flags.add(flag)
        }
        return flags
    }
}