package com.example.timelimit.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.timelimit.LocaleHelper
import com.example.timelimit.MainActivity
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

        setCurrentLanguageInUI()
        setupListeners()
    }

    private fun setCurrentLanguageInUI() {
        val prefs = requireContext().getSharedPreferences(requireContext().packageName, Context.MODE_PRIVATE)
        val currentLang = prefs.getString("Locale.Helper.Selected.Language", "uz") ?: "uz"
        
        val radioButtonId = when (currentLang) {
            "uz" -> R.id.rb_uzbek
            "en" -> R.id.rb_english
            "ru" -> R.id.rb_russian
            else -> R.id.rb_uzbek
        }
        binding.rgLanguages.check(radioButtonId)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigate(R.id.navigation_apps) }
        binding.btnClose.setOnClickListener { findNavController().navigate(R.id.navigation_apps) }

        binding.btnSaveLanguage.setOnClickListener {
            val languageCode = when (binding.rgLanguages.checkedRadioButtonId) {
                R.id.rb_uzbek -> "uz"
                R.id.rb_english -> "en"
                R.id.rb_russian -> "ru"
                else -> "uz"
            }
            
            LocaleHelper.setLocale(requireContext(), languageCode)
            
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            
            Toast.makeText(requireContext(), getString(R.string.language_changed), Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
