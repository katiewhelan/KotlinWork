package com.example.places.Activities

import android.app.AlertDialog
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.places.Database.DatabaseHandler
import com.example.places.Models.PlaceModel
import com.example.places.R
import com.example.places.Utils.GetAddressFromLatLong
import com.example.places.databinding.ActivityAddPlaceBinding
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class AddPlaceActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding : ActivityAddPlaceBinding

    private val cal = Calendar.getInstance()
    private lateinit var dateSetListener : DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage: Uri? = null
    private var mLat : Double = 0.0
    private var mLong : Double = 0.0
    private var mPlaceDetails: PlaceModel? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAddPlaceBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarAddPlace.setNavigationOnClickListener {
            onBackPressed()
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if(!Places.isInitialized()){
            Places.initialize(this@AddPlaceActivity, resources.getString(R.string.google_maps_api_key))
        }

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mPlaceDetails = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as PlaceModel?
        }
        dateSetListener = DatePickerDialog.OnDateSetListener{ view,year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        updateDateInView()

        if(mPlaceDetails != null){
            supportActionBar?.title = "Edit Place"

            binding.etLocation.setText(mPlaceDetails!!.title)
            binding.etDate.setText(mPlaceDetails!!.date)
            binding.etDescription.setText(mPlaceDetails!!.description)
            binding.etTitle.setText(mPlaceDetails!!.title)
            mLong = mPlaceDetails!!.longitude
            mLat = mPlaceDetails!!.latitude

            saveImageToInternalStorage = Uri.parse(mPlaceDetails!!.image)
             binding.ivPlaceImage.setImageURI(saveImageToInternalStorage)
            binding.btnSave.text = "UPDATE"


        }

        binding.etDate.setOnClickListener(this)
        binding.tvAddImage.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
        binding.etLocation.setOnClickListener(this)
        binding.tvSelectCurrentLocation.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
       when(v!!.id) {
          R.id.etDate -> {
               DatePickerDialog(
                       this@AddPlaceActivity,
                       dateSetListener,
                       cal.get(Calendar.YEAR),
                       cal.get(Calendar.MONTH),
                       cal.get(Calendar.DAY_OF_MONTH)).show()
           }

           R.id.tv_add_image ->{
               val pictureDialog = AlertDialog.Builder(this)
               pictureDialog.setTitle("Select Action")
               val pictureDialogItems = arrayOf("Select Photo From Gallery", "Use Camera")
               pictureDialog.setItems(pictureDialogItems){
                   _, which ->
                       when(which){
                           0 -> choosePhotoFromGallery()
                           1-> takePhotoFromCamera()
                       }

               }.show()
           }
           R.id.btn_save ->{
               when{
                   binding.etTitle.text.isNullOrEmpty() -> {Toast.makeText(this, "Add a Title", Toast.LENGTH_SHORT).show()}
                   binding.etDescription.text.isNullOrEmpty() ->{Toast.makeText(this, "Add a Description", Toast.LENGTH_SHORT).show()}
                   binding.etLocation.text.isNullOrEmpty() -> {Toast.makeText(this, "Add a Location", Toast.LENGTH_SHORT).show()}
                   saveImageToInternalStorage == null -> {Toast.makeText(this, "Add a Image", Toast.LENGTH_SHORT).show()}

                else -> {
                    val placeModel = PlaceModel(
                        if (mPlaceDetails == null) 0 else mPlaceDetails!!.id,
                    binding.etTitle.text.toString(),
                    saveImageToInternalStorage.toString(),
                    binding.etDescription.text.toString(),
                    binding.etDate.text.toString(),
                    binding.etLocation.text.toString(),
                    mLat,
                    mLong
                    )
                    val dbHandler = DatabaseHandler(this)

                    if(mPlaceDetails == null){
                        val addPlace = dbHandler.addPlace(placeModel)

                        if(addPlace > 0 ){
                            setResult(Activity.RESULT_OK)
                            finish()
                        }

                    } else {
                        val updatePlace = dbHandler.updatePlace(placeModel)
                        if(updatePlace > 0 ){
                            setResult(Activity.RESULT_OK)
                            finish()
                            }
                        }
                    }
               }
           }
           R.id.etLocation->{
               try{
                   val fields = listOf(
                       Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS
                   )
                   val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this@AddPlaceActivity)
                   startActivityForResult(intent, PlACE_AUTOCOMPLETE_REQUEST_CODE)

               }catch(e: Exception){
                   e.printStackTrace()
               }
           }
           R.id.tv_select_current_location->{
               if(!isLocationEnabled()) {
                   Toast.makeText(this, "You must turn on Locations", Toast.LENGTH_SHORT).show()
                   val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                   startActivity(intent)
               } else{
               Dexter.withActivity(this).withPermissions(
                   Manifest.permission.ACCESS_FINE_LOCATION,
                   Manifest.permission.ACCESS_COARSE_LOCATION)
                   .withListener(object: MultiplePermissionsListener {
                       override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                           if (report!!.areAllPermissionsGranted()) {
                               Toast.makeText(
                                   this@AddPlaceActivity,
                                   "Permissions Granted",
                                   Toast.LENGTH_SHORT
                               ).show()
                           }
                       }

                       override fun onPermissionRationaleShouldBeShown(
                           permissions: MutableList<PermissionRequest>?,
                           token: PermissionToken?
                       ) {
                          showRationalDialogForPermissions()
                       }
                   }).onSameThread().check()
               }
           }
       }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
    var locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 1000
        locationRequest.numUpdates = 1

        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.myLooper())
    }

    private val locationCallBack = object: LocationCallback(){
        override fun onLocationResult(lResult: LocationResult?) {

                val lastLocation: Location = lResult!!.lastLocation
                mLat = lastLocation.latitude
                Log.i("current Lat", "$mLat")
                mLong = lastLocation.longitude
                Log.i("Current Long", "$mLong")

            val addressTask = GetAddressFromLatLong(this@AddPlaceActivity,mLat, mLong)
            addressTask.setAddressListener(object: GetAddressFromLatLong.AddressListener {
                override fun onAddressFound(address: String?){
                    binding.etLocation.setText(address)
                }
                override fun onError(){
                    Log.e("Get Address:: ","Something went wrong")
                }
            })
            addressTask.getAddress()
        }
    }

    private fun isLocationEnabled(): Boolean{
        val locationManager : LocationManager= getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return  locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun takePhotoFromCamera(){
        Dexter.withActivity(this).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?)
            {if(report!!.areAllPermissionsGranted()){
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA)
            }
            }
            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken)
            {
                showRationalDialogForPermissions()
            }

        }).onSameThread().check()

    }

    private fun choosePhotoFromGallery(){
        Dexter.withActivity(this).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?)
            {if(report!!.areAllPermissionsGranted()){
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, GALLERY)
                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken)
            {
                showRationalDialogForPermissions()
            }

        }).onSameThread().check()

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == GALLERY){
                if(data != null){
                    val contentURI = data.data

                    try{
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI )
                      saveImageToInternalStorage =  saveImageToInternalStorage(selectedImageBitmap)
                        Log.e("Saved Image", "Path :: $saveImageToInternalStorage")
                        binding.ivPlaceImage.setImageBitmap(selectedImageBitmap)

                    }catch(e : IOException){
                        e.printStackTrace()
                        Toast.makeText(this@AddPlaceActivity, "Failed", Toast.LENGTH_SHORT).show()
                    }

                }
            } else if(requestCode == CAMERA){
                if(data != null) {

                    val imageBitmap: Bitmap = data!!.extras!!.get("data") as Bitmap
                    saveImageToInternalStorage =  saveImageToInternalStorage(imageBitmap)
                    Log.e("Saved Image", "Path :: $saveImageToInternalStorage")
                    binding.ivPlaceImage.setImageBitmap(imageBitmap)

                }

            } else if(requestCode == PlACE_AUTOCOMPLETE_REQUEST_CODE){
                val place : Place = Autocomplete.getPlaceFromIntent(data!!)
                binding.etLocation.setText(place.address!!.toString())
                mLat = place.latLng!!.latitude
                mLong = place.latLng!!.longitude
            }
        }
    }

    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this).setMessage("You have turned off these settings").setPositiveButton("Go to Settings")
        { _,_ ->
            try{
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            } catch(e: ActivityNotFoundException){
                e.printStackTrace()
            }
        }.setNegativeButton("Cancel"){dialog,_->
            dialog.dismiss()
        }.show()

    }
    private fun updateDateInView(){
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etDate.setText(sdf.format(cal.time).toString())
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap):Uri{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try{
            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    companion object{
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "PLACESIMAGES"
        private const val  PlACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }

}
