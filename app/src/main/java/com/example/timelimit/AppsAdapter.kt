package com.example.timelimit

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
            
            binding.tvBlockedStatus.visibility = if (appInfo.isLimited && appInfo.isBlocked) View.VISIBLE else View.GONE

            if (appInfo.isLimited && appInfo.limitTime > 0) {
                val usageInMinutes = appInfo.usageTime / 60
                binding.tvUsageTime.text = "${usageInMinutes} / ${appInfo.limitTime} min"
                binding.progressUsage.visibility = View.VISIBLE

                val limitInSeconds = (appInfo.limitTime * 60).toFloat()
                val progress = if (limitInSeconds > 0) {
                    ((appInfo.usageTime.toFloat() / limitInSeconds) * 100).toInt()
                } else { 0 }

                binding.progressUsage.progress = progress.coerceAtMost(100)

                // --- Correct way to change progress bar color ---
                val progressDrawable = binding.progressUsage.progressDrawable.mutate() as LayerDrawable
                val progressLayer = progressDrawable.findDrawableByLayerId(android.R.id.progress)
                val colorRes = if (appInfo.isBlocked) R.color.red else R.color.green
                progressLayer.setTint(ContextCompat.getColor(context, colorRes))
                
            } else {
                binding.tvUsageTime.text = context.getString(R.string.no_limit_set)
                binding.progressUsage.visibility = View.GONE
            }

            binding.btnEditLimit.setOnClickListener { onItemClick(appInfo) }
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