package com.example.owlpost.models.email


import android.net.Uri
import com.example.owlpost.models.FilenameAttachment
import java.io.File
import java.io.FilenameFilter
import java.io.InputStream
import java.lang.NullPointerException
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.internet.MimeUtility
import kotlin.collections.ArrayList


val FLAGS_BITS = mapOf<Flags.Flag, Int>(
    Pair(Flags.Flag.ANSWERED, 0x01),
    Pair(Flags.Flag.DELETED, 0x02),
    Pair(Flags.Flag.DRAFT, 0x04),
    Pair(Flags.Flag.FLAGGED, 0x08),
    Pair(Flags.Flag.RECENT, 0x10),
    Pair(Flags.Flag.SEEN, 0x20)
)

class OwlMessage {
    val uid: Long
    private val folderName: String

    val from: String get() = (message.from[0] as InternetAddress).address
    val subject: String get() = message.subject ?: ""
    val date: Date get() = message.sentDate ?: Date()
    val seen: Boolean get() = message.isSet(Flags.Flag.SEEN)
    val encrypted: Boolean get() = message.getHeader("owl_encrypted") != null
    val signed: Boolean get() = message.getHeader("owl_sign") != null
    val text get() = parseText("text/plain")
    val html get() = parseText("text/html")
    val attachmentsFilenames get() = parseAttachmentsFilenames()
    private val message: MimeMessage

    val to: Array<String> get() {
        return try{
            Array(message.getRecipients(Message.RecipientType.TO).size) { i ->
                (message.getRecipients(Message.RecipientType.TO)[i] as InternetAddress).address
            }
        } catch (e: NullPointerException){
            arrayOf()
        }
    }

    constructor(_uid: Long, _folderName: String, _message: MimeMessage){
        uid = _uid
        folderName = _folderName
        message = _message
    }

    constructor(path: String, _folderName: String, _uid: Long){
        val file = File("$path/$_folderName/${_uid}")
        val fis = file.inputStream()
        val session = Session.getInstance(Properties())
        val flagsBits = ByteArray(1)
        fis.read(flagsBits)
        val flags = parseIntToFlags(flagsBits[0].toUByte().toInt())
        uid = _uid
        folderName = _folderName
        message = MimeMessage(session, fis)
        message.setFlags(flags, true)
    }

    fun writeTo(path: String) {
        val fos = File("$path/$folderName/$uid").outputStream()
        val flagsBits = parseFlagsToInt(message.flags)
        fos.write(flagsBits)
        message.writeTo(fos)
    }

    fun getAttachmentInputStream(filename: String): InputStream? {
        if (message.isMimeType("multipart/*")){
            val multipart = message.content as MimeMultipart
            for (i in 0 until multipart.count){
                val bodyPart = multipart.getBodyPart(i)
                if (Part.ATTACHMENT.equals(bodyPart.disposition, true) && MimeUtility.decodeText(bodyPart.fileName) == filename)
                    return bodyPart.inputStream
            }
        }
        return null
    }

    private fun parseAttachmentsFilenames(): Array<FilenameAttachment>{
        val result = ArrayList<FilenameAttachment>()
        if (message.isMimeType("multipart/*")){
            val multipart = message.content as MimeMultipart
            for (i in 0 until multipart.count){
                val bodyPart = multipart.getBodyPart(i)
                if (Part.ATTACHMENT.equals(bodyPart.disposition, true))
                    result.add(FilenameAttachment(
                        MimeUtility.decodeText(bodyPart.fileName),
                        bodyPart.contentType.substring(0 until bodyPart.contentType.indexOf(";"))
                    ))
            }
        }
        return result.toTypedArray()
    }

    private fun parseText(mimeType: String): String {
        var result = ""
        when {
            message.isMimeType(mimeType) ->
                result += message.content.toString() + "\n"
            message.isMimeType("multipart/*") ->
                result += parseMultipartText(message.content as MimeMultipart, mimeType) + "\n"
        }
        return result
    }

    private fun parseMultipartText(multipart: MimeMultipart, mimeType: String): String{
        var result = ""
        for (i in 0 until multipart.count){
            val bodyPart = multipart.getBodyPart(i)
            if (bodyPart.isMimeType(mimeType))
                result += bodyPart.content.toString() + "\n"
            else if (bodyPart.isMimeType("multipart/*"))
                result += parseMultipartText(bodyPart.content as MimeMultipart, mimeType) + "\n"
        }
        return result
    }

    private fun parseFlagsToInt(flags: Flags): Int{
        var result = 0
        for ((flag, bits) in FLAGS_BITS){
            if (flags.contains(flag))
                result = result or bits
        }
        return result
    }

    private fun parseIntToFlags(flagsBits: Int): Flags{
        val result = Flags()
        for ((flag, bits) in FLAGS_BITS){
            if (flagsBits and bits != 0)
                result.add(flag)
        }
        return result
    }
}