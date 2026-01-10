package com.example.timelimit.ui.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.timelimit.R
import com.example.timelimit.databinding.FragmentSecurityBinding

class SecurityFragment : Fragment() {

    private var _binding: FragmentSecurityBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecurityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
    }

    private fun setupListeners() {
        // Back & Close - explicitly navigate to apps fragment
        binding.btnBack.setOnClickListener { 
            findNavController().navigate(R.id.navigation_apps) 
        }
        binding.btnClose.setOnClickListener { 
            findNavController().navigate(R.id.navigation_apps) 
        }

        // Switch listeners
        binding.switchPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) Toast.makeText(context, "Parol funksiyasi yoqildi", Toast.LENGTH_SHORT).show()
        }
        
        binding.switchFingerprint.setOnCheckedChangeListener { _, isChecked -> }
        binding.switchAutoLock.setOnCheckedChangeListener { _, isChecked -> }
        binding.switchEmergency.setOnCheckedChangeListener { _, isChecked -> }
        binding.switchDataPrivacy.setOnCheckedChangeListener { _, isChecked -> }

        // Enable All
        binding.btnEnableAll.setOnClickListener {
            binding.switchPassword.isChecked = true
            binding.switchFingerprint.isChecked = true
            binding.switchAutoLock.isChecked = true
            binding.switchEmergency.isChecked = true
            binding.switchDataPrivacy.isChecked = true
            Toast.makeText(context, "Barcha xavfsizlik funksiyalari yoqildi", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
