package com.example.timelimit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.timelimit.AppsViewModel
import com.example.timelimit.databinding.DialogEditTimeLimitBinding
import com.example.timelimit.model.AppInfo

class EditTimeLimitDialog(private val appInfo: AppInfo) : DialogFragment() {

    private var _binding: DialogEditTimeLimitBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditTimeLimitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        val usageInMinutes = appInfo.usageTime / 60
        val limitInMinutes = appInfo.limitTime

        binding.tvEditTitle.text = "${appInfo.appName} limitini o'zgartirish"
        binding.tvCurrentUsageEdit.text = "$usageInMinutes/$limitInMinutes daqiqa"

        if (limitInMinutes > 0) {
            val limitInSeconds = limitInMinutes * 60
            val progress = (appInfo.usageTime * 100 / limitInSeconds).toInt().coerceAtMost(100)
            binding.progressUsageEdit.progress = progress
        } else {
            binding.progressUsageEdit.progress = 0
        }

        val hours = appInfo.limitTime / 60
        val minutes = appInfo.limitTime % 60
        binding.etHoursEdit.setText(hours.toString())
        binding.etMinutesEdit.setText(minutes.toString())
    }

    private fun setupClickListeners() {
        binding.btnCloseEditDialog.setOnClickListener { dismiss() }
        binding.btnCancelEdit.setOnClickListener { dismiss() }

        // Quick Update Buttons
        binding.btn45MinEdit.setOnClickListener { updateCustomTimeFields(45) }
        binding.btn1HourEdit.setOnClickListener { updateCustomTimeFields(60) }
        binding.btn90MinEdit.setOnClickListener { updateCustomTimeFields(90) }

        // Delete Button now opens the UnblockAppDialog
        binding.btnDeleteLimit.setOnClickListener {
            val unblockDialog = UnblockAppDialog(appInfo)
            unblockDialog.show(parentFragmentManager, UnblockAppDialog.TAG)
            dismiss() // Close the current edit dialog
        }

        // Save Button
        binding.btnSaveLimit.setOnClickListener {
            val hours = binding.etHoursEdit.text.toString().toIntOrNull() ?: 0
            val minutes = binding.etMinutesEdit.text.toString().toIntOrNull() ?: 0
            val totalMinutes = (hours * 60) + minutes

            if (totalMinutes > 0) {
                viewModel.setAppLimit(appInfo.packageName, totalMinutes)
                dismiss()
            } else {
                // If user sets time to 0, it's equivalent to deleting the limit
                viewModel.removeAppLimit(appInfo.packageName)
                dismiss()
            }
        }
    }

    private fun updateCustomTimeFields(totalMinutes: Int) {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        binding.etHoursEdit.setText(hours.toString())
        binding.etMinutesEdit.setText(minutes.toString())
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "EditTimeLimitDialog"
    }
}