package com.example.timelimit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.timelimit.AppsViewModel
import com.example.timelimit.R
import com.example.timelimit.databinding.DialogUnblockAppBinding
import com.example.timelimit.model.AppInfo

class UnblockAppDialog(private val appInfo: AppInfo) : DialogFragment() {

    private var _binding: DialogUnblockAppBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogUnblockAppBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Correctly format the string resource with the app name
        binding.tvUnblockTitle.text = getString(R.string.unblock_dialog_title, appInfo.appName)

        binding.btnUnblockConfirm.setOnClickListener {
            // "Ha" means remove the limit entirely
            viewModel.removeAppLimit(appInfo.packageName)
            // Dismiss both this and the previous dialog if it's still open
            parentFragmentManager.findFragmentByTag(EditTimeLimitDialog.TAG)?.let {
                (it as? DialogFragment)?.dismiss()
            }
            dismiss()
        }

        binding.btnUnblockCancel.setOnClickListener { dismiss() }
        binding.btnCloseUnblockDialog.setOnClickListener { dismiss() }
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
        const val TAG = "UnblockAppDialog"
    }
}