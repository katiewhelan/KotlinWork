package com.trello.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.trello.R
import com.trello.databinding.ActivitySignUpBinding
import com.trello.firbase.FireStoreClass
import com.trello.models.User

class SignUpActivity : BaseActivity() {
    private lateinit var binding : ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(
                WindowInsets.Type.statusBars() or
                        WindowInsets.Type.navigationBars())

        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        setUpActionBar()
    }

    private fun setUpActionBar(){
        setSupportActionBar(binding.toolbarSignUpActivity)

        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        binding.toolbarSignUpActivity.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.btnSignUp.setOnClickListener{
            registerUser()
        }
    }

    fun userRegisteredSuccess(){
        Toast.makeText(this, "userRegistered", Toast.LENGTH_SHORT).show()
        hideProgressBarDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun registerUser(){
        val name: String = binding.etName.text.toString().trim{ it <= ' '}
        val password: String = binding.etPassword.text.toString().trim{ it <= ' '}
        val email: String = binding.etEmail.text.toString().trim{ it <= ' '}

        if(validateForm(name,email,password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password).addOnCompleteListener(
               OnCompleteListener<AuthResult> { task ->
                        if(task.isSuccessful){
                            val firebaseUser : FirebaseUser = task.result!!.user!!
                            val registeredEmail = firebaseUser.email!!
                            val user = User(firebaseUser.uid, name, registeredEmail)
                            FireStoreClass().registerUser(this, user)


                        }else {
                            Toast.makeText(this, "Problem making user", Toast.LENGTH_LONG).show()
                        }
               })
//            Toast.makeText(this@SignUpActivity, "User Registered", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateForm(name:String, email: String, password:String) : Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter a name")
                false
            }

            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter an email address")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password")
                false
            }
            else -> {
                true
            }
        }
    }
}