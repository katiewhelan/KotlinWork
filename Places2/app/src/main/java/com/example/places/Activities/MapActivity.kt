package com.example.places.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.places.Models.PlaceModel
import com.example.places.R
import com.example.places.databinding.ActivityMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private  lateinit var binding: ActivityMapBinding
    private var mPlaceDetails: PlaceModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mPlaceDetails = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as PlaceModel?
        }
        if(mPlaceDetails != null){
            setSupportActionBar(toolbar_map)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = mPlaceDetails!!.title
            toolbar_map.setNavigationOnClickListener {
                onBackPressed()
            }

            val supportMapFrag : SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            supportMapFrag.getMapAsync(this)

        }

    }

    override fun onMapReady(gm: GoogleMap?) {
        val position = LatLng(mPlaceDetails!!.latitude, mPlaceDetails!!.longitude)
        gm!!.addMarker(MarkerOptions().position(position).title(mPlaceDetails!!.location))
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 15F)
        gm.animateCamera(newLatLngZoom)
    }
}