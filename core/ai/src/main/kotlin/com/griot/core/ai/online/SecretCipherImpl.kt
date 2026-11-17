package com.griot.core.ai.online

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Default implementation for [SecretCipher]: AES-GCM with a key of the Android Keystore, created at the first use. The
 * stored text is the IV followed by the encrypted bytes, in Base64.
 */
@Singleton
internal class SecretCipherImpl @Inject constructor() : SecretCipher {
    override suspend fun encrypt(secret: String): String = withContext(Dispatchers.IO) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        Base64.getEncoder().encodeToString(cipher.iv + cipher.doFinal(secret.toByteArray()))
    }

    override suspend fun decrypt(encrypted: String): String? = withContext(Dispatchers.IO) {
        try {
            val bytes = Base64.getDecoder().decode(encrypted)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, bytes, 0, IV_SIZE))
            String(cipher.doFinal(bytes, IV_SIZE, bytes.size - IV_SIZE))
        } catch (e: GeneralSecurityException) {
            Log.w(TAG, "The saved key cannot be read back", e)
            null
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "The saved key is not Base64", e)
            null
        }
    }

    // The Keystore's key, created once.
    private fun key(): SecretKey {
        val store = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (store.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_BITS)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val TAG = "SecretCipher"
        const val KEYSTORE = "AndroidKeyStore"
        const val ALIAS = "griot_online_ai"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_BITS = 256
        const val TAG_BITS = 128
        const val IV_SIZE = 12
    }
}
