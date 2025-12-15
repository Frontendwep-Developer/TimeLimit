package com.example.timelimit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timelimit.databinding.FragmentAppsBinding
import com.example.timelimit.model.AppInfo
import com.example.timelimit.ui.EditTimeLimitDialog
import com.example.timelimit.ui.TimeLimitDialog

class AppsFragment : Fragment() {

    private var _binding: FragmentAppsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppsViewModel by activityViewModels()
    
    private lateinit var adapter: AppsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        if (viewModel.appsList.value.isNullOrEmpty()) {
            viewModel.loadInstalledApps(requireContext())
        }
    }

    private fun setupRecyclerView() {
        adapter = AppsAdapter { appInfo ->
            // Decide which dialog to show based on whether a limit is already set
            if (appInfo.limitTime > 0) {
                showEditTimeLimitDialog(appInfo)
            } else {
                showAddTimeLimitDialog(appInfo)
            }
        }
        binding.rvApps.layoutManager = LinearLayoutManager(requireContext())
        binding.rvApps.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.appsList.observe(viewLifecycleOwner) { apps ->
            Log.d("AppsFragment", "appsList observer triggered with ${apps.size} apps")
            adapter.submitList(apps)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d("AppsFragment", "isLoading observer triggered: $isLoading")
            // You can show a loading spinner here if needed
        }
    }

    private fun showAddTimeLimitDialog(appInfo: AppInfo) {
        val dialog = TimeLimitDialog(appInfo)
        dialog.show(parentFragmentManager, TimeLimitDialog.TAG)
    }

    private fun showEditTimeLimitDialog(appInfo: AppInfo) {
        val dialog = EditTimeLimitDialog(appInfo)
        dialog.show(parentFragmentManager, EditTimeLimitDialog.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
