package com.example.timelimit

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.timelimit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: AppsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)
    }

    override fun onResume() {
        super.onResume()
        checkAllPermissionsAndStart()
        viewModel.forceUpdate()
    }

    private fun checkAllPermissionsAndStart() {
        if (!hasUsageStatsPermission()) {
            showUsageStatsDialog()
        } else if (!checkOverlayPermission()) {
            showOverlayPermissionDialog()
        } else if (!isAccessibilityServiceEnabled()) {
            showAccessibilityPermissionDialog()
        } else {
            // Hamma ruxsatlar bor, endi batareyani tekshiramiz
            checkBatteryOptimization()
            startMonitoring()
        }
    }

    // 1. Monitoringni boshlash
    private fun startMonitoring() {
        Log.d("MainActivity", "All permissions granted. Starting foreground monitoring service.")

        // ForegroundMonitoringService orqali AppMonitoringService fon rejimida ishlaydi.
        // Bu TimeLimit ilovasi yopilganida ham limit va bloklash davom etishini ta'minlaydi.
        val serviceIntent = Intent(this, ForegroundMonitoringService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    // 2. Accessibility Service tekshirish
    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        return enabledServices.any { it.resolveInfo.serviceInfo.packageName == packageName }
    }

    // 3. Usage Stats ruxsatini tekshirish
    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // 4. Overlay ruxsatini tekshirish
    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    // 5. Batareya optimizatsiyasini tekshirish
    private fun checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = packageName
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                showBatteryOptimizationDialog()
            }
        }
    }

    // --- Dialoglar va Yo'naltirishlar ---

    private fun showUsageStatsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Ruxsatnoma kerak")
            .setMessage("Ilovalar vaqtini hisoblash uchun 'Foydalanishga ruxsat' (Usage Access) talab qilinadi.")
            .setPositiveButton("Sozlamalarga o'tish") { _, _ ->
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
            .setNegativeButton("Bekor qilish", null)
            .setCancelable(false)
            .show()
    }

    private fun showOverlayPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Bloklash oynasi uchun ruxsat")
            .setMessage("Bloklash oynasini ko'rsatish uchun 'Boshqa ilovalar ustida ko'rsatish' ruxsati kerak.")
            .setPositiveButton("Sozlamalarga o'tish") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                }
            }
            .setNegativeButton("Bekor qilish", null)
            .setCancelable(false)
            .show()
    }

    private fun showAccessibilityPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Qo'shimcha himoya")
            .setMessage("Ilovalarni ishonchli bloklash uchun Maxsus imkoniyatlar (Accessibility) xizmatini yoqing.")
            .setPositiveButton("Sozlamalarga o'tish") { _, _ ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            .setNegativeButton("Keyinroq") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    private fun showBatteryOptimizationDialog() {
         AlertDialog.Builder(this)
            .setTitle("Batareya cheklovini olib tashlash")
            .setMessage("Ilova yopilganda ham ishlashi uchun, iltimos, batareya optimizatsiyasini o'chiring.")
            .setPositiveButton("Sozlash") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                }
            }
            .setNegativeButton("Keyinroq", null)
            .show()
    }
}