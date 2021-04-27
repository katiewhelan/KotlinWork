package com.trello.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.trello.R
import com.trello.databinding.ActivitySignInBinding
import com.trello.models.User


class SignInActivity : BaseActivity() {
    private lateinit var binding : ActivitySignInBinding
    private lateinit var auth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setFullScreen()
        setUpActionBar()
        //isUserLoggedIn()

        binding.btnSignIn.setOnClickListener {
            logInUser()
        }
    }

    private fun isUserLoggedIn(){
        val currentUser = auth.currentUser
        if(currentUser != null){
            Toast.makeText(this, "logged in", Toast.LENGTH_SHORT).show()
        } else{
           // logInUser()
        }

    }

    private fun setFullScreen(){
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
    }

    private fun logInUser(){
        val password: String = binding.etPassword.text.toString().trim{ it <= ' '}
        val email: String = binding.etEmail.text.toString().trim{ it <= ' '}

        if(validateSignInForm(email,password)){
            showProgressDialog(resources.getString(R.string.please_wait))


        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                hideProgressBarDialog()
                if (task.isSuccessful) {
                    Log.d("Sign In", "signInWithEmail:success")
                    val user = auth.currentUser
                    startActivity(Intent(this,MainActivity::class.java))
                    Toast.makeText(baseContext, "Authentication passed",
                        Toast.LENGTH_SHORT).show()
                    //updateUI(user)
                } else {

                    Log.w("sign in", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                   // updateUI(null)
                    // ...
                }

                // ...
            }
        }

    }

    private fun validateSignInForm(email: String, password:String) : Boolean {
        return when {
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

    fun signInSuccess(user: User){
        hideProgressBarDialog()
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }


    private fun setUpActionBar(){
        setSupportActionBar(binding.toolbarSignInActivity)

        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        binding.toolbarSignInActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }





}