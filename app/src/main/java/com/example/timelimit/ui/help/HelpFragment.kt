package com.example.timelimit.ui.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.timelimit.R
import com.example.timelimit.databinding.FragmentHelpBinding

class HelpFragment : Fragment() {

    private var _binding: FragmentHelpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
    }

    private fun setupListeners() {
        // Back & Close - explicitly navigate to apps fragment
        binding.btnBack.setOnClickListener { 
            findNavController().navigate(R.id.navigation_apps) 
        }
        binding.btnClose.setOnClickListener { 
            findNavController().navigate(R.id.navigation_apps) 
        }

        // FAQ Toggles
        setupFaqToggle(binding.faqHeader1, binding.faqAnswer1, binding.faqArrow1)
        setupFaqToggle(binding.faqHeader2, binding.faqAnswer2, binding.faqArrow2)
        setupFaqToggle(binding.faqHeader3, binding.faqAnswer3, binding.faqArrow3)
    }

    private fun setupFaqToggle(header: View, answer: TextView, arrow: View) {
        header.setOnClickListener {
            answer.isVisible = !answer.isVisible
            val rotation = if (answer.isVisible) 180f else 0f
            arrow.animate().rotation(rotation).setDuration(200).start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
