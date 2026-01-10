package com.example.timelimit.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
        // Back listener
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        // Switch listeners
        binding.switch5Min.setOnCheckedChangeListener { _, isChecked -> }
        binding.switchLimitReached.setOnCheckedChangeListener { _, isChecked -> }
        binding.switch1Hour.setOnCheckedChangeListener { _, isChecked -> }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}