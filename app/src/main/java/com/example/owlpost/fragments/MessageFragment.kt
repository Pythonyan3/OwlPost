package com.example.owlpost.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.owlpost.MainActivity
import com.example.owlpost.R
import com.example.owlpost.databinding.FragmentMessageBinding
import com.example.owlpost.models.FilenameAttachment
import com.example.owlpost.models.email.OwlMessage
import com.example.owlpost.ui.PERMISSIONS_REQUEST_CODE
import com.example.owlpost.ui.SAVE_ATTACHMENT_REQUEST_CODE
import com.example.owlpost.ui.adapters.RecyclerReceivedAttachmentsAdapter
import com.example.owlpost.ui.shortToast
import kotlinx.android.synthetic.main.fragment_message.*
import java.io.File
import java.io.FileNotFoundException
import java.text.DateFormat


class MessageFragment : Fragment() {
    private lateinit var binding: FragmentMessageBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var message: OwlMessage
    private lateinit var attachmentToSave: FilenameAttachment

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

        val adapter = RecyclerReceivedAttachmentsAdapter(message.attachmentsFilenames)
        adapter.onDownloadClickListener = object: RecyclerReceivedAttachmentsAdapter.OnDownloadClickListener{
            override fun onDownloadClick(attachment: FilenameAttachment) {
                attachmentToSave = attachment
                showFileCreateIntent(attachment)
            }
        }
        receivedAttachments?.adapter = adapter
        receivedAttachments?.layoutManager = LinearLayoutManager(this.context)

        updateEncryptedInfo()
        updateSignInfo()

        updateContent()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null){
            when (requestCode){
                SAVE_ATTACHMENT_REQUEST_CODE -> {
                    try {
                        val fis = message.getAttachmentInputStream(attachmentToSave.filename)
                            ?: throw FileNotFoundException("")
                        val fos = context?.contentResolver?.openOutputStream(data.data as Uri)
                            ?: throw FileNotFoundException("")
                        fos.write(fis.readBytes())
                        fis.close()
                        fos.close()
                        mainActivity.shortToast(getString(R.string.file_saved_success))
                    }
                    catch (e: FileNotFoundException){
                        mainActivity.shortToast(getString(R.string.file_saved_error))
                    }
                }
            }
        }
    }

    private fun showFileCreateIntent(attachment: FilenameAttachment) {
        if (ContextCompat.checkSelfPermission(
                mainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                mainActivity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = attachment.mimeType
            intent.putExtra(Intent.EXTRA_TITLE, attachment.filename)
            startActivityForResult(intent, SAVE_ATTACHMENT_REQUEST_CODE)
        }
    }
}