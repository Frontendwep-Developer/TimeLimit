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
import com.example.timelimit.ui.AppSelectionDialog

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
        setupListeners()
        
        // Boshida faqat loading ko'rsatamiz
        showLoadingState()
        
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = AppsAdapter { appInfo ->
            if (appInfo.limitTime > 0) {
                showEditTimeLimitDialog(appInfo)
            } else {
                showAddTimeLimitDialog(appInfo)
            }
        }
        binding.rvApps.layoutManager = LinearLayoutManager(requireContext())
        binding.rvApps.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnAddAppLarge.setOnClickListener {
            showAppSelectionDialog()
        }
        
        binding.fabAddApp.setOnClickListener {
            showAppSelectionDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.appsList.observe(viewLifecycleOwner) { allApps ->
            // Faqat limiti bor yoki bloklangan ilovalarni filtrlab ko'rsatamiz
            val activeApps = allApps.filter { it.isLimited || it.isBlocked }
            adapter.submitList(activeApps)
            
            updateUIState(activeApps.isEmpty())
        }
    }

    private fun showLoadingState() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvApps.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.GONE
        binding.btnAddAppLarge.visibility = View.GONE
        binding.fabAddApp.visibility = View.GONE
    }

    private fun updateUIState(isEmpty: Boolean) {
        binding.progressBar.visibility = View.GONE
        if (isEmpty) {
            binding.rvApps.visibility = View.GONE
            binding.fabAddApp.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.btnAddAppLarge.visibility = View.VISIBLE
        } else {
            binding.rvApps.visibility = View.VISIBLE
            binding.fabAddApp.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
            binding.btnAddAppLarge.visibility = View.GONE
        }
    }

    private fun showAppSelectionDialog() {
        val allApps = viewModel.appsList.value ?: emptyList()
        val availableApps = allApps.filter { !it.isLimited && !it.isBlocked }
        
        val dialog = AppSelectionDialog(availableApps) { selectedApp ->
            showAddTimeLimitDialog(selectedApp)
        }
        dialog.show(parentFragmentManager, AppSelectionDialog.TAG)
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