package com.example.firebasephoneauth

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.example.firebasephoneauth.databinding.ActivityMainBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.ActionCodeSettings.newBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider.newBuilder
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.nio.file.attribute.AclEntry.newBuilder
import java.util.concurrent.TimeUnit



class MainActivity : AppCompatActivity()
{

    //view binding
    private lateinit var binding: ActivityMainBinding

    private var forceResendingToken: PhoneAuthProvider.ForceResendingToken?=null

    private var mCallback: PhoneAuthProvider.OnVerificationStateChangedCallbacks?=null
    private var mVerificationId: String?=null
    private lateinit var firebaseAuth: FirebaseAuth

    private val Tag ="MAIN_TAG"

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.phoneL.visibility= View.VISIBLE
        binding.codeL.visibility = View.GONE

        firebaseAuth = FirebaseAuth.getInstance()


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        mCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){


            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential)
            }

            }

            fun onVerificationFailed(e: FirebaseException) {
                progressDialog.dismiss()
                Toast.makeText(this@MainActivity, "${e.message}",Toast.LENGTH_SHORT).show()


            }

            fun onCodeSent(verificationId : String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(TAG, "onCodeSent: $verificationId")
                mVerificationId = verificationId
                forceResendingToken = token
                progressDialog.dismiss()

                binding.phoneL . visibility = View.VISIBLE
                binding.codeL.visibility=View.GONE
                Toast.makeText(this@MainActivity,"Verification code sent ....",Toast.LENGTH_SHORT).show()
                binding.codeSentDesc.text= "Please type code ${binding.phoneEt.text.toString().trim()}"
        }
    binding.phoneContinueBtn.setOnClickListener{
    val phone = binding.phoneEt.text.toString().trim()
    if (TextUtils.isEmpty(phone)){
        Toast.makeText(this@MainActivity,"Please enter phone",Toast.LENGTH_SHORT).show()
    }
    else (
            startPhoneNumberVerification(phone)
            )
}

    binding.resendCodeTv.setOnClickListener{
    val phone = binding.phoneEt.text.toString().trim()

    if (TextUtils.isEmpty(phone)){
        Toast.makeText(this@MainActivity, "Please enter code",Toast.LENGTH_SHORT).show()
    }
    else (
            resendVerificationCode(phone, forceResendingToken)
            )

}

    binding.codeSubmitBtn.setOnClickListener{
    val code = binding.codeEt.text.toString().trim()
    if(TextUtils.isEmpty(code)) {
        Toast.makeText(this@MainActivity, "Please enter verification code", Toast.LENGTH_SHORT)
            .show()
    }
    else(
            verifyPhoneNumberWithCode(mVerificationId, code)
            )
}






    }

    private fun startPhoneNumberVerification(phone: String)
    {
        progressDialog.setMessage("Verifying Phone Number ......")
        progressDialog.show()



        val options = PhoneAuthOptions.newBuilder(firebaseAuth)

            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallback)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendVerificationCode(phone: String, token: PhoneAuthProvider.ForceResendingToken?)
    {
        progressDialog.setMessage("Resending code......")
        progressDialog.show()


        val options = PhoneAuthOptions.newBuilder(firebaseAuth)

            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallback)
            .setForceResendingToken(token)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    private fun verifyPhoneNumberWithCode(verificationId: String?, code:String){
        progressDialog.setMessage( "Verifying code...." )
        progressDialog.show()

        val credential= verificationId?.let { PhoneAuthProvider.getCredential(it, code) }
        if (credential != null) {
            signInWithPhoneAuthCredential(credential)
        }

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential){
        progressDialog.setMessage("Logging In")

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                progressDialog.dismiss()
                val phone = firebaseAuth.currentUser?.phoneNumber
                Toast.makeText(this, "Logged In as $phone", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this,ProfileActivity::class.java))
            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this, "${e.message}",Toast.LENGTH_SHORT).show()
    }
}

}


