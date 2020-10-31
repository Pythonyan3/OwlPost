package com.example.owlpost.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.owlpost.databinding.FragmentMailboxBinding

class MailboxFragment : Fragment() {
    private lateinit var binding: FragmentMailboxBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMailboxBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
    }
}