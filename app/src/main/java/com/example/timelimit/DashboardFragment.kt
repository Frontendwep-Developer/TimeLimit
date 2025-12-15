package com.example.timelimit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.timelimit.R
import com.example.timelimit.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupClickListeners()
        loadMockData() // Vaqtinchalik ma'lumotlar
    }

    private fun setupUI() {
        // Setup progress bar
        binding.progressToday.progress = 45

        // Setup app progress bars
        // Instagram: 32/60 = 53%
        // TikTok: 15/30 = 50%
    }

    private fun setupClickListeners() {
        // Add App button
        binding.btnAddApp.setOnClickListener {
            // Navigate to apps screen
            findNavController().navigate(R.id.navigation_apps)
        }

        // View Stats button
        binding.btnViewStats.setOnClickListener {
            // Navigate to statistics screen
            findNavController().navigate(R.id.navigation_statistics)
        }

        // App cards click listeners
        // Will add later when we have actual app list
    }

    private fun loadMockData() {
        // Vaqtinchalik ma'lumotlar
        binding.tvLimitedApps.text = "3"
        binding.tvBlockedApps.text = "1"
        binding.tvTimeSaved.text = "2h 15m"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}