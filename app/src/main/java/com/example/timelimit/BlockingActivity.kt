package com.example.timelimit

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import com.example.timelimit.databinding.ActivityBlockingBinding

class BlockingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlockingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Full screen rejimini yoqish
        hideSystemUI()

        binding = ActivityBlockingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val packageName = intent.getStringExtra("packageName")
        updateUI(packageName)

        binding.btnExit.setOnClickListener {
            exitApp()
        }
    }

    private fun updateUI(packageName: String?) {
        if (packageName != null) {
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                binding.tvAppName.text = "$appName is blocked for today"
            } catch (e: PackageManager.NameNotFoundException) {
                binding.tvAppName.text = "App is blocked for today"
            }
        } else {
            binding.tvAppName.text = "App is blocked for today"
        }
    }

    private fun exitApp() {
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(homeIntent)
        finishAffinity() // Ilovani to'liq yopish
    }

    override fun onBackPressed() {
        // Orqaga tugmasini bosganda ham ilovani yopamiz
        exitApp()
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
    }
}