package com.example.timelimit

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.timelimit.databinding.ItemAppBinding
import com.example.timelimit.model.AppInfo

class AppsAdapter(
    private val onItemClick: (AppInfo) -> Unit
) : ListAdapter<AppInfo, AppsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class ViewHolder(private val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(appInfo: AppInfo, onItemClick: (AppInfo) -> Unit) {
            val context = binding.root.context

            binding.ivAppIcon.setImageDrawable(appInfo.icon)
            binding.tvAppName.text = appInfo.appName

            // Limit ma'lumoti
            if (appInfo.isLimited) {
                binding.tvLimitInfo.text = "Limit: ${appInfo.limitTime} daqiqa"
            } else {
                binding.tvLimitInfo.text = "Limit o'rnatilmagan"
            }

            // Foydalanish ma'lumoti
            val usageMinutes = appInfo.usageTime / 60
            binding.tvUsageInfo.text = "Bugun: $usageMinutes daqiqa"

            // Progress Bar va Foiz
            val limitInSeconds = appInfo.limitTime * 60
            val progress = if (limitInSeconds > 0) {
                ((appInfo.usageTime.toFloat() / limitInSeconds) * 100).toInt()
            } else { 0 }
            
            binding.pbCircleUsage.progress = progress.coerceAtMost(100)
            binding.tvProgressPercent.text = "${progress.coerceAtMost(100)}%"

            // Ranglar va status
            if (appInfo.isBlocked) {
                binding.viewStatusIndicator.setBackgroundColor(Color.parseColor("#B3261E")) // Red
                binding.tvProgressPercent.setTextColor(Color.parseColor("#B3261E"))
                binding.ivStatusBadge.setImageResource(R.drawable.ic_lock) // Lock icon
                binding.ivStatusBadge.setColorFilter(Color.parseColor("#B3261E"))
                
                // Progress bar rangini o'zgartirish
                updateProgressColor(Color.parseColor("#B3261E"))
            } else {
                binding.viewStatusIndicator.setBackgroundColor(Color.parseColor("#366938")) // Green
                binding.tvProgressPercent.setTextColor(Color.parseColor("#366938"))
                binding.ivStatusBadge.setImageResource(R.drawable.ic_check_circle)
                binding.ivStatusBadge.setColorFilter(Color.parseColor("#366938"))
                
                updateProgressColor(Color.parseColor("#366938"))
            }

            binding.btnEdit.setOnClickListener { onItemClick(appInfo) }
        }
        
        private fun updateProgressColor(color: Int) {
             val progressDrawable = binding.pbCircleUsage.progressDrawable.mutate() as LayerDrawable
             val progressLayer = progressDrawable.findDrawableByLayerId(android.R.id.progress)
             // LayerDrawable ichidagi RotateDrawable ni topib, uning shape rangini o'zgartirish kerak
             // Oddiyroq usul: Tint ishlatish
             progressLayer.setTint(color)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.isLimited == newItem.isLimited && 
                   oldItem.limitTime == newItem.limitTime &&
                   oldItem.usageTime == newItem.usageTime &&
                   oldItem.isBlocked == newItem.isBlocked
        }
    }
}