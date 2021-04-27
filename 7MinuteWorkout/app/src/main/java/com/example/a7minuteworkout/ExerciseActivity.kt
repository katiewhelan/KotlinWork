package com.example.a7minuteworkout

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a7minuteworkout.databinding.ActivityExerciseBinding
import kotlinx.android.synthetic.main.dialogcustomback.*
import java.util.*
import kotlin.collections.ArrayList

class ExerciseActivity : AppCompatActivity() , TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityExerciseBinding
    private var restTimer : CountDownTimer? = null
    private var restProgress = 0
    private var exerciseProgress = 0
    private var exerciseTimer : CountDownTimer? = null
    private var exerciseList : ArrayList<ExerciseModel>? = null
    private var currentExercisePosition : Int = -1
    private var tts: TextToSpeech? = null
    private var player: MediaPlayer? = null
    private var exerciseStatusAdapter : ExerciseStatusAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolBar)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        binding.toolBar.setNavigationOnClickListener{
            customDialogForBack()
        }

        tts = TextToSpeech(this, this )
        exerciseList = Constants.defaultExerciseList()
        setUpRestView()
        setUpExerciseStatusRecyclerView()

    }

    private fun setExerciseProgressBar(){
        binding.exerciseProgressBar.progress = exerciseProgress
        exerciseTimer = object: CountDownTimer(3000,1000){
            override fun onTick(millisUntilFinished: Long) {
                exerciseProgress++
                binding.exerciseProgressBar.progress = 3 - exerciseProgress
                binding.tvExerciseTimer.text = (3-exerciseProgress).toString()
            }

            override fun onFinish() {
                exerciseList!![currentExercisePosition].setIsCompleted(true)
                exerciseList!![currentExercisePosition].setIsSelected(false)
                exerciseStatusAdapter!!.notifyDataSetChanged()
                if (currentExercisePosition < exerciseList?.size!! - 1) {

                    setUpRestView()
                } else {
                    finish()
                    val intent = Intent(this@ExerciseActivity, Finish_Activity::class.java)
                    startActivity(intent)
                }
            }
        }.start()


    }

    private fun setRestProgressBar(){
        binding.progressBar.progress = restProgress
        restTimer = object: CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                restProgress++
                binding.progressBar.progress = 2 - restProgress
                binding.tvTimer.text = (2 - restProgress).toString()
            }


            override fun onFinish() {
               // Toast.makeText(this@ExerciseActivity, "Start exercise", Toast.LENGTH_SHORT).show()
                currentExercisePosition++

                exerciseList!![currentExercisePosition].setIsSelected(true)
                exerciseStatusAdapter!!.notifyDataSetChanged()

               setUpExerciseView()
            }


        }.start()
    }

    override fun onDestroy() {
        if(restTimer != null){
            restTimer!!.cancel()
            restProgress =  0
        }
        if(tts != null ){
            tts!!.stop()
            tts!!.shutdown()

        }
        if(exerciseTimer !=null){
            exerciseTimer!!.cancel()
            exerciseProgress = 0
        }

        if(player != null){
            player!!.stop()
        }
        super.onDestroy()
    }

    private fun setUpRestView(){
        try {
            player = MediaPlayer.create(applicationContext, R.raw.press_start)
            player!!.isLooping = false
            player!!.start()

        }catch (e :Exception){
            e.printStackTrace()
        }
        binding.llRestView.visibility = View.VISIBLE
        binding.llExerciseView.visibility = View.GONE
        if(restTimer != null){
            restTimer!!.cancel()
            restProgress = 0
        }
        setRestProgressBar()
        binding.tvUpcomingExercise.setText(exerciseList!![currentExercisePosition+1].getName())

    }



    private fun setUpExerciseView(){
        speakOut(exerciseList!![currentExercisePosition].getName())
        binding.llRestView.visibility = View.GONE
        binding.llExerciseView.visibility = View.VISIBLE


        if(exerciseTimer != null){
            exerciseTimer!!.cancel()
            exerciseProgress = 0
        }
        setExerciseProgressBar()
         binding.imageView.setImageResource(exerciseList!![currentExercisePosition].getImage())
         binding.tvExerciseName.setText(exerciseList!![currentExercisePosition].getName())

    }
    override fun onInit(status: Int) {
        if(status == TextToSpeech.SUCCESS){
            val result = tts!!.setLanguage(Locale.US)
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TTS", "Not Supported")
            } else {
                Log.e("TTS", "Init Failed")
            }


        }
    }

    private fun setUpExerciseStatusRecyclerView(){
        binding.rvExerciseStatus.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        exerciseStatusAdapter = ExerciseStatusAdapter(exerciseList!!, this)
        binding.rvExerciseStatus.adapter = exerciseStatusAdapter
    }

    private fun speakOut(text: String){
        tts!!.speak(text,TextToSpeech.QUEUE_FLUSH, null, " ")
    }

    private fun customDialogForBack(){
        var customDialog = Dialog(this)

        customDialog.setContentView(R.layout.dialogcustomback)

        customDialog.btYes.setOnClickListener {
            finish()
            customDialog.dismiss()
        }
        customDialog.btNo.setOnClickListener{
            customDialog.dismiss()
        }
        customDialog.show()

    }
}