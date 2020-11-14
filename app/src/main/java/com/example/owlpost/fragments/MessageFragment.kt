package com.example.owlpost.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.owlpost.R
import com.example.owlpost.databinding.FragmentMailboxBinding
import com.example.owlpost.databinding.FragmentMessageBinding


class MessageFragment : Fragment() {
    private lateinit var binding: FragmentMessageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMessageBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
}