package com.example.timelimit.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.timelimit.R
import com.example.timelimit.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AboutViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()

        // Restore scroll position
        binding.aboutScrollView.post { binding.aboutScrollView.scrollTo(0, viewModel.scrollY) }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { goBackToMenu() }
        binding.btnClose.setOnClickListener { closeScreen() }

        binding.textTerms.setOnClickListener {
            findNavController().navigate(R.id.action_aboutFragment_to_termsFragment)
        }
        binding.textPolicy.setOnClickListener {
            findNavController().navigate(R.id.action_aboutFragment_to_policyFragment)
        }
        binding.textLicense.setOnClickListener {
            findNavController().navigate(R.id.action_aboutFragment_to_licenseFragment)
        }
    }

    override fun onPause() {
        super.onPause()
        // Save scroll position
        viewModel.scrollY = binding.aboutScrollView.scrollY
    }

    private fun goBackToMenu() {
        findNavController().navigateUp()
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)?.openDrawer(GravityCompat.START)
    }

    private fun closeScreen() {
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}