package com.haker.simpleattendance

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.haker.simpleattendance.databinding.ActivityCompareBinding
import java.io.File


class CompareActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompareBinding
    private var fileName1 = ""
    private var data = ""
    private var percent = 0.0
    private var name = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCompareBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        name = Common.getDataFromSharedPref("name", this).toString()
        fileName1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + "MySelfie/$name.png"
        //fileName1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + "Camera/IMG_20240408_113437_644.jpg"
        Log.i("fileName1", fileName1)
        val file1 = File(fileName1)
        val bitmap1 = MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.fromFile(file1))
        binding.ivSelfie1.setImageBitmap(bitmap1)

        data = intent.getStringExtra("imageUri").toString()
        val imageUri = data.toUri()
        //binding.ivSelfie2.setImageURI(imageUri)

        val bitmap2 = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
        binding.ivSelfie2.setImageBitmap(bitmap2)

        binding.btnCompare.setOnClickListener {
            percent = compareImages(bitmap1, bitmap2)
            if (percent < 50) {
                Toast.makeText(this, "Two Selfies are the same!", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Two Selfies are not the same!", Toast.LENGTH_SHORT).show()
            }
            //Toast.makeText(this, compareImages(bitmap1, bitmap2).toString(), Toast.LENGTH_SHORT).show()

        }
    }

    fun compareImages(bitmap1: Bitmap, bitmap2: Bitmap): Double {

        var width1 = bitmap1.width
        var height1 = bitmap1.height
        var width2 = bitmap2.width
        var height2 = bitmap2.height

        if ((width1 != width2) || (height1 != height2)) {
            percent = 0.0
            Log.i("here1", "here1")
        }
        else {
            Log.i("here2", "here2")
            var difference = 0L
            var y = 0
            while (y < height1) {
                // original y++
                var x = 0
                while (x < width1) {
                    // original x++
                    val rgbA: Int = bitmap1.getPixel(x, y)
                    val rgbB: Int = bitmap2.getPixel(x, y)
                    val redA = rgbA shr 16 and 0xff
                    val greenA = rgbA shr 8 and 0xff
                    val blueA = rgbA and 0xff
                    val redB = rgbB shr 16 and 0xff
                    val greenB = rgbB shr 8 and 0xff
                    val blueB = rgbB and 0xff
                    difference += Math.abs(redA - redB)
                    difference += Math.abs(greenA - greenB)
                    difference += Math.abs(blueA - blueB)
                    x = x + 2
                }
                y = y + 2
            }

            Log.i("difference", difference.toString())
            var total_pixels = width1 * height1 * 3
            Log.i("total_pixels", total_pixels.toString())
            var avg_diff_pixels = difference / total_pixels
            Log.i("avg_diff_pixels", avg_diff_pixels.toString())
            //percent = (avg_diff_pixels / 255).toDouble()
            var result = (avg_diff_pixels * 100) / 255
            Log.i("result", result.toString())
            percent = result.toDouble()
            Log.i("percent", percent.toString())
        }
        return percent
    }

//    fun compareImages(bitmap1: Bitmap, bitmap2: Bitmap): Boolean {
//        if (bitmap1.width != bitmap2.width ||
//            bitmap1.height != bitmap2.height
//        ) {
//            return false
//        }
//        for (y in 0 until bitmap1.height) {
//            for (x in 0 until bitmap1.width) {
//                if (bitmap1.getPixel(x, y) != bitmap2.getPixel(x, y)) {
//                    return false
//                }
//            }
//        }
//        return true
//    }
//
//    fun equals(bitmap1: Bitmap, bitmap2: Bitmap): Boolean {
//        val buffer1 = ByteBuffer.allocate(bitmap1.height * bitmap1.rowBytes)
//        bitmap1.copyPixelsToBuffer(buffer1)
//        val buffer2 = ByteBuffer.allocate(bitmap2.height * bitmap2.rowBytes)
//        bitmap2.copyPixelsToBuffer(buffer2)
//        return Arrays.equals(buffer1.array(), buffer2.array())
//    }

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