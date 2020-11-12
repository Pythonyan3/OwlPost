package com.example.owlpost.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.example.owlpost.AddEmailActivity
import com.example.owlpost.MainActivity
import com.example.owlpost.SendMailActivity
import com.example.owlpost.databinding.FragmentMailboxBinding
import com.example.owlpost.ui.ADD_EMAIL_REQUEST_CODE
import com.example.owlpost.ui.MailDrawer
import com.example.owlpost.ui.SEND_EMAIL_REQUEST_CODE
import kotlinx.android.synthetic.main.fragment_mailbox.*

class MailboxFragment: Fragment() {
    private lateinit var binding: FragmentMailboxBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMailboxBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        initViews()
    }

    private fun initViews() {
        newMailActionButton.setOnClickListener{
            val activityMain = activity as MainActivity
            val intent = Intent(activityMain, SendMailActivity::class.java)
            intent.putExtra("email", activityMain.activeUser.email)
            intent.putExtra("password", activityMain.activeUser.password)
            activityMain.startActivityForResult(intent, SEND_EMAIL_REQUEST_CODE)
        }
    }
}