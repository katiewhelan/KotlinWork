package com.example.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.calculator.databinding.ActivityMainBinding
import java.lang.ArithmeticException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    var lastNumeric = false
    var lastDot = false
    val lastOperation = false
    fun onDigit(view: View){
       // Toast.makeText(this, "Button Works", Toast.LENGTH_LONG).show()
        binding.input.append((view as Button).text)
        lastNumeric = true
    }

    fun onClear(view: View){
        binding.input.setText("")
        lastDot = false
        lastNumeric = false
    }

    fun onDecimal(view: View){
        if(lastNumeric && !lastDot){
            binding.input.append((view as Button).text)
            lastNumeric = false
            lastDot = true
        }
    }

    fun onEquals(view: View){
        if(lastNumeric) {
            var tvValue = binding.input.text.toString()
            var prefix = ""

            if (tvValue.startsWith("-")) {
                prefix = "-"
                tvValue = tvValue.substring(1)
            }
            try {
                if (tvValue.contains("-")) {
                    val splitVal = tvValue.split("-")
                    var right = splitVal[0]
                    var left = splitVal[1]

                    if (!prefix.isEmpty()) {
                        right = prefix + right

                    }

                    binding.input.setText(removeZeroAfterDot((right.toDouble() - left.toDouble()).toString()))

                } else if(tvValue.contains("+")) {
                    val splitVal = tvValue.split("+")
                    var right = splitVal[0]
                    var left = splitVal[1]

                    if (!prefix.isEmpty()) {
                        right = prefix + right

                    }

                    binding.input.setText(removeZeroAfterDot((right.toDouble() + left.toDouble()).toString()))

                }else if(tvValue.contains("*")) {
                        val splitVal = tvValue.split("*")
                        var right = splitVal[0]
                        var left = splitVal[1]

                        if (!prefix.isEmpty()) {
                            right = prefix + right

                        }

                        binding.input.setText(removeZeroAfterDot((right.toDouble() * left.toDouble()).toString()))


                    }else if(tvValue.contains("/")) {
                    val splitVal = tvValue.split("/")
                    var right = splitVal[0]
                    var left = splitVal[1]

                    if (!prefix.isEmpty()) {
                        right = prefix + right

                    }

                    binding.input.setText(removeZeroAfterDot((right.toDouble() / left.toDouble()).toString()))

                }

                }catch(e: ArithmeticException) {
                e.printStackTrace()
            }
        }
    }

    fun onOperator(view: View){
        if(lastNumeric && !isOperatorAdd(binding.input.text.toString())){
            binding.input.append((view as Button).text)
            lastNumeric = false
            lastDot = false
        }
    }

    private fun removeZeroAfterDot(result:String): String{
        var value = result
        if(result.contains(".0")){
            value = result.substring(0,result.length-2)
        }
        return value
    }

    private fun isOperatorAdd(value :String) : Boolean {
        return if(value.startsWith("-")){
            false
        } else{
            value.contains("/") || value.contains("*") || value.contains("+") || value.contains("-")
        }
    }






}