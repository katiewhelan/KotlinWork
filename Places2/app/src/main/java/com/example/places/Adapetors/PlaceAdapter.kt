package com.example.places.Adapetors

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.places.Models.PlaceModel
import com.example.places.R
import com.example.places.databinding.ItemPlaceBinding
import kotlinx.android.synthetic.main.item_place.view.*
import android.app.Activity

import android.content.Intent


import com.example.places.Activities.AddPlaceActivity
import com.example.places.Activities.MainActivity
import com.example.places.Database.DatabaseHandler



open class PlaceAdapter (

    private val context: Context,
    private var list : ArrayList<PlaceModel>
    ): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
  // private lateinit var binding: ItemPlaceBinding

    private var onClickListener: OnClickListener? = null
   // binding = ItemPlaceBinding.inflate(layoutInflater)
    // setContentView(binding.root)



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_place,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
       val model = list[position]
//        if(holder is MyViewHolder){
//            binding.ivPlaceImage.setImageURI(Uri.parse(model.image))
//            binding.tvTitle.text = model.title
//            binding.tvDescription.text = model.description
//        }
        holder.itemView.ivPlaceImage.setImageURI(Uri.parse(model.image))
        holder.itemView.tvTitle.text = model.title
        holder.itemView.tvDescription.text = model.description

        holder.itemView.setOnClickListener {
            if(onClickListener != null){
                onClickListener!!.onClick(position, model)
            }
        }

    }

    fun notifyEditItem(activity:Activity, position: Int, requestCode: Int){
        val intent = Intent(context, AddPlaceActivity::class.java)

        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    fun removeAt(position: Int){
        val dbHandler = DatabaseHandler(context)
        val isDelete = dbHandler.deletePlace(list[position])
         if(isDelete > 0 ){
             list.removeAt(position)
             notifyItemRemoved(position)
         }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: PlaceModel)
    }
    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)
}

