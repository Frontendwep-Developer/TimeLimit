package com.example.timelimit.ui.settings

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.timelimit.R
import com.example.timelimit.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupLanguageSettings()
        setupPermissions()
        setupOtherSettings()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionSwitches()
    }

    private fun setupPermissions() {
        // Usage Access (Foydalanish statistikasi)
        binding.switchUsagePermission.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        // Switch bosilganda ham, layout bosilganda ham ishlashi uchun (agar kerak bo'lsa)
        // Lekin switchning o'zini bosish yetarli.
        // Qo'shimcha UX uchun icon yoki text bosilganda ham ochish mumkin:
        binding.iconUsageAccess.setOnClickListener { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }


        // Draw Overlay (Ilovalar ustida ko'rsatish)
        binding.switchOverlayPermission.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${requireContext().packageName}")
                )
                startActivity(intent)
            }
        }
        binding.iconOverlay.setOnClickListener {
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${requireContext().packageName}")
                )
                startActivity(intent)
            }
        }

        // Accessibility Service
        binding.switchAccessibilityPermission.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
         binding.iconAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun updatePermissionSwitches() {
        binding.switchUsagePermission.isChecked = hasUsageStatsPermission()
        binding.switchOverlayPermission.isChecked = checkOverlayPermission()
        binding.switchAccessibilityPermission.isChecked = isAccessibilityServiceEnabled()
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), requireContext().packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), requireContext().packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(requireContext())
        } else {
            true
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        return enabledServices.any { it.resolveInfo.serviceInfo.packageName == requireContext().packageName }
    }

    private fun setupLanguageSettings() {
        binding.languageButton.setOnClickListener {
            val isVisible = binding.languageAccordionContainer.visibility == View.VISIBLE
            if (isVisible) {
                closeLanguageAccordion()
            } else {
                binding.languageAccordionContainer.visibility = View.VISIBLE
                binding.iconChevronLanguage.animate().rotation(90f).setDuration(200).start()
                
                val currentLang = binding.textLanguageValue.text.toString()
                when (currentLang) {
                    "O'zbek" -> binding.langUz.isChecked = true
                    "English" -> binding.langEn.isChecked = true
                    "Русский" -> binding.langRu.isChecked = true
                }
            }
        }

        binding.btnLangCancel.setOnClickListener {
            closeLanguageAccordion()
        }

        binding.btnLangOk.setOnClickListener {
            val selectedId = binding.languageRadioGroup.checkedRadioButtonId
            if (selectedId != -1) {
                val radioButton = binding.root.findViewById<RadioButton>(selectedId)
                val selectedLang = radioButton.text.toString()
                binding.textLanguageValue.text = selectedLang
                // TODO: Apply language change logic
            }
            closeLanguageAccordion()
        }
    }

    private fun setupOtherSettings() {
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // Handle dark mode toggle
        }
        
        binding.btnFaq.setOnClickListener {
            // Open FAQ
        }
        
        binding.btnTelegram.setOnClickListener {
            // Open Telegram
        }
        
        binding.btnEmail.setOnClickListener {
            // Send Email
        }
        
        binding.btnFeedback.setOnClickListener {
            // Send Feedback
        }
    }

    private fun closeLanguageAccordion() {
        binding.languageAccordionContainer.visibility = View.GONE
        binding.iconChevronLanguage.animate().rotation(0f).setDuration(200).start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}