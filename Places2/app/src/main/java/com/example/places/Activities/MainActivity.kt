package com.example.places.Activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.places.Adapetors.PlaceAdapter
import com.example.places.Database.DatabaseHandler
import com.example.places.Models.PlaceModel
import com.example.places.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import pl.kitek.rvswipetodelete.SwipeToDeleteCallback
import pl.kitek.rvswipetoedit.SwipeToEditCallback

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.fabAddPlace.setOnClickListener{
            val intent = Intent(this, AddPlaceActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }
            getPlaceListFromLocalDB()
    }

    private fun setupPlacesRecyclerView(placeList: ArrayList<PlaceModel>) {
        binding.rvPlacesLists.layoutManager = LinearLayoutManager(this)
        binding.rvPlacesLists.setHasFixedSize(true)
        val placesAdapter = PlaceAdapter(this, placeList)
        binding.rvPlacesLists.adapter = placesAdapter

        placesAdapter.setOnClickListener(object :
            PlaceAdapter.OnClickListener {
            override fun onClick(position: Int, model: PlaceModel) {
                val intent = Intent(this@MainActivity, PlaceDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })
        val editSwipeHandler = object : SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter =  binding.rvPlacesLists.adapter as PlaceAdapter
                adapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition,
                    ADD_PLACE_ACTIVITY_REQUEST_CODE)
            }


        }
        val deleteSwipeHandler = object: SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter= binding.rvPlacesLists.adapter as PlaceAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                getPlaceListFromLocalDB()
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(rvPlacesLists)


        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rvPlacesLists)
    }

    private fun getPlaceListFromLocalDB(){
        val dbHandler = DatabaseHandler(this)
        val getPlaceList : ArrayList<PlaceModel> = dbHandler.getPlaceList()

        if(getPlaceList.size > 0){
            binding.rvPlacesLists.visibility = View.VISIBLE
            binding.tvNoRecordsAvailable.visibility = View.GONE
            setupPlacesRecyclerView(getPlaceList)
        } else{
            binding.rvPlacesLists.visibility = View.GONE
            binding.tvNoRecordsAvailable.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                getPlaceListFromLocalDB()
            } else{
                Log.e("Activity", "Cancelled or Back Pressed")
                }
        }
    }
    companion object{
        private const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        var EXTRA_PLACE_DETAILS = "extra_place_details"
    }
}