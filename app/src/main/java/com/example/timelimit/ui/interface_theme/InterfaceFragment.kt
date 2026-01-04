package com.example.timelimit.ui.interface_theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.timelimit.R
import com.example.timelimit.databinding.FragmentInterfaceBinding

class InterfaceFragment : Fragment() {

    private var _binding: FragmentInterfaceBinding? = null
    private val binding get() = _binding!!

    private var selectedMode: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInterfaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadCurrentTheme()
        setupListeners()
    }

    private fun setupListeners() {
        // Back & Close
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnClose.setOnClickListener { findNavController().navigateUp() }
        binding.btnCancel.setOnClickListener { findNavController().navigateUp() }

        // Click listeners for rows
        binding.layoutLight.setOnClickListener { updateSelection(AppCompatDelegate.MODE_NIGHT_NO) }
        binding.layoutDark.setOnClickListener { updateSelection(AppCompatDelegate.MODE_NIGHT_YES) }
        binding.layoutAuto.setOnClickListener { updateSelection(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) }

        // Save
        binding.btnSave.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(selectedMode)
            Toast.makeText(requireContext(), "Mavzu o'zgartirildi", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun loadCurrentTheme() {
        selectedMode = AppCompatDelegate.getDefaultNightMode()
        if (selectedMode == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) {
            selectedMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        updateSelection(selectedMode)
    }

    private fun updateSelection(mode: Int) {
        selectedMode = mode
        binding.rbLight.isChecked = false
        binding.rbDark.isChecked = false
        binding.rbAuto.isChecked = false

        when (mode) {
            AppCompatDelegate.MODE_NIGHT_NO -> binding.rbLight.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> binding.rbDark.isChecked = true
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> binding.rbAuto.isChecked = true
            else -> binding.rbAuto.isChecked = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}