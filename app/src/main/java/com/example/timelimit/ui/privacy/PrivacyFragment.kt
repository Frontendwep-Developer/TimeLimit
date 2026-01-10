package com.example.timelimit.ui.privacy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.timelimit.R
import com.example.timelimit.databinding.FragmentPrivacyBinding

class PrivacyFragment : Fragment() {

    private var _binding: FragmentPrivacyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrivacyBinding.inflate(inflater, container, false)
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

        // Buttons
        binding.btnDetails.setOnClickListener {
            Toast.makeText(context, "Batafsil ma'lumot", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnDelete.setOnClickListener {
            Toast.makeText(context, "Ma'lumotlar o'chirildi", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnPolicy.setOnClickListener {
            Toast.makeText(context, "Maxfiylik siyosati ochilmoqda...", Toast.LENGTH_SHORT).show()
        }

        // Switch
        binding.switchConsent.setOnCheckedChangeListener { _, isChecked ->
             val message = if(isChecked) "Rozilik berildi" else "Rozilik qaytarib olindi"
             Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
