package com.example.timelimit.ui.permissions

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.timelimit.R
import com.example.timelimit.databinding.FragmentPermissionsBinding

class PermissionsFragment : Fragment() {

    private var _binding: FragmentPermissionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPermissionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        checkPermissionsAndUpdateUI()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigate(R.id.navigation_apps) }
        binding.btnClose.setOnClickListener { findNavController().navigate(R.id.navigation_apps) }

        // 1. Usage Access
        binding.switchUsageAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        // 2. Overlay
        val overlayListener = View.OnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${requireContext().packageName}"))
                startActivity(intent)
            }
        }
        binding.switchOverlay.setOnClickListener(overlayListener)
        binding.btnOverlaySettings.setOnClickListener(overlayListener)

        // 3. Accessibility
        val accessibilityListener = View.OnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        binding.switchAccessibility.setOnClickListener(accessibilityListener)
        binding.btnAccessibilitySettings.setOnClickListener(accessibilityListener)

        // 4. Battery Optimization
        val batteryListener = View.OnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:${requireContext().packageName}")
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                }
            }
        }
        binding.switchBattery.setOnClickListener(batteryListener)
        binding.btnBatterySettings.setOnClickListener(batteryListener)

        binding.btnEnableAll.setOnClickListener { enableNextPermission() }
        binding.btnDisableAll.setOnClickListener {
             val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
             intent.data = Uri.parse("package:${requireContext().packageName}")
             startActivity(intent)
        }
    }

    private fun checkPermissionsAndUpdateUI() {
        val hasUsage = hasUsageStatsPermission()
        val hasOverlay = checkOverlayPermission()
        val hasAccessibility = isAccessibilityServiceEnabled()
        val hasBattery = isBatteryOptimizationIgnored()

        binding.switchUsageAccess.isChecked = hasUsage
        binding.switchOverlay.isChecked = hasOverlay
        binding.switchAccessibility.isChecked = hasAccessibility
        binding.switchBattery.isChecked = hasBattery

        // Update progress
        var count = 0
        if (hasUsage) count++
        if (hasOverlay) count++
        if (hasAccessibility) count++
        if (hasBattery) count++

        binding.progressBarPermissions.progress = count
        binding.tvProgressText.text = "$count/4 permissions granted"
        binding.tvProgressPercent.text = "${(count * 100) / 4}%"
    }

    private fun enableNextPermission() {
        if (!hasUsageStatsPermission()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        } else if (!checkOverlayPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${requireContext().packageName}")))
            }
        } else if (!isAccessibilityServiceEnabled()) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        } else if (!isBatteryOptimizationIgnored()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${requireContext().packageName}")))
                } catch (e: Exception) {
                    startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                }
            }
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), requireContext().packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), requireContext().packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun checkOverlayPermission(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(requireContext()) else true

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        return enabledServices.any { it.resolveInfo.serviceInfo.packageName == requireContext().packageName }
    }

    private fun isBatteryOptimizationIgnored(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.isIgnoringBatteryOptimizations(requireContext().packageName)
        } else true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
