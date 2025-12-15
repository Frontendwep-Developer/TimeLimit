package com.example.timelimit

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.timelimit.R
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
        val appInfo = getItem(position)
        holder.bind(appInfo, onItemClick)
    }

    class ViewHolder(private val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(appInfo: AppInfo, onItemClick: (AppInfo) -> Unit) {
            val context = binding.root.context

            binding.ivAppIcon.setImageDrawable(appInfo.icon)
            binding.tvAppName.text = appInfo.appName

            if (appInfo.limitTime > 0) {
                val usageInMinutes = appInfo.usageTime / 60
                val limitInMinutes = appInfo.limitTime

                binding.tvUsageTime.text = "${usageInMinutes} / ${limitInMinutes} min"
                binding.progressUsage.visibility = View.VISIBLE

                val progress = (appInfo.usageTime * 100 / (limitInMinutes * 60)).toInt().coerceAtMost(100)
                binding.progressUsage.progress = progress

                val isBlocked = usageInMinutes >= limitInMinutes

                if (isBlocked) {
                    binding.progressUsage.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red))
                    binding.btnEditLimit.setImageResource(R.drawable.ic_lock_open)
                } else {
                    binding.progressUsage.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green))
                    binding.btnEditLimit.setImageResource(R.drawable.ic_edit)
                }

            } else {
                binding.tvUsageTime.text = context.getString(R.string.no_limit_set)
                binding.progressUsage.visibility = View.GONE
                binding.btnEditLimit.setImageResource(R.drawable.ic_edit)
            }

            binding.root.setOnClickListener { onItemClick(appInfo) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem == newItem
        }
    }
}