package com.example.timelimit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.timelimit.databinding.FragmentStatisticsBinding
import com.example.timelimit.model.AppInfoForStats

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatisticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.limitedApps.observe(viewLifecycleOwner) { apps ->
            if (!apps.isNullOrEmpty()) {
                // For now, let's just load the stats for the first app
                viewModel.selectApp(apps.first().packageName)
            }
        }

        viewModel.selectedAppInfo.observe(viewLifecycleOwner) { appInfo ->
            binding.toolbar.title = "${appInfo.appName} - Haftalik"
            viewModel.loadWeeklyStatsForApp(appInfo.packageName)
        }

        viewModel.weeklyTotalUsage.observe(viewLifecycleOwner) { 
             val minutes = it / 60
             val seconds = it % 60
             binding.totalTimeTextview.text = "${minutes}m ${seconds}s" 
        }
        viewModel.dailyUsageData.observe(viewLifecycleOwner) { binding.barChartView.setData(it) }
        viewModel.todayUsage.observe(viewLifecycleOwner) { 
            // Placeholder for daily_limit_textview
        }
        viewModel.remainingTime.observe(viewLifecycleOwner) { 
            val minutes = it / 60
            val seconds = it % 60
            if (minutes > 0) {
                binding.remainingTimeChip.text = "${minutes} daqiqa ${seconds} soniya qoldi"
            } else {
                binding.remainingTimeChip.text = "${seconds} soniya qoldi"
            }
        }
        viewModel.dailyProgress.observe(viewLifecycleOwner) { 
            binding.progressBar.progress = it
        }
        viewModel.openingsCount.observe(viewLifecycleOwner) { 
             binding.openingsCountTextview.text = "$it marta"
        }
        viewModel.averageTimeChange.observe(viewLifecycleOwner) { 
             binding.averageTimeChangeTextview.text = String.format("%.1f%%", it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}