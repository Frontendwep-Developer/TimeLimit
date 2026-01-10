package com.example.timelimit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.timelimit.databinding.FragmentStatisticsBinding

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
                viewModel.selectApp(apps.first().packageName)
            }
        }

        viewModel.selectedAppInfo.observe(viewLifecycleOwner) { appInfo ->
            // Markaziy toolbarni yangilash
            activity?.findViewById<TextView>(R.id.toolbar_title)?.text = "${appInfo.appName.uppercase()} STATISTIKASI"
            viewModel.loadWeeklyStatsForApp(appInfo.packageName)
        }

        viewModel.weeklyTotalUsage.observe(viewLifecycleOwner) { 
             val minutes = it / 60
             val seconds = it % 60
             binding.totalTimeTextview.text = "${minutes}m ${seconds}s" 
        }
        viewModel.dailyUsageData.observe(viewLifecycleOwner) { binding.barChartView.setData(it) }
        
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
