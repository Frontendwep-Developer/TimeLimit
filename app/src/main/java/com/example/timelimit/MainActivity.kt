package com.example.timelimit

import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.text.TextUtils
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
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

        // Toolbarni o'rnatamiz
        setSupportActionBar(binding.toolbar)
        
        // MUHIM: Standart sarlavhalarni o'chirish
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.title = null // Toolbarning o'z title'ini o'chirish

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_apps, R.id.navigation_settings, R.id.navigation_notifications, R.id.navigation_interface, R.id.navigation_security, R.id.navigation_privacy, R.id.navigation_help, R.id.navigation_about),
            binding.drawerLayout
        )

        // Navigatsiyani Toolbar bilan bog'laymiz
        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration)
        
        // NavigationUI sarlavhani majburlab qo'shmasligi uchun quyidagini qilamiz:
        navController.addOnDestinationChangedListener { _, _, _ ->
            // Har safar navigatsiya o'zgarganda sarlavhani majburlab yashiramiz
            binding.toolbar.title = null
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        binding.navView.setNavigationItemSelectedListener(this)

        // MAXSUS TextView NI YANGILASH
        val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_apps -> toolbarTitle.text = "ILOVALAR"
                R.id.navigation_settings -> toolbarTitle.text = "SOZLAMALAR"
                R.id.navigation_notifications -> toolbarTitle.text = "BILDIRISHNOMALAR"
                R.id.navigation_permissions -> toolbarTitle.text = "RUXSATLAR"
                R.id.navigation_interface -> toolbarTitle.text = "MAVZU"
                R.id.navigation_security -> toolbarTitle.text = "XAVFSIZLIK"
                R.id.navigation_privacy -> toolbarTitle.text = "MAXFIYLIK"
                R.id.navigation_help -> toolbarTitle.text = "YORDAM"
                R.id.navigation_about -> toolbarTitle.text = "ILOVA HAQIDA"
                else -> toolbarTitle.text = destination.label?.toString()?.uppercase() ?: "TIMELIMIT"
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Navigatsiyani qo'lda amalga oshiramiz (NavigationUI o'zi sarlavhani o'zgartirib yubormasligi uchun)
        when (item.itemId) {
            R.id.navigation_apps -> navController.navigate(R.id.navigation_apps)
            R.id.nav_language -> navController.navigate(R.id.navigation_settings)
            R.id.nav_notifications -> navController.navigate(R.id.navigation_notifications)
            R.id.nav_interface -> navController.navigate(R.id.navigation_interface)
            R.id.nav_permissions -> navController.navigate(R.id.navigation_permissions)
            R.id.nav_security -> navController.navigate(R.id.navigation_security)
            R.id.nav_privacy -> navController.navigate(R.id.navigation_privacy)
            R.id.nav_help -> navController.navigate(R.id.navigation_help)
            R.id.nav_about -> navController.navigate(R.id.navigation_about)
            else -> NavigationUI.onNavDestinationSelected(item, navController)
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
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
        if (areAllPermissionsGranted()) {
            startMonitoring()
            viewModel.forceUpdate()
        }
    }

    private fun areAllPermissionsGranted(): Boolean {
        return hasUsageStatsPermission() &&
                checkOverlayPermission() &&
                isAccessibilityServiceEnabled() &&
                isBatteryOptimizationIgnored()
    }

    private fun startMonitoring() {
        val serviceIntent = Intent(this, UsageTrackingService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName = ComponentName(this, AppBlockerService::class.java)
        val enabledServicesSetting = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)

        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledComponent = ComponentName.unflattenFromString(componentNameString)
            if (enabledComponent != null && enabledComponent == expectedComponentName) {
                return true
            }
        }
        return false
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
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
