package com.example.draw

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.Image
import android.media.MediaScannerConnection
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.example.draw.databinding.ActivityMainBinding

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream




class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

   // private lateinit var binding2: DialogBrushSizeBinding
    private var mImageButtonCurrentPaint : ImageButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
       // binding2 = DialogBrushSizeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.drawingView.setSizeForBrush(20.toFloat())
        mImageButtonCurrentPaint = binding.llPaintColors[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_selected)
        )

        binding.ibBrush.setOnClickListener(){
            showBrushSizeDialog()
        }

        binding.ibSave.setOnClickListener(){
           if(isReadStorageAllowed()){
              // Toast.makeText(this, "clicked Save and have access", Toast.LENGTH_LONG).show()
               BitmapAsyncTask(getBitMapFromView(binding.flDrawingViewContainer)).execute()
           } else {
               requestStoragePermission()
           }
        }

        binding.ibUndo.setOnClickListener(){
            //Toast.makeText(this,"Hello",Toast.LENGTH_SHORT).show()
            //Toast.makeText(this@MainActivity, "You clicked the undo button", Toast.LENGTH_SHORT).show()
          drawing_View.undoButtonClicked()
        }

        binding.ibGallery.setOnClickListener(){
            if(isReadStorageAllowed()){
                //run code to get image
                val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhotoIntent, GALLERY)
            }else{
                requestStoragePermission()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == GALLERY){
                try{
                    if(data!!.data != null){
                        binding.ivBackground.visibility = View.VISIBLE
                        binding.ivBackground.setImageURI(data.data)
                    }else{
                        Toast.makeText(this,"Image is not correct type", Toast.LENGTH_SHORT).show()
                    }

                }catch(e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showBrushSizeDialog(){
        val brushDialog = Dialog(this)
        //brushDialog.setContentView(binding2.root)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush Size: ")
        //val smallBtn = binding2.ibSmallBrush
        val smallBtn = brushDialog.ib_small_brush
        smallBtn.setOnClickListener{
            binding.drawingView.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        }
        //val mediumBtn = binding2.ibMediumBrush
        val mediumBtn = brushDialog.ib_medium_brush
        mediumBtn.setOnClickListener {
            binding.drawingView.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        }
            //val largeBtn = binding2.ibLargeBrush
             val largeBtn = brushDialog.ib_large_brush
            largeBtn.setOnClickListener{
                binding.drawingView.setSizeForBrush(30.toFloat())
                brushDialog.dismiss()
            }
            brushDialog.show()
        }
    fun paintClicked(view: View){
        //Toast.makeText(this,"you clicked a color", Toast.LENGTH_SHORT).show()
        if(view !== mImageButtonCurrentPaint){
            val imageButton = view as ImageButton
            var colorTag = imageButton.tag.toString()
            binding.drawingView.setColor(colorTag)
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_selected)
            )
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_normal)
            )
            mImageButtonCurrentPaint = view

        }
    }



    private fun saveButtonClicked(){
        Toast.makeText(this, "You clicked the save button", Toast.LENGTH_SHORT).show()
    }

    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())) {

            Toast.makeText(this, "Need Permission to add a background", Toast.LENGTH_SHORT).show()
        }
        ActivityCompat.requestPermissions(this,  arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isReadStorageAllowed(): Boolean{
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun getBitMapFromView(view:View) : Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if(bgDrawable != null){
            bgDrawable.draw(canvas)

        }else{
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }

    private inner class BitmapAsyncTask(val mBitmap: Bitmap):
        AsyncTask<Any, Void, String>(){
        private lateinit var mProgressBar : Dialog
        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog()
        }
        override fun doInBackground(vararg params: Any?): String {

            var result = ""

            if(mBitmap != null){
                try{
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90,bytes)
                    val f = File(externalCacheDir!!.absoluteFile.toString() +
                            File.separator+ "Draw_"+
                            System.currentTimeMillis()/1000 +".png")
                    val fos = FileOutputStream(f)
                    fos.write(bytes.toByteArray())
                    fos.close()
                    result = f.absolutePath
                }catch(e : Exception){
                   result = ""
                    e.printStackTrace()
                }


            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            cancleProgressDialog()
            if(!result!!.isEmpty()){
                Toast.makeText(this@MainActivity, "File saved", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@MainActivity, "Error", Toast.LENGTH_SHORT).show()
            }
            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result),null){
                path, uri -> val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                shareIntent.type = "image/png"
                startActivity(
                        Intent.createChooser(
                                shareIntent, "Share"
                        )
                )

            }
        }

        private fun showProgressDialog(){
            mProgressBar = Dialog(this@MainActivity)
            mProgressBar.setContentView(R.layout.dialog_custom_progress)
            mProgressBar.show()
        }

        private fun cancleProgressDialog(){
            mProgressBar.dismiss()
        }
    }

    companion object{
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }
}
