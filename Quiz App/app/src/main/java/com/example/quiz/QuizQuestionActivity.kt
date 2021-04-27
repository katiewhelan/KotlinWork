package com.example.quiz

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.TypefaceCompatApi26Impl
import com.example.quiz.databinding.ActivityQuizQuestionBinding
import com.google.android.material.tabs.TabLayout



class QuizQuestionActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        private const val ResultActivityRequestCode = 1
    }
    private lateinit var binding: ActivityQuizQuestionBinding
    private var mCurrentPosition: Int = 1
    private var mQuestionsList: ArrayList<Question>? = null
    private var mSelectedOptionPosition: Int = 0
    private var mCorrectAnswer = 0
    private var mUserName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Log.i("Question Count ", "${mQuestionsList.size}")
         mQuestionsList = Constants.getQuestions()
        mUserName = intent.getStringExtra(Constants.USER_NAME)


        setQuestion()

        binding.tvOptionOne.setOnClickListener(this)
        binding.tvOptionTwo.setOnClickListener(this)
        binding.tvOptionThree.setOnClickListener(this)
        binding.tvOptionFour.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)


    }

    private fun setQuestion(){
        val question : Question? = mQuestionsList!![mCurrentPosition -1]
        defaultOptionsView()

        if(mCurrentPosition == mQuestionsList!!.size){
            binding.btnSubmit.setText("FINISH")
        }else{
            binding.btnSubmit.setText("SUBMIT")
        }

        binding.progressBar.progress = mCurrentPosition
        binding.tvProgress.setText("$mCurrentPosition" + " /" + binding.progressBar.max.toString())
        binding.ivImage.setImageResource(question!!.image)
        binding.tvQuestion.setText(question!!.question)
        binding.tvOptionOne.setText(question!!.optionOne)
        binding.tvOptionTwo.setText(question!!.optionTwo)
        binding.tvOptionThree.setText(question!!.optionThree)
        binding.tvOptionFour.setText(question!!.optionFour)


    }

    private fun defaultOptionsView(){
        val options =ArrayList<TextView>()
        options.add(0, binding.tvOptionOne)
        options.add(1, binding.tvOptionTwo)
        options.add(2, binding.tvOptionThree)
        options.add(3, binding.tvOptionFour)

        for(option in options){
            option.setTextColor(Color.parseColor("#7A8089"))
            option.typeface = Typeface.DEFAULT
            option.background = ContextCompat.getDrawable(this, R.drawable.default_option_border_bg)


        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_option_One -> {
                selectedOptionsView(binding.tvOptionOne, 1)
            }
            R.id.tv_option_Two -> {
                selectedOptionsView(binding.tvOptionTwo, 2)
            }
            R.id.tv_option_Three -> {
                selectedOptionsView(binding.tvOptionThree, 3)
            }
            R.id.tv_option_Four -> {
                selectedOptionsView(binding.tvOptionFour, 4)
            }

            R.id.btn_submit -> {
                if (mSelectedOptionPosition == 0) {
                    mCurrentPosition++

                    when {
                        mCurrentPosition <= mQuestionsList!!.size -> {
                            setQuestion()
                        }
                        else -> {
                            val intent = Intent(this, ResultActivity:: class.java )
                            intent.putExtra(Constants.USER_NAME, mUserName )
                            intent.putExtra(Constants.Total_Questions, mQuestionsList!!.size)
                            intent.putExtra(Constants.Correct_Answers, mCorrectAnswer)
                            startActivity(intent)
                            //Toast.makeText(this, "end of quiz", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {

                    val question = mQuestionsList?.get(mCurrentPosition - 1)
                    if (question!!.correctAnswer !=mSelectedOptionPosition){
                        answerView(mSelectedOptionPosition, R.drawable.wrong_option_border_bg)
                    }else{
                        mCorrectAnswer++
                    }
                        answerView(question.correctAnswer, R.drawable.correct_option_border_bg)

                    if(mCurrentPosition ==mQuestionsList!!.size){
                        binding.btnSubmit.setText("FINISH")
                    }else {
                        binding.btnSubmit.setText("Go To Next Question")
                    }
                    mSelectedOptionPosition = 0
                }
            }
        }
    }





    private fun answerView(answerInt: Int, drawView: Int) {
        when (answerInt) {
            1 -> {
                binding.tvOptionOne.background = ContextCompat.getDrawable(this, drawView)
            }
            2 -> {
                binding.tvOptionTwo.background = ContextCompat.getDrawable(this, drawView)
            }
            3 -> {
                binding.tvOptionThree.background = ContextCompat.getDrawable(this, drawView)
            }
            4 -> {
                binding.tvOptionFour.background = ContextCompat.getDrawable(this, drawView)
            }
        }
    }





    private fun selectedOptionsView(textView :TextView, selectedOption :Int){
        defaultOptionsView()
        mSelectedOptionPosition = selectedOption
        textView.setTextColor(Color.parseColor("#363A43"))
        textView.setTypeface(textView.typeface, Typeface.BOLD)
        textView.background = ContextCompat.getDrawable(this,R.drawable.selected_option_border_bg)

    }


}

