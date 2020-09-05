package com.example.adrian.datesgmb.login

import android.hardware.biometrics.BiometricManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.adrian.datesgmb.MainActivity
import com.example.adrian.datesgmb.databinding.LoginFragmentBinding
import com.example.adrian.datesgmb.ipc.IPCViewModel
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.util.*
import java.util.concurrent.Executor

class LoginFragment: Fragment() {
    private val TAG: String = MainActivity::class.java.getName()
    private val KEY_NAME = UUID.randomUUID().toString()

    private var mToBeSignedMessage: String? = null

    /**
     * Lazily initialize our [IPCViewModel].
     */
    private val viewModel: LoginViewModel by lazy {
        ViewModelProviders.of(this).get(LoginViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = LoginFragmentBinding.inflate(inflater)

        binding.viewModel = viewModel

        binding.register.setOnClickListener {
            if (canAuthenticateWithBiometrics()) {  // Check whether this device can authenticate with biometrics
                Log.i(TAG, "Try registration")
                // Generate keypair and init signature
                val signature: Signature?
                try {
                    val keyPair =
                        generateKeyPair(KEY_NAME, true)
                    // Send public key part of key pair to the server, this public key will be used for authentication
                    mToBeSignedMessage = Base64.encodeToString(
                        keyPair!!.public.encoded,
                        Base64.URL_SAFE
                    ) +
                            ":" +
                            KEY_NAME +
                            ":" +  // Generated by the server to protect against replay attack
                            "12345"
                    signature = initSignature(KEY_NAME)
                } catch (e: java.lang.Exception) {
                    throw java.lang.RuntimeException(e)
                }

                // Create biometricPrompt
                showBiometricPrompt(signature)
            } else {
                // Cannot use biometric prompt
                Toast.makeText(context, "Cannot use biometric", Toast.LENGTH_SHORT).show()
            }
        }

        binding.login.setOnClickListener {
            if (canAuthenticateWithBiometrics()) {  // Check whether this device can authenticate with biometrics
                Log.i(TAG, "Try authentication")

                // Init signature
                val signature: Signature?
                try {
                    // Send key name and challenge to the server, this message will be verified with registered public key on the server
                    mToBeSignedMessage = KEY_NAME +
                            ":" +  // Generated by the server to protect against replay attack
                            "12345"
                    signature = initSignature(KEY_NAME)
                } catch (e: java.lang.Exception) {
                    throw java.lang.RuntimeException(e)
                }

                // Create biometricPrompt
                showBiometricPrompt(signature)
            } else {
                // Cannot use biometric prompt
                Toast.makeText(context, "Cannot use biometric", Toast.LENGTH_SHORT).show()
            }
        }

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        return binding.root
    }

    private fun showBiometricPrompt(signature: Signature?) {
        val authenticationCallback: BiometricPrompt.AuthenticationCallback =
            getAuthenticationCallback()

        // Set prompt info
        val promptInfo = PromptInfo.Builder()
            .setDescription("Description")
            .setTitle("Title")
            .setSubtitle("Subtitle")
            .setNegativeButtonText("Cancel")
            .build()

        // Show biometric prompt
        if (signature != null) {
            Log.i(TAG, "Show biometric prompt")
            activity?.let { BiometricPrompt(it, getMainThreadExecutor(), authenticationCallback) }?.authenticate(
                promptInfo,
                BiometricPrompt.CryptoObject(signature)
            )
        }
    }

    private fun getAuthenticationCallback(): BiometricPrompt.AuthenticationCallback {
        // Callback for biometric authentication result
        return object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence
            ) {
                super.onAuthenticationError(errorCode, errString)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                Log.i(TAG, "onAuthenticationSucceeded")
                super.onAuthenticationSucceeded(result)
                if (result.cryptoObject != null &&
                    result.cryptoObject!!.signature != null
                ) {
                    try {
                        val signature =
                            result.cryptoObject!!.signature
                        signature!!.update(mToBeSignedMessage!!.toByteArray())
                        val signatureString = Base64.encodeToString(
                            signature.sign(),
                            Base64.URL_SAFE
                        )
                        // Normally, ToBeSignedMessage and Signature are sent to the server and then verified
                        Log.i(TAG, "Message: $mToBeSignedMessage")
                        Log.i(TAG, "Signature (Base64 EncodeD): $signatureString")
                        Toast.makeText(context,"$mToBeSignedMessage:$signatureString", Toast.LENGTH_SHORT).show()
                        requireParentFragment().findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToIPCFragment())
                    } catch (e: SignatureException) {
                        throw RuntimeException()
                    }
                } else {
                    // Error
                    Toast.makeText(context, "Something wrong", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }
        }
    }

    @Throws(Exception::class)
    private fun generateKeyPair(
        keyName: String,
        invalidatedByBiometricEnrollment: Boolean
    ): KeyPair? {
        val keyPairGenerator =
            KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                "AndroidKeyStore"
            )
        val builder = KeyGenParameterSpec.Builder(
            keyName,
            KeyProperties.PURPOSE_SIGN
        )
            .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            .setDigests(
                KeyProperties.DIGEST_SHA256,
                KeyProperties.DIGEST_SHA384,
                KeyProperties.DIGEST_SHA512
            ) // Require the user to authenticate with a biometric to authorize every use of the key
            .setUserAuthenticationRequired(true)

        // Generated keys will be invalidated if the biometric templates are added more to user device
        if (Build.VERSION.SDK_INT >= 24) {
            builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment)
        }
        keyPairGenerator.initialize(builder.build())
        return keyPairGenerator.generateKeyPair()
    }

    @Throws(Exception::class)
    private fun getKeyPair(keyName: String): KeyPair? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        if (keyStore.containsAlias(keyName)) {
            // Get public key
            val publicKey = keyStore.getCertificate(keyName).publicKey
            // Get private key
            val privateKey =
                keyStore.getKey(keyName, null) as PrivateKey
            // Return a key pair
            return KeyPair(publicKey, privateKey)
        }
        return null
    }

    @Throws(Exception::class)
    private fun initSignature(keyName: String): Signature? {
        val keyPair = getKeyPair(keyName)
        if (keyPair != null) {
            val signature =
                Signature.getInstance("SHA256withECDSA")
            signature.initSign(keyPair.private)
            return signature
        }
        return null
    }

    private fun getMainThreadExecutor(): Executor {
        return MainThreadExecutor()
    }

    private class MainThreadExecutor : Executor {
        private val handler = Handler(Looper.getMainLooper())
        override fun execute(r: Runnable) {
            handler.post(r)
        }
    }

    /**
     * Indicate whether this device can authenticate the user with biometrics
     * @return true if there are any available biometric sensors and biometrics are enrolled on the device, if not, return false
     */
    private fun canAuthenticateWithBiometrics(): Boolean {
        // Check whether the fingerprint can be used for authentication (Android M to P)
        val fingerprintManagerCompat =
            context?.let { FingerprintManagerCompat.from(it) }
        if (fingerprintManagerCompat != null) {
            return if (Build.VERSION.SDK_INT < 29) {

                fingerprintManagerCompat.hasEnrolledFingerprints() && fingerprintManagerCompat.isHardwareDetected
            } else {    // Check biometric manager (from Android Q)
                val biometricManager: BiometricManager =
                    requireContext().getSystemService<BiometricManager>(
                        BiometricManager::class.java
                    )
                if (biometricManager != null) {
                    biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
                } else false
            }
        } else {
            return false
        }
    }

}