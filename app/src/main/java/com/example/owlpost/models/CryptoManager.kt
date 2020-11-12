package com.example.owlpost.models

import android.content.Context
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import android.util.Base64

class CryptoManager(private val algorithm: String = "RSA") {
    fun generateKeysPair(): KeyPair{
        val keyPairGenerator = KeyPairGenerator.getInstance(algorithm)
        keyPairGenerator.initialize(2048)
        return keyPairGenerator.generateKeyPair()
    }

    fun encodeKeyToSpec(key: PublicKey): X509EncodedKeySpec{
        return X509EncodedKeySpec(key.encoded)
    }

    fun encodeKeyToSpec(key: PrivateKey): PKCS8EncodedKeySpec{
        return PKCS8EncodedKeySpec(key.encoded)
    }

    fun encodeKeyToBase64String(key: PublicKey): String{
        val keySpec = X509EncodedKeySpec(key.encoded)
        return Base64.encodeToString(keySpec.encoded, Base64.DEFAULT)
    }

    fun encodeKeyToBase64String(key: PrivateKey): String{
        val keySpec = PKCS8EncodedKeySpec(key.encoded)
        return Base64.encodeToString(keySpec.encoded, Base64.DEFAULT)
    }

    fun publicKeySpecDecode(bytes: ByteArray): PublicKey{
        val keySpec = X509EncodedKeySpec(bytes)
        val keyFactory = KeyFactory.getInstance(algorithm)
        return keyFactory.generatePublic(keySpec)
    }

    fun privateKeySpecDecode(bytes: ByteArray): PrivateKey{
        val keySpec = PKCS8EncodedKeySpec(bytes)
        val keyFactory = KeyFactory.getInstance(algorithm)
        return keyFactory.generatePrivate(keySpec)
    }

    fun publicKeyBase64StringDecode(base64String: String): PublicKey{
        val bytes = Base64.decode(base64String.toByteArray(), Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(bytes)
        val keyFactory = KeyFactory.getInstance(algorithm)
        return keyFactory.generatePublic(keySpec)
    }

    fun privateKeyBase64StringDecode(base64String: String): PrivateKey{
        val bytes = Base64.decode(base64String.toByteArray(), Base64.DEFAULT)
        val keySpec = PKCS8EncodedKeySpec(bytes)
        val keyFactory = KeyFactory.getInstance(algorithm)
        return keyFactory.generatePrivate(keySpec)
    }
}