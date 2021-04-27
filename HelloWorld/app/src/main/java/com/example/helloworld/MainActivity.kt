package com.example.helloworld

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.helloworld.databinding.ActivityMainBinding
//import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

            var timesClicked = 0

        binding.button.setOnClickListener {
            timesClicked += 1
            binding.text123.text = timesClicked.toString()
            //text123.text
            Toast.makeText(this@MainActivity, "Hello Katie ", Toast.LENGTH_SHORT).show()
        }
    }
}
