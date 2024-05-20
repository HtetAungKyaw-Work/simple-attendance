package com.haker.simpleattendance

import android.annotation.SuppressLint
import android.content.ContentValues
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
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.haker.simpleattendance.databinding.ActivityRegisterBinding
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Calendar


class RegisterActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val PERMISSION_CAMERA_REQUEST = 1
    }

    private lateinit var binding: ActivityRegisterBinding

    private lateinit var galleryLauncher: ActivityResultLauncher<String>

    private val IMAGE_CAPTURE_CODE = 1001

    private var takenPhoto: Bitmap? = null
    private var galleryUri: Uri? = null

    private var name = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
            galleryUri = it
            try {
                binding.ivProfile.setImageURI(galleryUri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.ivProfile.setOnClickListener {
            showAlertForTakePhotoOrChooseGallery()
        }

        binding.btnSave.setOnClickListener {
            if (isValidate()) {

                binding.progressBar.visibility = View.VISIBLE

                name = binding.etName.text.toString()
                val currentDateAndTime = Calendar.getInstance().time
                Log.i("currentDateAndTime", currentDateAndTime.toString())
                val inputData = "$name&$currentDateAndTime"

                Common.putDataToSharedPref("name", name, this)

                if (takenPhoto != null) {
                    saveToGallery(this, takenPhoto!!, "MySelfie")
                }
                else {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, galleryUri)
                    saveToGallery(this, bitmap, "MySelfie")
                }

                goToPrintQR(inputData)
            }
        }
    }

    private fun isValidate(): Boolean {
        if (binding.etName.length() == 0) {
            binding.etName.error = "Please enter Name"
            binding.etName.requestFocus()
            return false
        }
        if (takenPhoto == null && galleryUri == null) {
            Toast.makeText(this, "You need to set the profile selfie picture!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun showAlertForTakePhotoOrChooseGallery() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle("Choose...")
            .setPositiveButton("Take Photo") { dialog, which ->
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
            .setNegativeButton("Gallery") { dialog, which ->
                galleryLauncher.launch("image/*")
            }
            .setNeutralButton("Cancel") { dialog, which ->

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
        cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT)
        cameraIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
        cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)

        filePhoto = getPhotoFile(FILE_NAME)
        val image_uri = FileProvider.getUriForFile(this, this.applicationContext.packageName + ".provider", filePhoto);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    private fun getPhotoFile(fileName: String): File {
        val directoryStorage = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if(requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK){
            takenPhoto = BitmapFactory.decodeFile(filePhoto.absolutePath)
            binding.ivProfile.setImageBitmap(takenPhoto)
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun goToPrintQR(inputData: String) {
        val intent = Intent(this, PrintQRActivity::class.java)
        intent.putExtra("data", inputData)
        startActivity(intent)
        finish()
    }

    private fun saveToGallery(context: Context, bitmap: Bitmap, albumName: String) {

        val filename = "$name.png"
        val write: (OutputStream) -> Boolean = {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/$albumName")
            }

            context.contentResolver.let {
                it.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
                    it.openOutputStream(uri)?.let(write)
                }
            }

        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + albumName
            val file = File(imagesDir)
            if (!file.exists()) {
                file.mkdir()
            }
            val image = File(imagesDir, filename)
            write(FileOutputStream(image))
        }
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