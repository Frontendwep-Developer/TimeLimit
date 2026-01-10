package com.example.timelimit.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timelimit.databinding.DialogAppSelectionBinding
import com.example.timelimit.databinding.ItemAppSelectionBinding
import com.example.timelimit.model.AppInfo
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AppSelectionDialog(
    private val availableApps: List<AppInfo>,
    private val onAppSelected: (AppInfo) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogAppSelectionBinding? = null
    private val binding get() = _binding!!
    
    private var selectedApp: AppInfo? = null

    companion object {
        const val TAG = "AppSelectionDialog"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAppSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AppSelectionAdapter(availableApps) { app ->
            // Ilova tanlanganda (bosilganda)
            selectedApp = app
            binding.btnAddSelectedApp.isEnabled = true
        }

        binding.rvAppSelection.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAppSelection.adapter = adapter
        
        binding.btnClose.setOnClickListener { dismiss() }
        
        // "Qo'shish" tugmasi bosilganda
        binding.btnAddSelectedApp.setOnClickListener {
            selectedApp?.let { app ->
                onAppSelected(app)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class AppSelectionAdapter(
        private val apps: List<AppInfo>,
        private val onClick: (AppInfo) -> Unit
    ) : RecyclerView.Adapter<AppSelectionAdapter.ViewHolder>() {

        private var selectedPosition = -1

        inner class ViewHolder(val binding: ItemAppSelectionBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemAppSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = apps[position]
            holder.binding.ivAppIcon.setImageDrawable(app.icon)
            holder.binding.tvAppName.text = app.appName
            
            // Checkbox holati
            holder.binding.rbSelected.isChecked = (position == selectedPosition)

            holder.binding.root.setOnClickListener { 
                val previousPosition = selectedPosition
                selectedPosition = holder.adapterPosition
                
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                
                onClick(app)
            }
        }

        override fun getItemCount(): Int = apps.size
    }
}