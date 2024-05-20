package com.haker.simpleattendance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.haker.simpleattendance.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnRegister.setOnClickListener {
            goToRegister()
        }

        binding.btnCheckInOut.setOnClickListener {
            goToCheckInOut()
        }

        binding.btnVisual.setOnClickListener {
            goToChart()
        }
    }

    private fun goToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToCheckInOut() {
        val intent = Intent(this, CheckInOutActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToChart() {
        val intent = Intent(this, ChartActivity::class.java)
        startActivity(intent)
        finish()
    }
}