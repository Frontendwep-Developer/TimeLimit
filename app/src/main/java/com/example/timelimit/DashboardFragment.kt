package com.example.timelimit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timelimit.databinding.FragmentDashboardBinding
import com.example.timelimit.ui.dashboard.BlockedAppsAdapter

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppsViewModel by activityViewModels()
    private lateinit var blockedAppsAdapter: BlockedAppsAdapter

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
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        blockedAppsAdapter = BlockedAppsAdapter()
        binding.rvBlockedApps.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBlockedApps.adapter = blockedAppsAdapter
    }

    private fun observeViewModel() {
        viewModel.totalUsageTimeFormatted.observe(viewLifecycleOwner) {
            // Updated to match new layout ID
            binding.tvUsedTime.text = it
        }

        // Most used app logic removed as UI elements were removed in the new design
//        viewModel.mostUsedApp.observe(viewLifecycleOwner) {
//            it?.let {
//                binding.ivMostUsedAppIcon.setImageDrawable(it.icon)
//                binding.tvMostUsedAppName.text = it.appName
//            }
//        }

        viewModel.blockedApps.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                binding.tvNoBlockedApps.visibility = View.VISIBLE
                binding.rvBlockedApps.visibility = View.GONE
            } else {
                binding.tvNoBlockedApps.visibility = View.GONE
                binding.rvBlockedApps.visibility = View.VISIBLE
                blockedAppsAdapter.submitList(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}