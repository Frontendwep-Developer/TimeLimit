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
import android.view.View
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
            setOf(R.id.navigation_apps, R.id.navigation_settings, R.id.navigation_permissions, R.id.navigation_notifications, R.id.navigation_interface, R.id.navigation_security, R.id.navigation_privacy, R.id.navigation_help, R.id.navigation_about, R.id.navigation_terms, R.id.navigation_policy, R.id.navigation_license),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setNavigationItemSelectedListener(this)

        // Fragmentlar o'zgarganda Toolbarni boshqarish
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_settings,
                R.id.navigation_permissions,
                R.id.navigation_notifications,
                R.id.navigation_interface,
                R.id.navigation_security,
                R.id.navigation_privacy,
                R.id.navigation_help,
                R.id.navigation_about,
                R.id.navigation_terms,
                R.id.navigation_policy,
                R.id.navigation_license -> {
                    // Bu fragmentlarda shaxsiy header bor, shuning uchun asosiysini yashiramiz
                    supportActionBar?.hide()
                }
                else -> {
                    // Boshqa fragmentlarda (masalan, Apps) ko'rsatamiz
                    supportActionBar?.show()
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_apps -> navController.navigate(R.id.navigation_apps)
            R.id.nav_language -> navController.navigate(R.id.navigation_settings) // Settings is now Language
            R.id.nav_notifications -> navController.navigate(R.id.navigation_notifications)
            R.id.nav_interface -> navController.navigate(R.id.navigation_interface)
            R.id.nav_permissions -> navController.navigate(R.id.navigation_permissions)
            R.id.nav_security -> navController.navigate(R.id.navigation_security)
            R.id.nav_privacy -> navController.navigate(R.id.navigation_privacy)
            R.id.nav_help -> navController.navigate(R.id.navigation_help)
            R.id.nav_about -> navController.navigate(R.id.navigation_about)
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
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
        // Barcha ruxsatlar berilganligini tekshirish
        if (areAllPermissionsGranted()) {
            startMonitoring()
            viewModel.forceUpdate()
        } else {
            // Agar ruxsat berilmagan bo'lsa va biz allaqachon ruxsatlar ekranida bo'lmasak
            if (navController.currentDestination?.id != R.id.navigation_permissions) {
                Toast.makeText(this, "Ilova to'liq ishlashi uchun ruxsatlar kerak", Toast.LENGTH_SHORT).show()
                navController.navigate(R.id.navigation_permissions)
            }
        }
    }

    private fun areAllPermissionsGranted(): Boolean {
        return hasUsageStatsPermission() &&
                checkOverlayPermission() &&
                isAccessibilityServiceEnabled() &&
                isBatteryOptimizationIgnored()
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

    private fun isBatteryOptimizationIgnored(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            return pm.isIgnoringBatteryOptimizations(packageName)
        }
        return true
    }
}