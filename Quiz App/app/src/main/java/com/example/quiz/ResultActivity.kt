package com.example.quiz

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

import com.example.quiz.databinding.ActivityResultBinding





class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userName = intent.getStringArrayExtra(Constants.USER_NAME)

        val totalCorrect = intent.getStringArrayExtra(Constants.Correct_Answers)
        val totalQuestions = intent.getStringArrayExtra(Constants.Total_Questions)

        Toast.makeText(this, "name: $userName", Toast.LENGTH_SHORT)

        binding.tvName.setText("$userName")
        binding.tvScore.setText("Your Score is  $totalCorrect out of $totalQuestions")

        binding.btnFinish.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }




    }
}