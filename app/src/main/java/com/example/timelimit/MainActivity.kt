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
import android.view.MenuItem
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.timelimit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private val viewModel: AppsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_apps, R.id.navigation_settings, R.id.navigation_permissions, R.id.navigation_notifications),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val bundle = Bundle()
        
        when (item.itemId) {
            // Asosiy navigatsiya
            R.id.navigation_apps -> {
                navController.navigate(R.id.navigation_apps)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            R.id.navigation_settings -> {
                navController.navigate(R.id.navigation_settings)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            
            // Maxsus ruxsatlar bo'limi
            R.id.nav_permissions -> {
                navController.navigate(R.id.navigation_permissions)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }

            // Bildirishnomalar bo'limi
            R.id.nav_notifications -> {
                navController.navigate(R.id.navigation_notifications)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            
            // TIL bo'limi
            R.id.nav_language -> {
                bundle.putString("target_section", "language")
                navController.navigate(R.id.navigation_settings, bundle)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            
            // Boshqa bo'limlar (Hozircha o'chirilgan)
            R.id.nav_interface,
            R.id.nav_security,
            R.id.nav_privacy,
            R.id.nav_help,
            R.id.nav_help_center,
            R.id.nav_contact -> {
                // Hech nima qilmaydi
                return false
            }
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
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
            checkBatteryOptimization()
            startMonitoring()
        }
    }

    private fun startMonitoring() {
        Log.d("MainActivity", "All permissions granted. Starting foreground monitoring service.")
        val serviceIntent = Intent(this, ForegroundMonitoringService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        return enabledServices.any { it.resolveInfo.serviceInfo.packageName == packageName }
    }

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

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = packageName
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                showBatteryOptimizationDialog()
            }
        }
    }

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