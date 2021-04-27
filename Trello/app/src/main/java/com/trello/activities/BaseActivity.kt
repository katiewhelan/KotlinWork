package com.trello.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.trello.R
import com.trello.databinding.ActivityBaseBinding


open class BaseActivity : AppCompatActivity() {

    private lateinit var binding : ActivityBaseBinding
    private var doubleBackToExitPressedOnce = false
    private lateinit var mProgressDialog : Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun showProgressDialog(text: String){
        mProgressDialog = Dialog(this)
        mProgressDialog.setContentView(R.layout.dialog_progress)
//        HOW to use this with binding
       //mProgressDialog.tv_progress_text.text = text

    }

    fun hideProgressBarDialog(){
        mProgressDialog.dismiss()
    }

    fun getCurrentUserId() : String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun doubleBackToExit(){
        if(doubleBackToExitPressedOnce){
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true

        Toast.makeText(this,resources.getString(R.string.please_click_back_again_to_exit), Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        },2000)
    }

    fun showErrorSnackBar(message: String){
    val sb = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val sbv = sb.view
        sbv.setBackgroundColor(ContextCompat.getColor(this,R.color.snackbar_error_color))
        sb.show()


    }

}