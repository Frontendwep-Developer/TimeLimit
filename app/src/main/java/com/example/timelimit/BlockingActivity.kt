package com.example.timelimit

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.timelimit.databinding.ActivityBlockingBinding

class BlockingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlockingBinding
    private var blockedPackageName: String? = null

    companion object {
        private const val TAG = "BlockingActivity"
        const val ACTION_EXIT_APP = "com.example.timelimit.ACTION_EXIT_APP"
        const val ACTION_APP_UNBLOCKED = "com.example.timelimit.ACTION_APP_UNBLOCKED"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: BlockingActivity started")

        binding = ActivityBlockingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUI()

        blockedPackageName = intent.getStringExtra("packageName")
        updateUI(blockedPackageName)

        binding.btnExit.setOnClickListener {
            exitBlockedApp()
        }

        binding.btnTimeLimit.setOnClickListener {
            openTimeLimitApp()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        blockedPackageName = intent?.getStringExtra("packageName")
        updateUI(blockedPackageName)
    }

    private fun updateUI(packageName: String?) {
        if (packageName != null) {
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                binding.tvAppName.text = appName
                binding.ivBlockedIcon.setImageDrawable(packageManager.getApplicationIcon(appInfo))
            } catch (e: Exception) {
                binding.tvAppName.text = "Ilova"
            }
        }
    }

    private fun exitBlockedApp() {
        Log.d(TAG, "exitBlockedApp: Closing app: $blockedPackageName")

        try {
            // AppBlockerService'ga chiqish haqida xabar berish
            LocalBroadcastManager.getInstance(this).sendBroadcast(
                Intent(ACTION_EXIT_APP).apply {
                    putExtra("packageName", blockedPackageName)
                }
            )

            // Ilovadan chiqish uchun Home ekraniga o'tamiz
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
            
            finish()

        } catch (e: Exception) {
            Log.e(TAG, "Error in exitBlockedApp: ${e.message}")
            finish()
        }
    }

    private fun openTimeLimitApp() {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error opening TimeLimit: ${e.message}")
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        exitBlockedApp()
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
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
    }
}
