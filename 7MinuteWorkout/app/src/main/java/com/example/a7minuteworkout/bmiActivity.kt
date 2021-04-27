package com.example.a7minuteworkout

import androidx.appcompat.app.AppCompatActivity
import com.example.a7minuteworkout.databinding.ActivityBmiBinding
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_bmi.*
import java.math.BigDecimal
import java.math.RoundingMode

class bmiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBmiBinding
    val MetricView = "Metric_Units"
    val USView = "US_Units"
    var currentVeiw = USView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBmiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(toolBar_bmi_activity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = "Calculate BMI"
        } else{
            Toast.makeText(this, "Please give valid info", Toast.LENGTH_SHORT).show()
        }

        toolBar_bmi_activity.setNavigationOnClickListener{
            onBackPressed()
        }

        setUsView()

        binding.rgUnits.setOnCheckedChangeListener { group, checkedId ->
            if(checkedId == R.id.rbUSUnits){
                setUsView()
            } else {
                setMetricView()
            }

        }

        binding.btnCalculateUnits.setOnClickListener{
            var bmi : Float = 0.0F // weight / (height * height)
            var weight : Float = 0.0F
            var height : Float = 0.0F
            if(currentVeiw == MetricView){

            height = binding.etMetricUnitHeight.text.toString().toFloat() / 100
            weight = binding.etMetricUnitWeight.text.toString().toFloat()

            } else {
                height = ((binding.etFeetUnitHeight.text.toString().toFloat() * 12) + binding.etInchUnitHeight.text.toString().toFloat())
                weight = (binding.etUSUnitWeight.text.toString().toFloat() * 703)

            }
            bmi = weight/(height*height)

           validateBmiResults(bmi)
            }
        }

    private fun setMetricView(){
        currentVeiw = MetricView
        binding.tilMetricUnitWeight.visibility = View.VISIBLE
        binding.tilMetricUnitHeight.visibility = View.VISIBLE

        binding.etMetricUnitHeight.text!!.clear()
        binding.etMetricUnitWeight.text!!.clear()

        binding.tilUSUnitWeight.visibility = View.GONE
        binding.llHeightUnitsUs.visibility = View.GONE

        binding.llDisplayBMIResult.visibility = View.INVISIBLE

    }

    private fun setUsView(){
        currentVeiw = USView
        binding.tilUSUnitWeight.visibility = View.VISIBLE
        binding.llHeightUnitsUs.visibility = View.VISIBLE

        binding.etFeetUnitHeight.text!!.clear()
        binding.etInchUnitHeight.text!!.clear()
        binding.etUSUnitWeight.text!!.clear()

        binding.tilMetricUnitWeight.visibility = View.GONE
        binding.tilMetricUnitHeight.visibility = View.GONE

        binding.llDisplayBMIResult.visibility = View.INVISIBLE
    }
private fun displayBMIResults(bmi : Float){
    var bmiLabel: String
    var bmiDescription : String

    if (java.lang.Float.compare(bmi, 15f) <= 0) {
        bmiLabel = "Very severely underweight"
        bmiDescription = "See a Doctor"
    } else if (java.lang.Float.compare(bmi, 15f) > 0 && java.lang.Float.compare(
            bmi,
            16f
        ) <= 0
    ) {
        bmiLabel = "Severely underweight"
        bmiDescription = "See a Doctor"
    } else if (java.lang.Float.compare(bmi, 16f) > 0 && java.lang.Float.compare(
            bmi,
            18.5f
        ) <= 0
    ) {
        bmiLabel = "Underweight"
        bmiDescription = "See a Doctor"
    } else if (java.lang.Float.compare(bmi, 18.5f) > 0 && java.lang.Float.compare(
            bmi,
            25f
        ) <= 0
    ) {
        bmiLabel = "Normal"
        bmiDescription = "You are in a good shape!"
    } else if (java.lang.Float.compare(bmi, 25f) > 0 && java.lang.Float.compare(
            bmi,
            30f
        ) <= 0
    ) {
        bmiLabel = "Overweight"
        bmiDescription = "See a Doctor"
    } else if (java.lang.Float.compare(bmi, 30f) > 0 && java.lang.Float.compare(
            bmi,
            35f
        ) <= 0
    ) {
        bmiLabel = "Obese Class | (Moderately obese)"
        bmiDescription = "See a Doctor"
    } else if (java.lang.Float.compare(bmi, 35f) > 0 && java.lang.Float.compare(
            bmi,
            40f
        ) <= 0
    ) {
        bmiLabel = "Obese Class || (Severely obese)"
        bmiDescription = "See a Doctor"
    } else {
        bmiLabel = "Obese Class ||| (Very Severely obese)"
        bmiDescription = "See a Doctor"
    }

    val bmiValue = BigDecimal(bmi.toDouble()).setScale(2,RoundingMode.HALF_EVEN).toString()

    binding.llDisplayBMIResult.visibility = View.VISIBLE

    binding.tvBMIValue.setText(bmiValue)
    binding.tvBMIType.setText(bmiLabel)
    binding.tvBMIDescription.setText(bmiDescription)

}
    private fun validateBmiResults(bmi:Float) {
        if(currentVeiw == MetricView){
            if(validateMetricUnit()){
                displayBMIResults(bmi)
            }else {
                Toast.makeText(this@bmiActivity, "Please provide accurate info", Toast.LENGTH_SHORT).show()
            }
        }else{
            if(validateUSUnit()){
                displayBMIResults(bmi)
            } else{
                Toast.makeText(this@bmiActivity, "Please provide accurate info", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun validateMetricUnit() : Boolean {
        var isValid = true

        if(binding.etMetricUnitWeight.text.toString().isEmpty())
            isValid = false

        else if(binding.etMetricUnitHeight.text.toString().isEmpty())
                isValid = false

        return isValid
    }

    private fun validateUSUnit() : Boolean {
        var isValid = true

        if(binding.etUSUnitWeight.text.toString().isEmpty())
            isValid = false

        else if(binding.etFeetUnitHeight.text.toString().isEmpty())
            isValid = false
        else if(binding.etInchUnitHeight.text.toString().isEmpty())
            isValid = false

        return isValid
    }
}