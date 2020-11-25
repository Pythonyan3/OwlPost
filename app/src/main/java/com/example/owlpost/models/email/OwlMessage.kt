package com.example.owlpost.models.email


import com.example.owlpost.models.cryptography.OwlCryptoManager
import com.example.owlpost.models.cryptography.OwlKeysManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.NullPointerException
import java.security.PrivateKey
import java.security.PublicKey
import java.util.*
import javax.activation.DataHandler
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.util.ByteArrayDataSource


val FLAGS_BITS = mapOf<Flags.Flag, Int>(
    Pair(Flags.Flag.ANSWERED, 0x01),
    Pair(Flags.Flag.DELETED, 0x02),
    Pair(Flags.Flag.DRAFT, 0x04),
    Pair(Flags.Flag.FLAGGED, 0x08),
    Pair(Flags.Flag.RECENT, 0x10),
    Pair(Flags.Flag.SEEN, 0x20)
)

class OwlMessage {
    var uid: Long = -1
    private var folderName: String = "Inbox"
    private val mimeManager = MimeMessageManager()
    val message: MimeMessage

    val from: String
        get() = (message.from[0] as InternetAddress).address
    val subject: String
        get() = message.subject ?: ""
    val date: Date
        get() = message.sentDate ?: Date()
    val seen: Boolean
        get() = message.isSet(Flags.Flag.SEEN)
    val encrypted: Boolean
        get() = message.getHeader(ENCRYPTION_HEADER_NAME) != null
    val signed: Boolean
        get() = message.getHeader(SIGNATURE_HEADER_NAME) != null
    val isExchangeRequest: Boolean
        get() = message.getHeader(EXCHANGE_REQUEST) != null
    val isExchangeResponse: Boolean
        get() = message.getHeader(EXCHANGE_RESPONSE) != null
    val exchangeEncryptionKey: String
        get() = message.getHeader(ENCRYPTION_KEY_EXCHANGE_HEADER_NAME)[0]
    val exchangeSignKey: String
        get() = message.getHeader(SIGNATURE_KEY_EXCHANGE_HEADER_NAME)[0]


    val text: String
        get() = mimeManager.parseText(message, "text/plain")
    val html: String
        get() = mimeManager.parseText(message, "text/html")
    val attachmentParts
        get() = mimeManager.parseAttachmentsParts(message)
    var flags: Flags
        get() = message.flags
        set(value) {
            message.setFlags(message.flags, false)
            message.setFlags(value, true)
        }

    val to: Array<String> get() {
        return try{
            Array(message.getRecipients(Message.RecipientType.TO).size) { i ->
                (message.getRecipients(Message.RecipientType.TO)[i] as InternetAddress).address
            }
        } catch (e: NullPointerException){
            arrayOf()
        }
    }

    constructor(_message: MimeMessage){
        message = _message
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
        val flags = mimeManager.parseIntToFlags(flagsBits[0].toUByte().toInt())
        uid = _uid
        folderName = _folderName
        message = MimeMessage(session, fis)
        message.setFlags(flags, true)
    }

    fun writeTo(path: String) {
        val fos = File("$path/$folderName/$uid").outputStream()
        val flagsBits = mimeManager.parseFlagsToInt(message.flags)
        fos.write(flagsBits)
        message.writeTo(fos)
    }

    suspend fun encrypt(publicKey: PublicKey){
        val cryptoManager = OwlCryptoManager()
        val keyManager = OwlKeysManager()
        var textSymmetricKeyString = ""
        // encrypt text
        withContext(Dispatchers.IO){
            message.saveChanges()
            if (text.isNotEmpty()){
                val secretKey = keyManager.generateSecretKey()
                val plainTextParts = mimeManager.parseTextParts(message, "text/plain")
                plainTextParts.forEach { part ->
                    part.setContent(cryptoManager.encrypt(part.content.toString(), secretKey), "text/plain; charset=utf-8")
                }
                val htmlTextParts = mimeManager.parseTextParts(message, "text/html")
                htmlTextParts.forEach { part ->
                    part.setContent(cryptoManager.encrypt(part.content.toString(), secretKey), "text/html; charset=utf-8")
                }
                textSymmetricKeyString = cryptoManager.encryptKeyBase64(secretKey, publicKey)
            }
            // encrypt attachments
            val attachments = attachmentParts
            attachments.forEach { part ->
                val secretKey = keyManager.generateSecretKey()
                val encryptedBytes = cryptoManager.encrypt(part.inputStream.readBytes(), secretKey)
                val encryptedKey = cryptoManager.encryptKey(secretKey, publicKey)
                val bytesDataSource = ByteArrayDataSource(encryptedKey + encryptedBytes, part.contentType)
                part.dataHandler = DataHandler(bytesDataSource)
            }
            // set encrypted header with key
            if (attachments.isNotEmpty() || text.isNotEmpty())
                message.setHeader( ENCRYPTION_HEADER_NAME, textSymmetricKeyString)
        }
    }

    suspend fun decrypt(privateKey: PrivateKey){
        val cryptoManager = OwlCryptoManager()
        // decrypt text
        withContext(Dispatchers.IO){
            message.saveChanges()
            if (text.isNotEmpty()){
                val encryptedKey = message.getHeader(ENCRYPTION_HEADER_NAME)[0]
                val secretKey = cryptoManager.decryptKey(encryptedKey, privateKey)
                mimeManager.parseTextParts(message, "text/plain").forEach { part ->
                    part.setContent(cryptoManager.decrypt(
                        part.content.toString(),
                        secretKey
                    ), "text/plain; charset=utf-8")
                }
                mimeManager.parseTextParts(message, "text/html").forEach { part ->
                    part.setContent(cryptoManager.decrypt(
                        part.content.toString(),
                        secretKey
                    ), "text/html; charset=utf-8")
                }
            }
            // decrypt attachments
            val attachments = attachmentParts
            attachments.forEach { part ->
                val fis = part.inputStream
                val encryptedSecretKey = ByteArray(256)
                fis.read(encryptedSecretKey)
                val encryptedData = ByteArray(fis.available())
                fis.read(encryptedData)
                val secretKey = cryptoManager.decryptKey(encryptedSecretKey, privateKey)
                val bytesDataSource = ByteArrayDataSource(
                    cryptoManager.decrypt(encryptedData, secretKey),
                    part.contentType
                )
                part.dataHandler = DataHandler(bytesDataSource)
            }
            message.saveChanges()
        }
    }

    suspend fun sign(privateKey: PrivateKey){
        withContext(Dispatchers.IO){
            message.saveChanges()
            val cryptoManager = OwlCryptoManager()
            message.setHeader(
                SIGNATURE_HEADER_NAME,
                cryptoManager.sign(mimeManager.getBytesToSign(message), privateKey)
            )
        }
    }

    suspend fun verify(publicKey: PublicKey): Boolean{
        var result = false
        withContext(Dispatchers.IO){
            val cryptoManager = OwlCryptoManager()
            val digest = message.getHeader(SIGNATURE_HEADER_NAME)[0]
            result = cryptoManager.verify(mimeManager.getBytesToSign(message), digest, publicKey)
        }
        return result
    }

    fun copy(path: String): OwlMessage {
        return OwlMessage(path, folderName, uid)
    }

}