package com.example.owlpost.models.cryptography

import android.util.Base64
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec


const val ASYMMETRIC_ENCRYPT_ALGORITHM = "RSA"
const val SYMMETRIC_ENCRYPT_ALGORITHM = "AES"
const val ENCRYPT_KEY = 0x01
const val SIGN_KEY = 0x02
const val PUBLIC_KEY = 0x04
const val PRIVATE_KEY = 0x08


class KeysManager{
    fun generateKeysPair(algorithm: String = ASYMMETRIC_ENCRYPT_ALGORITHM): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(algorithm)
        keyPairGenerator.initialize(2048)
        return keyPairGenerator.generateKeyPair()
    }

    fun encodeKeyToBase64String(key: PublicKey): String{
        val keySpec = X509EncodedKeySpec(key.encoded)
        return Base64.encodeToString(keySpec.encoded, Base64.DEFAULT)
    }

    fun encodeKeyToBase64String(key: PrivateKey): String{
        val keySpec = PKCS8EncodedKeySpec(key.encoded)
        return Base64.encodeToString(keySpec.encoded, Base64.DEFAULT)
    }

    fun publicKeyBase64StringDecode(base64String: String, algorithm: String): PublicKey {
        val bytes = Base64.decode(base64String.toByteArray(), Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(bytes)
        val keyFactory = KeyFactory.getInstance(algorithm)
        return keyFactory.generatePublic(keySpec)
    }

    fun privateKeyBase64StringDecode(base64String: String, algorithm: String): PrivateKey {
        val bytes = Base64.decode(base64String.toByteArray(), Base64.DEFAULT)
        val keySpec = PKCS8EncodedKeySpec(bytes)
        val keyFactory = KeyFactory.getInstance(algorithm)
        return keyFactory.generatePrivate(keySpec)
    }
}