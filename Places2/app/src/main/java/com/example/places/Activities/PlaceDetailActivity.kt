package com.example.places.Activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.places.Models.PlaceModel
import com.example.places.databinding.ActivityPlaceDetailBinding
import kotlinx.android.synthetic.main.activity_place_detail.*

class PlaceDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaceDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var placeDetailModel : PlaceModel? = null

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
          //  placeDetailModel = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS)as PlaceModel
            placeDetailModel = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)as PlaceModel?
        }

        if(placeDetailModel != null){
            setSupportActionBar(toolbar_place_detail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = placeDetailModel.title

            toolbar_place_detail.setNavigationOnClickListener {
                onBackPressed()
            }

            binding.ivPlaceImage.setImageURI(Uri.parse(placeDetailModel.image))
            binding.tvDescription.text = placeDetailModel.description
            binding.tvLocation.text = placeDetailModel.location
            binding.btnViewOnMap.setOnClickListener{
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, placeDetailModel)
                startActivity(intent)
            }
        }
    }
}