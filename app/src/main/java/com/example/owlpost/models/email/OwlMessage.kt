package com.example.owlpost.models.email


import java.io.File
import java.util.*
import javax.mail.Flags
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


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
    val subject: String get() = message.subject ?: "(No Subject)"
    val from: String get() = (message.from[0] as InternetAddress).address
    //val to = message.allRecipients
    val date: Date get() = message.sentDate ?: Date()
    val seen: Boolean get() = message.isSet(Flags.Flag.SEEN)
    private val message: MimeMessage

    constructor(_uid: Long, _folderName: String, _message: MimeMessage){
        uid = _uid
        folderName = _folderName
        message = _message
    }

    constructor(path: String, _folderName: String, _uid: Long){
        val file = File("$path/$_folderName/${_uid}")
        val fis = file.inputStream()
        val properties = Properties()
        val session = Session.getInstance(properties)
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