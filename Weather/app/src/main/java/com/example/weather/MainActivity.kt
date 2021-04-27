package com.example.weather

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import com.example.weather.databinding.ActivityMainBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.usage.ConfigurationStats
import android.content.ActivityNotFoundException
import android.content.SharedPreferences
import android.net.Uri
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import com.example.weather.models.WeatherResponse
import com.example.weather.network.WeatherService
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import retrofit.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var binding : ActivityMainBinding
    private var mProgressBar : Dialog? = null
    private lateinit var mSharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mSharedPreferences = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE)

        setUpUI()

        if(!isLocationEnabled()){
            Toast.makeText(this, "Turn on Location Services", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
        } else {
           Dexter.withActivity(this).withPermissions(
               Manifest.permission.ACCESS_FINE_LOCATION,
               Manifest.permission.ACCESS_COARSE_LOCATION
           ).withListener(object : MultiplePermissionsListener{
               override fun onPermissionsChecked(report: MultiplePermissionsReport?){
                   if(report!!.areAllPermissionsGranted()){
                       requestLocationData()
                   }
                   if(report.isAnyPermissionPermanentlyDenied ){
                       Toast.makeText(this@MainActivity, "Need GPS Access", Toast.LENGTH_SHORT).show()
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

    private fun getLocationWeatherDetails(lat: Double, long: Double){
        if(Constants.isNetworkAvailable(this)){
        val retrofit : Retrofit  = Retrofit.Builder().baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()

            val service : WeatherService = retrofit.create<WeatherService>(WeatherService::class.java)

            val listCall : Call<WeatherResponse> = service.getWeather(
                lat, long, Constants.METRIC_UNIT, Constants.APP_ID
            )
            showCustomProgressDialog()
            listCall.enqueue(object: Callback<WeatherResponse>{
                override fun onResponse(response: Response<WeatherResponse>?, retrofit: Retrofit?) {
                    if(response!!.isSuccess){
                        hideCustomProgressDialog()
                        val weatherList = response.body()
                        val weatherResponseJsonString = Gson().toJson(weatherList)
                        val editor = mSharedPreferences.edit()
                        editor.putString(Constants.WEATHER_RESPONSE_DATE, weatherResponseJsonString)
                        editor.apply()
                        setUpUI()
                        Log.i("response result", "$weatherList")
                    }else{
                        val responseCode = response.code()
                        when(responseCode){
                            400 -> {
                                Log.e("Error 400", "Bad Connection.....Now What")
                            }
                            404->{
                                Log.e("Error 404", "Not Found")
                        }else ->{
                                Log.e("Generic", "¯\\_(ツ)_/¯")
                            }
                        }
                    }
                }

                override fun onFailure(t: Throwable?) {
                    Log.e("Error from open weather", t!!.message.toString())
                    hideCustomProgressDialog()
                }

            })

        }else{
            Toast.makeText(this,"NO INTERNET", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData(){
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
    }

    private val mLocationCallback = object: LocationCallback(){
        override fun onLocationResult(p0: LocationResult?) {
            val mLastLocation : Location = p0!!.lastLocation
            val lat = mLastLocation.latitude
            val long = mLastLocation.longitude
            Log.i("Longitude", "$long")
            Log.i("Lati", "$lat")
            getLocationWeatherDetails(lat, long)
        }
    }

    private fun showRationalDialogForPermissions(){
     AlertDialog.Builder(this)
         .setMessage("Turned off GPS")
         .setPositiveButton(
             "Go to Settings"
         ){_,_->
             try {
                 val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                  val uri = Uri.fromParts("package", packageName, null)
                 intent.data = uri
                 startActivity(intent)
             } catch(e: ActivityNotFoundException){
                 e.printStackTrace()
             }
         }.setNegativeButton("Cancel"){dialog, _ -> dialog.dismiss()}.show()
}

    private fun showCustomProgressDialog(){
        mProgressBar = Dialog(this)

        mProgressBar!!.setContentView(R.layout.dialog_custom_progress)
        mProgressBar!!.show()
    }

    private fun hideCustomProgressDialog(){
        if(mProgressBar != null){
            mProgressBar!!.dismiss()
        }
    }
    @SuppressLint("SetTextI18n")
    private fun setUpUI(){
        val weatherListResponseJsonString = mSharedPreferences.getString(Constants.WEATHER_RESPONSE_DATE, "")

        if(!weatherListResponseJsonString.isNullOrEmpty()){
            val weatherList = Gson().fromJson(weatherListResponseJsonString,WeatherResponse::class.java)

        for(i in weatherList.weather.indices) {
            Log.i("Weather Name", weatherList.weather.toString())
            binding.tvMain.text = weatherList.weather[i].main
            binding.tvMainDescription.text = weatherList.weather[i].description
            binding.tvTemp.text =
                weatherList.main.temp.toString() + getUnit(application.resources.configuration.locales.toString())
            binding.tvSunriseTime.text = unixTime(weatherList.sys.sunrise)
            binding.tvSunsetTime.text = unixTime(weatherList.sys.sunset)
            binding.tvHumidity.text = weatherList.main.humidity.toString() + " per cent"
            binding.tvMin.text = weatherList.main.tempMin.toString() + " min"
            binding.tvMax.text = weatherList.main.tempMax.toString() + " max"
            binding.tvSpeed.text = weatherList.wind.speed.toString()
            binding.tvName.text = weatherList.name
            binding.tvCountry.text = weatherList.sys.country

            when (weatherList.weather[i].icon) {
                "01d" -> binding.ivMain.setImageResource(R.drawable.sunny)
                "02d" -> binding.ivMain.setImageResource(R.drawable.cloud)
                "03d" -> binding.ivMain.setImageResource(R.drawable.cloud)
                "04d" -> binding.ivMain.setImageResource(R.drawable.cloud)
                "04n" -> binding.ivMain.setImageResource(R.drawable.cloud)
                "10d" -> binding.ivMain.setImageResource(R.drawable.rain)
                "11d" -> binding.ivMain.setImageResource(R.drawable.storm)
                "13d" -> binding.ivMain.setImageResource(R.drawable.snowflake)
                "01n" -> binding.ivMain.setImageResource(R.drawable.cloud)
                "02n" -> binding.ivMain.setImageResource(R.drawable.cloud)
                "03n" -> binding.ivMain.setImageResource(R.drawable.cloud)
                "10n" -> binding.ivMain.setImageResource(R.drawable.cloud)
                "11n" -> binding.ivMain.setImageResource(R.drawable.rain)
                "13n" -> binding.ivMain.setImageResource(R.drawable.snowflake)
            }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_refresh ->{
                requestLocationData()
                true
            }else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getUnit(value: String): String? {
        var value = "°C"
        if ("US" == value || "LR" == value || "MM" == value) {
            value = "°F"
        }
        return value
    }

    private fun unixTime(timex:Long):String?{
        val date = Date(timex * 1000L)
        val sdf = SimpleDateFormat("HH:mm", Locale.US)
        sdf.timeZone= TimeZone.getDefault()
        return sdf.format(date)
    }

    private fun isLocationEnabled():Boolean{
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}