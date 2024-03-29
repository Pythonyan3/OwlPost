package com.example.owlpost.models.email


import com.example.owlpost.models.cryptography.OwlCryptoManager
import com.example.owlpost.models.cryptography.OwlKeysManager
import com.example.owlpost.models.cryptography.SYMMETRIC_KEY_SIZE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.security.PrivateKey
import java.security.PublicKey
import java.util.*
import javax.activation.DataHandler
import javax.mail.Flags
import javax.mail.Message
import javax.mail.Session
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
    private val path: String
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
        get() = message.getHeader(EXCHANGE_REQUEST_HEADER) != null
    val isExchangeResponse: Boolean
        get() = message.getHeader(EXCHANGE_RESPONSE_HEADER) != null
    val exchangeEncryptionKey: String
        get() = message.getHeader(ENCRYPTION_KEY_EXCHANGE_HEADER_NAME)[0]
    val exchangeSignKey: String
        get() = message.getHeader(SIGNATURE_KEY_EXCHANGE_HEADER_NAME)[0]


    val text: String
        get() = mimeManager.parseText(message, PLAIN_MIME_TYPE)
    val html: String
        get() = mimeManager.parseText(message, HTML_MIME_TYPE)
    val attachmentParts
        get() = mimeManager.parseAttachmentsParts(message)
    var flags: Flags
        get() = message.flags
        set(value) {
            message.setFlags(message.flags, false)
            message.setFlags(value, true)
        }

    val to: Array<String>
        get() {
            return try {
                Array(message.getRecipients(Message.RecipientType.TO).size) { i ->
                    (message.getRecipients(Message.RecipientType.TO)[i] as InternetAddress).address
                }
            } catch (e: NullPointerException) {
                arrayOf()
            }
        }

    constructor(_path: String, _message: MimeMessage) {
        path = _path
        message = _message
    }

    constructor(_path: String, _uid: Long, _folderName: String, _message: MimeMessage) {
        uid = _uid
        folderName = _folderName
        message = _message
        path = _path
    }

    constructor(_path: String, _folderName: String, _uid: Long) {
        uid = _uid
        folderName = _folderName
        path = _path
        val file = File("$path/$_folderName/${_uid}")
        val fis = file.inputStream()
        val session = Session.getInstance(Properties())
        val flagsBits = ByteArray(1)
        fis.read(flagsBits)
        val flags = mimeManager.parseIntToFlags(flagsBits[0].toUByte().toInt())
        message = MimeMessage(session, fis)
        message.setFlags(flags, true)
    }

    fun writeTo() {
        val fos = File("$path/$folderName/$uid").outputStream()
        val flagsBits = mimeManager.parseFlagsToInt(message.flags)
        fos.write(flagsBits)
        message.writeTo(fos)
    }

    fun setFlag(flag: Flags.Flag) {
        message.setFlag(flag, true)
    }

    fun saveFlags() {
        val file = RandomAccessFile("$path/$folderName/$uid", "rw")
        val flagsBits = mimeManager.parseFlagsToInt(message.flags)
        file.write(flagsBits)
        file.close()
    }

    suspend fun encrypt(publicKey: PublicKey) {
        val cryptoManager = OwlCryptoManager()
        val keyManager = OwlKeysManager()
        var textSymmetricKeyString = ""
        // encrypt text
        withContext(Dispatchers.IO) {
            message.saveChanges()
            if (text.isNotEmpty()) {
                val secretKey = keyManager.generateSecretKey()
                mimeManager.setText(message, cryptoManager.encrypt(text, secretKey), PLAIN_MIME_TYPE)
                mimeManager.setText(message, cryptoManager.encrypt(html, secretKey), HTML_MIME_TYPE)
                textSymmetricKeyString = cryptoManager.encryptKeyBase64(secretKey, publicKey)
            }
            // encrypt attachments
            val attachments = attachmentParts
            attachments.forEach { part ->
                val secretKey = keyManager.generateSecretKey()
                val encryptedBytes = cryptoManager.encrypt(part.inputStream.readBytes(), secretKey)
                val encryptedKey = cryptoManager.encryptKey(secretKey, publicKey)
                val bytesDataSource =
                    ByteArrayDataSource(encryptedKey + encryptedBytes, part.contentType)
                part.dataHandler = DataHandler(bytesDataSource)
            }
            // set encrypted header with key
            if (attachments.isNotEmpty() || text.isNotEmpty())
                message.setHeader(ENCRYPTION_HEADER_NAME, textSymmetricKeyString)
        }
    }

    suspend fun decrypt(privateKey: PrivateKey) {
        val cryptoManager = OwlCryptoManager()
        // decrypt text
        withContext(Dispatchers.IO) {
            if (text.isNotEmpty()) {
                val encryptedKey = message.getHeader(ENCRYPTION_HEADER_NAME)[0]
                val secretKey = cryptoManager.decryptKey(encryptedKey, privateKey)
                mimeManager.setText(message, cryptoManager.decrypt(text, secretKey), PLAIN_MIME_TYPE)
                mimeManager.setText(message, cryptoManager.decrypt(html, secretKey), HTML_MIME_TYPE)
            }
            // decrypt attachments
            val attachments = attachmentParts
            attachments.forEach { part ->
                val encryptedBytes = part.inputStream.readBytes()
                val secretKey = cryptoManager.decryptKey(
                    encryptedBytes.sliceArray(0 until SYMMETRIC_KEY_SIZE),
                    privateKey
                )
                val bytesDataSource = ByteArrayDataSource(
                    cryptoManager.decrypt(
                        encryptedBytes.sliceArray(SYMMETRIC_KEY_SIZE until encryptedBytes.size),
                        secretKey
                    ),
                    part.contentType
                )
                part.dataHandler = DataHandler(bytesDataSource)
            }
        }
    }

    suspend fun sign(privateKey: PrivateKey) {
        withContext(Dispatchers.IO) {
            val cryptoManager = OwlCryptoManager()
            message.setHeader(
                SIGNATURE_HEADER_NAME,
                cryptoManager.sign(mimeManager.getBytesToSign(message), privateKey)
            )
        }
    }

    suspend fun verify(publicKey: PublicKey): Boolean {
        var result = false
        withContext(Dispatchers.IO) {
            val cryptoManager = OwlCryptoManager()
            val digest = message.getHeader(SIGNATURE_HEADER_NAME)[0]
            result = cryptoManager.verify(mimeManager.getBytesToSign(message), digest, publicKey)
        }
        return result
    }

    fun copy(): OwlMessage {
        return OwlMessage(path, folderName, uid)
    }

}