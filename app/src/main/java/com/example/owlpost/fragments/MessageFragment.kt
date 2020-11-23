package com.example.owlpost.fragments

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.owlpost.MainActivity
import com.example.owlpost.R
import com.example.owlpost.databinding.FragmentMessageBinding
import com.example.owlpost.models.email.OwlMessage
import kotlinx.android.synthetic.main.fragment_message.*
import java.text.DateFormat


class MessageFragment : Fragment() {
    private lateinit var binding: FragmentMessageBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var message: OwlMessage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessageBinding.inflate(layoutInflater, container, false)
        mainActivity = activity as MainActivity
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        mainActivity.drawer.disableDrawer()
        mainActivity.toolbar.title = ""
        message = mainActivity.mailbox.currentMessage
        updateData()
    }

    override fun onStop() {
        super.onStop()
        mainActivity.drawer.enableDrawer()
        mainActivity.drawer.updateTitle()
    }

    private fun updateData() {
        messageSubject.text =
            if (message.subject.isNotEmpty()) message.subject else getString(R.string.no_subject)
        fromIcon.text = message.from.substring(0 until 1)
        mainActivity.messageEmailColors[message.from]?.let { color ->
            (fromIcon.background as GradientDrawable).setColor(color)
        }
        messageFrom.text = message.from
        messageDate.text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(message.date)
        messageTo.text = getString(R.string.message_to_prefix, message.to.joinToString("\n"))

        updateEncryptedInfo()
        updateSignInfo()

        updateContent()

        //TODO Show attachments
        //TODO Save attachment
    }

    private fun updateEncryptedInfo(){
        val visibility = if (message.encrypted) View.VISIBLE else View.GONE
        owlEncryption.visibility = visibility
        owlEncryptionIcon.visibility = visibility
    }

    private fun updateSignInfo(){
        val visibility = if (message.signed) View.VISIBLE else View.GONE
        owlSign.visibility = visibility
        owlSignIcon.visibility = visibility
    }

    private fun updateContent(){
        val html = message.html
        if (html.isNotEmpty()){
            messageContentText.visibility = View.GONE
            messageContentHtml.visibility = View.VISIBLE
            messageContentHtml.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
        }
        else{
            messageContentHtml.visibility = View.GONE
            messageContentText.visibility = View.VISIBLE
            messageContentText.text = message.text
        }
    }
}