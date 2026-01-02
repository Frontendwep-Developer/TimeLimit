package com.example.timelimit.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.timelimit.R
import com.example.timelimit.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
    }

    private fun setupListeners() {
        // Orqaga qaytish
        binding.btnBack.setOnClickListener {
            goBack()
        }

        // Yopish
        binding.btnClose.setOnClickListener {
            goBack()
        }

        // Switch listeners (Optional: Agar switch o'zgarganda biror narsa qilish kerak bo'lsa)
        binding.switch5Min.setOnCheckedChangeListener { _, isChecked ->
             // TODO: Save preference
        }
        
        binding.switchLimitReached.setOnCheckedChangeListener { _, isChecked ->
             // TODO: Save preference
        }
        
        binding.switch1Hour.setOnCheckedChangeListener { _, isChecked ->
             // TODO: Save preference
        }
    }

    private fun goBack() {
        findNavController().navigateUp()
        // Agar kerak bo'lsa, drawerni ochish:
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)?.openDrawer(GravityCompat.START)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}