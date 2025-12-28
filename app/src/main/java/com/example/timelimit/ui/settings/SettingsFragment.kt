package com.example.timelimit.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        // --- Til Sozlamalari (Accordion) ---
        binding.languageButton.setOnClickListener {
            val isVisible = binding.languageAccordionContainer.visibility == View.VISIBLE
            if (isVisible) {
                // Yopish
                closeLanguageAccordion()
            } else {
                // Ochish
                binding.languageAccordionContainer.visibility = View.VISIBLE
                binding.iconChevronLanguage.animate().rotation(90f).setDuration(200).start()
                
                // Hozirgi tilni belgilab qo'yish
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
                
                // Tanlangan tilni matnga yozish
                binding.textLanguageValue.text = selectedLang
                
                // TODO: Bu yerda ilova tilini o'zgartirish logikasi bo'ladi
            }
            closeLanguageAccordion()
        }

        // --- Boshqa sozlamalar ---
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