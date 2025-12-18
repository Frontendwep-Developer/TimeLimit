package com.example.timelimit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.timelimit.databinding.FragmentStatisticsBinding

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: StatisticsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(StatisticsViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()

        // TODO: Replace with dynamic package name and limit
        viewModel.loadWeeklyStatsForApp("com.instagram.android", 60L)
    }

    private fun observeViewModel() {
        viewModel.weeklyTotalUsage.observe(viewLifecycleOwner) {
            binding.totalTimeTextview.text = it
        }

        viewModel.todayUsage.observe(viewLifecycleOwner) {
            binding.dailyLimitTextview.text = it
        }

        viewModel.remainingTime.observe(viewLifecycleOwner) {
            binding.remainingTimeChip.text = it
        }

        viewModel.dailyProgress.observe(viewLifecycleOwner) {
            binding.progressBar.progress = it
        }

        viewModel.openingsCount.observe(viewLifecycleOwner) {
            binding.openingsCountTextview.text = it
        }

        viewModel.averageTimeChange.observe(viewLifecycleOwner) {
            binding.averageTimeChangeTextview.text = it
        }

        viewModel.dailyUsageData.observe(viewLifecycleOwner) { dailyUsage ->
            binding.barChartView.setData(dailyUsage)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
