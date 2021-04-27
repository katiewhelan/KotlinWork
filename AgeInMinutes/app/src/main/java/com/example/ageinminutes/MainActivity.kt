package com.example.ageinminutes

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.ageinminutes.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnDatePicker.setOnClickListener { view ->
            clickDatePicker(view)
            // Toast.makeText(this, "you clicked a button", Toast.LENGTH_LONG).show()
        }
    }

    fun clickDatePicker(view: View) {

        val myCalendar = Calendar.getInstance()
        val year = myCalendar.get(Calendar.YEAR)
        val month = myCalendar.get(Calendar.MONTH)
        val day = myCalendar.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { view, selectedYear, selectedMonth, selectedDayOfMonth ->
            //Toast.makeText(this, "the chosen year is $selectedYear, month $selectedMonth and day $selectedDayOfMonth", Toast.LENGTH_LONG).show()
            val selectedDateString = "$selectedDayOfMonth/${selectedMonth+1}/$selectedYear"
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate = dateFormat.parse(selectedDateString)
            val selectedDateInMinutes = selectedDate.time/60000
            val currentDate = dateFormat.parse(dateFormat.format(System.currentTimeMillis()))
            val currentDateInMinutes = currentDate!!.time/60000
            val totalMinutes = currentDateInMinutes - selectedDateInMinutes
            val totalDays = totalMinutes / (60 * 24)

            binding.datePickedByUser.setText(selectedDateString)
            binding.minutesToDate.setText(totalMinutes.toString())
            binding.DaysToDate.setText(totalDays.toString())

        }, year, month, day).show()
    }

}


