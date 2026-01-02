package com.example.timelimit.ui.settings

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
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

        setupUI()
        handleArguments()
    }

    private fun setupUI() {
        // Yopish tugmasi
        binding.btnClose.setOnClickListener {
            if (binding.layoutLanguageExpandable.isVisible) {
                closeLanguageSection()
            } else {
                findNavController().navigateUp()
            }
        }

        // Layout animatsiyasini yoqish
        binding.settingsContentContainer.layoutTransition = LayoutTransition()
        
        // TIL bo'limini ochish
        binding.layoutLanguageHeader.setOnClickListener {
            openLanguageSection()
        }

        // OK tugmasi
        binding.btnSaveLanguage.setOnClickListener {
            val selectedLanguage = when (binding.rgLanguages.checkedRadioButtonId) {
                R.id.rb_uzbek -> "O'zbekcha (Lotin)"
                R.id.rb_uzbek_cyrl -> "O'zbekcha (Kirill)"
                R.id.rb_russian -> "Русский"
                R.id.rb_english -> "English"
                R.id.rb_turkish -> "Türkçe"
                else -> "O'zbekcha"
            }
            binding.tvCurrentLanguage.text = selectedLanguage
            
            // Sozlamalardan chiqish
            findNavController().navigateUp()
            
            // Navbar (Drawer) ni ochish
            requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)?.openDrawer(GravityCompat.START)
        }
        
        // Bildirishnomalar
        binding.layoutNotificationsHeader.setOnClickListener {
            // TODO: Bildirishnomalar sozlamalarini ochish
        }
    }

    private fun openLanguageSection() {
        binding.cardNotifications.isVisible = false
        binding.layoutLanguageHeader.isVisible = false
        binding.layoutLanguageExpandable.isVisible = true
        binding.tvTitle.text = "TIL"
        binding.tvSubtitle.isVisible = false
        binding.btnClose.setImageResource(R.drawable.ic_arrow_back)
    }

    private fun closeLanguageSection() {
        binding.layoutLanguageExpandable.isVisible = false
        binding.cardNotifications.isVisible = true
        binding.layoutLanguageHeader.isVisible = true
        binding.tvTitle.text = "SOZLAMALAR"
        binding.tvSubtitle.isVisible = true
        binding.btnClose.setImageResource(R.drawable.ic_close)
    }

    private fun handleArguments() {
        val targetSection = arguments?.getString("target_section")
        if (targetSection == "language") {
            openLanguageSection()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}