package com.example.a7minuteworkout

import android.database.sqlite.SQLiteOpenHelper
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.a7minuteworkout.databinding.ActivityFinishBinding
import kotlinx.android.synthetic.main.activity_finish_.*
import java.text.SimpleDateFormat
import java.util.*

class Finish_Activity : AppCompatActivity() {
     private lateinit var binding: ActivityFinishBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFinishBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setSupportActionBar(toolBar_finish_activity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        toolBar_finish_activity.setNavigationOnClickListener{
            onBackPressed()
        }

        binding.finishButton.setOnClickListener{
            finish()
        }

            addDateToDataBase()



    }

    private fun addDateToDataBase(){
        val cal = Calendar.getInstance()
        val dateTIme = cal.time
        Log.i("DATE: ", " " + dateTIme)

        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
        val date = sdf.format(dateTIme)

        val dbHandler = SqliteOpenHelper(this, null)
        dbHandler.addDate(date)
        Log.i("DATE: ", "ADDED")
    }
}