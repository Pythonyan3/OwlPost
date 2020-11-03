package com.example.owlpost.models

import android.content.Context
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import android.util.Base64

class CipherWrapper(private val algorithm: String = "RSA") {
    fun generateKeysPair(): KeyPair{
        val keyPairGenerator = KeyPairGenerator.getInstance(algorithm)
        keyPairGenerator.initialize(2048)
        return keyPairGenerator.generateKeyPair()
    }

    fun encodeKey(key: PublicKey): String{
        val keySpec = X509EncodedKeySpec(key.encoded)
        return Base64.encodeToString(keySpec.encoded, Base64.DEFAULT)
    }

    fun encodeKey(key: PrivateKey): String{
        val keySpec = PKCS8EncodedKeySpec(key.encoded)
        return Base64.encodeToString(keySpec.encoded, Base64.DEFAULT)
    }

    fun publicKeySpecDecode(base64String: String): PublicKey{
        val bytes = Base64.decode(base64String.toByteArray(), Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(bytes)
        val keyFactory = KeyFactory.getInstance(algorithm)
        return keyFactory.generatePublic(keySpec)
    }

    fun privateKeySpecDecode(base64String: String): PrivateKey{
        val bytes = Base64.decode(base64String.toByteArray(), Base64.DEFAULT)
        val keySpec = PKCS8EncodedKeySpec(bytes)
        val keyFactory = KeyFactory.getInstance(algorithm)
        return keyFactory.generatePrivate(keySpec)
    }
}