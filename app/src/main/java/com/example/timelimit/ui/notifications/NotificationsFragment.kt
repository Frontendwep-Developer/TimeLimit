package com.example.timelimit.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.timelimit.R
import com.example.timelimit.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
    }

    private fun setupListeners() {
        // Orqaga qaytish tugmasi (<)
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.navigation_apps)
        }

        // Yopish tugmasi (X)
        binding.btnClose.setOnClickListener {
            findNavController().navigate(R.id.navigation_apps)
        }

        // Switch listeners
        binding.switch5Min.setOnCheckedChangeListener { _, isChecked -> }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
