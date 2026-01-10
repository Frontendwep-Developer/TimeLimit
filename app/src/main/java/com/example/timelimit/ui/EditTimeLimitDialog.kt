package com.example.timelimit.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.timelimit.AppsViewModel
import com.example.timelimit.databinding.DialogEditTimeLimitBinding
import com.example.timelimit.model.AppInfo
import com.google.android.material.button.MaterialButton

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
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        binding.tvEditTitle.text = appInfo.appName

        val hours = appInfo.limitTime / 60
        val minutes = appInfo.limitTime % 60
        
        binding.etHoursEdit.setText(hours.toString().padStart(2, '0'))
        binding.etMinutesEdit.setText(minutes.toString().padStart(2, '0'))

        val totalMinutes = appInfo.limitTime
        when (totalMinutes) {
            15 -> setCheckedButton(binding.btn15MinEdit)
            30 -> setCheckedButton(binding.btn30MinEdit)
            45 -> setCheckedButton(binding.btn45MinEdit)
        }
    }

    private fun setupClickListeners() {
        binding.btnCloseEditDialog.setOnClickListener { dismiss() }

        binding.btn15MinEdit.setOnClickListener { 
            updateCustomTimeFields(15) 
            setCheckedButton(binding.btn15MinEdit)
        }
        binding.btn30MinEdit.setOnClickListener { 
            updateCustomTimeFields(30)
            setCheckedButton(binding.btn30MinEdit)
        }
        binding.btn45MinEdit.setOnClickListener { 
            updateCustomTimeFields(45)
            setCheckedButton(binding.btn45MinEdit)
        }

        binding.btnSaveLimit.setOnClickListener {
            saveLimit()
        }
        
        binding.btnSaveLimit.setOnLongClickListener {
            viewModel.removeAppLimit(appInfo.packageName)
            Toast.makeText(requireContext(), "Limit o'chirildi", Toast.LENGTH_SHORT).show()
            dismiss()
            true
        }
    }
    
    private fun setCheckedButton(selectedButton: MaterialButton) {
        binding.btn15MinEdit.isChecked = false
        binding.btn30MinEdit.isChecked = false
        binding.btn45MinEdit.isChecked = false
        selectedButton.isChecked = true
    }

    private fun saveLimit() {
        val hoursStr = binding.etHoursEdit.text.toString().trim()
        val minutesStr = binding.etMinutesEdit.text.toString().trim()
        
        val hours = hoursStr.toIntOrNull() ?: 0
        val minutes = minutesStr.toIntOrNull() ?: 0
        
        // LIMIT TEKSHIRUVI:
        // 1. Soat 2 dan oshmasligi kerak
        if (hours > 2) {
            Toast.makeText(requireContext(), "Maksimal limit 2 soat!", Toast.LENGTH_SHORT).show()
            binding.etHoursEdit.error = "Max 2"
            return
        }
        
        // 2. Agar aniq 2 soat bo'lsa, daqiqa 0 bo'lishi shart
        if (hours == 2 && minutes > 0) {
             Toast.makeText(requireContext(), "Maksimal limit 2 soat!", Toast.LENGTH_SHORT).show()
             binding.etMinutesEdit.error = "0 bo'lishi kerak"
             return
        }
        
        // 3. Daqiqa 59 dan oshmasligi kerak (ihtiyot shart)
        if (minutes > 59) {
            Toast.makeText(requireContext(), "Daqiqa 59 dan oshmasligi kerak!", Toast.LENGTH_SHORT).show()
            binding.etMinutesEdit.error = "Xato"
            return
        }

        val totalMinutes = (hours * 60) + minutes

        if (totalMinutes > 0) {
            viewModel.setAppLimit(appInfo.packageName, totalMinutes)
            dismiss()
        } else {
            viewModel.removeAppLimit(appInfo.packageName)
            Toast.makeText(requireContext(), "Limit o'chirildi", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun updateCustomTimeFields(totalMinutes: Int) {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        binding.etHoursEdit.setText(hours.toString().padStart(2, '0'))
        binding.etMinutesEdit.setText(minutes.toString().padStart(2, '0'))
        
        // Errorlarni tozalash
        binding.etHoursEdit.error = null
        binding.etMinutesEdit.error = null
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