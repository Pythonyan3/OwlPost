package com.example.owlpost.models.cryptography

import android.annotation.SuppressLint
import android.util.Base64
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class OwlCryptoManager(private val asymmetricAlgorithm: String = "RSA", private val symmetricAlgorithm: String = "AES") {
    @SuppressLint("GetInstance")
    fun encrypt(text: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(symmetricAlgorithm)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return Base64.encodeToString(cipher.doFinal(text.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
    }

    @SuppressLint("GetInstance")
    fun encrypt(bytes: ByteArray, secretKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance(symmetricAlgorithm)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return Base64.encode(cipher.doFinal(bytes), Base64.NO_WRAP)
    }

    @SuppressLint("GetInstance")
    fun decrypt(bytes: ByteArray, secretKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance(symmetricAlgorithm)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return cipher.doFinal(Base64.decode(bytes, Base64.NO_WRAP))
    }

    @SuppressLint("GetInstance")
    fun decrypt(data: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(symmetricAlgorithm)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return cipher.doFinal(Base64.decode(data, Base64.NO_WRAP)).toString(Charsets.UTF_8)
    }

    fun encryptKeyBase64(secretKey: SecretKey, publicKey: PublicKey): String{
        val cipher = Cipher.getInstance(asymmetricAlgorithm)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return Base64.encodeToString(cipher.doFinal(secretKey.encoded), Base64.NO_WRAP)
    }

    fun encryptKey(secretKey: SecretKey, publicKey: PublicKey): ByteArray{
        val cipher = Cipher.getInstance(asymmetricAlgorithm)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(secretKey.encoded)
    }

    fun decryptKey(secretKey: String, privateKey: PrivateKey): SecretKey {
        val cipher = Cipher.getInstance(asymmetricAlgorithm)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        return SecretKeySpec(cipher.doFinal(Base64.decode(secretKey, Base64.NO_WRAP)), symmetricAlgorithm)
    }

    fun decryptKey(secretKeyBytes: ByteArray, privateKey: PrivateKey): SecretKey {
        val cipher = Cipher.getInstance(asymmetricAlgorithm)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        return SecretKeySpec(cipher.doFinal(secretKeyBytes), symmetricAlgorithm)
    }

    fun sign(data: ByteArray, privateKey: PrivateKey): String {
        val signature = Signature.getInstance("SHA256WithRSA")
        signature.initSign(privateKey)
        signature.update(data)
        return Base64.encodeToString(signature.sign(), Base64.NO_WRAP)
    }

    fun verify(data: ByteArray, digest: String, publicKey: PublicKey): Boolean {
        val signature = Signature.getInstance("SHA256WithRSA")
        signature.initVerify(publicKey)
        signature.update(data)
        return signature.verify(Base64.decode(digest, Base64.NO_WRAP))
    }
}