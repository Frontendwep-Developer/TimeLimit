package com.example.timelimit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.timelimit.AppsViewModel
import com.example.timelimit.databinding.DialogAddTimeLimitBinding
import com.example.timelimit.model.AppInfo

class TimeLimitDialog(private val appInfo: AppInfo) : DialogFragment() {

    private var _binding: DialogAddTimeLimitBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddTimeLimitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Views
        binding.tvAppNameDialog.text = appInfo.appName
        binding.ivAppIconDialog.setImageDrawable(appInfo.icon)

        // Quick Time Buttons now update the input fields
        binding.btn15Min.setOnClickListener { updateCustomTimeFields(15) }
        binding.btn30Min.setOnClickListener { updateCustomTimeFields(30) }
        binding.btn1Hour.setOnClickListener { updateCustomTimeFields(60) }
        binding.btn2Hours.setOnClickListener { updateCustomTimeFields(120) }

        // The "ADD TIME LIMIT" button is the only one that saves the limit
        binding.btnAddTimeLimit.setOnClickListener {
            val hours = binding.etHours.text.toString().toIntOrNull() ?: 0
            val minutes = binding.etMinutes.text.toString().toIntOrNull() ?: 0
            val totalMinutes = (hours * 60) + minutes

            if (totalMinutes > 0) {
                setLimitAndDismiss(totalMinutes)
            } else {
                Toast.makeText(requireContext(), "Iltimos, vaqt kiriting", Toast.LENGTH_SHORT).show()
            }
        }

        // Close Button
        binding.btnCloseDialog.setOnClickListener {
            dismiss()
        }
    }

    private fun updateCustomTimeFields(totalMinutes: Int) {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        binding.etHours.setText(hours.toString())
        binding.etMinutes.setText(minutes.toString())
    }

    private fun setLimitAndDismiss(minutes: Int) {
        viewModel.setAppLimit(appInfo.packageName, minutes)
        dismiss()
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
        const val TAG = "TimeLimitDialog"
    }
}