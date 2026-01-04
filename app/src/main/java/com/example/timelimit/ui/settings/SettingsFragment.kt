package com.example.timelimit.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
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
    }

    private fun setupUI() {
        // Orqaga tugmasi
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

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
            
            Toast.makeText(requireContext(), "$selectedLanguage tanlandi", Toast.LENGTH_SHORT).show()
            
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}