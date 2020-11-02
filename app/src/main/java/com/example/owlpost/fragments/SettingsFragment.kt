package com.example.owlpost.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.owlpost.databinding.FragmentSettingsBinding
import com.example.owlpost.ui.MailDrawer


class SettingsFragment(private val drawer: MailDrawer) : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        drawer.disableDrawer()
    }

    override fun onStop() {
        super.onStop()
        drawer.enableDrawer()
        drawer.refreshTitle()
    }
}