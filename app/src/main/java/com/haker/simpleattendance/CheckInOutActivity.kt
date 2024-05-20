package com.haker.simpleattendance

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.haker.simpleattendance.databinding.ActivityCheckInOutBinding
import java.io.ByteArrayOutputStream
import java.io.File


@SuppressLint("ObsoleteSdkInt")
class CheckInOutActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val PERMISSION_CAMERA_REQUEST = 1
    }

    private lateinit var binding: ActivityCheckInOutBinding

    private val IMAGE_CAPTURE_CODE = 1001

    var vFilename: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCheckInOutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnScan.setOnClickListener {
            startQRCodeScanner()
        }

        binding.btnCapture.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (isCameraPermissionGranted()) {
                    openCamera()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.CAMERA),
                        PERMISSION_CAMERA_REQUEST
                    )
                }
            } else {
                Toast.makeText(this, "Sorry you're version android is not support, Min Android 6.0 (Marsmallow)", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startQRCodeScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setBeepEnabled(false)
        integrator.setTorchEnabled(false)
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {
                //Toast.makeText(this, result.contents, Toast.LENGTH_SHORT).show()
                showAlertDialog(result.contents)
            } else {
                Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
        else if(requestCode == IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK){
            val takenPhoto = BitmapFactory.decodeFile(filePhoto.absolutePath)
            //binding.ivProfile.setImageBitmap(takenPhoto)
            //val encodeString = encodeImage(takenPhoto)
//            Log.i("encodeString", encodeString)
            val imageUri = getImageUri(this, takenPhoto)
            Log.i("imageUri", imageUri.toString())
            goToCompare(imageUri.toString())
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
//        if(requestCode == IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK){
//            binding.ivProfile.setImageURI(data?.data)
//        }
    }

    private fun showAlertDialog(data: String) {
        val dataArr = data.split("&")
        val name = dataArr[0]
        val date = dataArr[1]
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle("Scan Result")
            .setMessage("Name : $name \nDate : $date")
            .setPositiveButton("OK") { dialog, which ->

            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            baseContext,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_CAMERA_REQUEST) {
            if (isCameraPermissionGranted()) {
                openCamera()
            } else {
                Log.e(TAG, "no camera permission")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun openCamera() {

//        val values = ContentValues()
//        values.put(MediaStore.Images.Media.TITLE, "New Picture")
//        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")

        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)


        filePhoto = getPhotoFile(FILE_NAME)
        val image_uri = FileProvider.getUriForFile(this, this.applicationContext.packageName + ".provider", filePhoto);

        cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT)
        cameraIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
        cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    private fun getPhotoFile(fileName: String): File {
        val directoryStorage = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }

    private fun encodeImage(bm: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    private fun goToCompare(data: String) {
        val intent = Intent(this, CompareActivity::class.java)
        intent.putExtra("imageUri", data)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        goToMain()
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

private const val REQUEST_CODE = 13
private lateinit var filePhoto: File
private const val FILE_NAME = "photo.jpg"