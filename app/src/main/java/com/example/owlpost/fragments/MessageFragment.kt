package com.example.owlpost.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
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
import com.example.owlpost.models.SettingsException
import com.example.owlpost.models.cryptography.ENCRYPT_KEY
import com.example.owlpost.models.cryptography.SIGN_KEY
import com.example.owlpost.models.email.OwlMessage
import com.example.owlpost.models.email.SMTPManager
import com.example.owlpost.ui.*
import com.example.owlpost.ui.adapters.RecyclerReceivedAttachmentsAdapter
import com.example.owlpost.ui.widgets.LoadingDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_send_mail.*
import kotlinx.android.synthetic.main.fragment_message.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.security.InvalidKeyException
import java.text.DateFormat
import javax.mail.AuthenticationFailedException
import javax.mail.BodyPart
import javax.mail.MessagingException
import javax.mail.internet.MimeUtility


class MessageFragment : Fragment() {
    private lateinit var binding: FragmentMessageBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var message: OwlMessage
    private lateinit var attachmentToSave: BodyPart
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var acceptRequestDialog: AlertDialog.Builder
    private lateinit var onlyImportDialog: AlertDialog.Builder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessageBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = activity as MainActivity
        message = mainActivity.selectedMessage.copy()
        loadingDialog = LoadingDialog(mainActivity)
        acceptRequestDialog = createConfirmAlertDialog(mainActivity)
        onlyImportDialog = createConfirmAlertDialog(mainActivity)
        setConfirmDialogButton()
        updateData()
    }

    private fun checkRequestResponse() {
        if (message.isExchangeRequest){
            acceptRequestDialog.setMessage(getString(R.string.accept_request_dialog_message, message.from))
            acceptRequestDialog.show()
        }
        else if (message.isExchangeResponse){
            acceptRequestDialog.setMessage(getString(R.string.accept_response_dialog_message, message.from))
            acceptRequestDialog.show()
        }
    }

    override fun onStart() {
        super.onStart()
        mainActivity.drawer.disableDrawer()
        mainActivity.toolbar.title = ""
    }

    override fun onStop() {
        super.onStop()
        mainActivity.drawer.enableDrawer()
    }

    private fun updateData() {
        CoroutineScope(Dispatchers.Main).launch {
            loadingDialog.setTitle(getString(R.string.loading_title_decrypt))
            loadingDialog.show()
            updateSignInfo()
            updateEncryptedInfo()

            messageSubject.text =
                if (message.subject.isNotEmpty()) message.subject else getString(R.string.no_subject)
            fromIcon.text = message.from.substring(0 until 1)
            mainActivity.messageEmailColors[message.from]?.let { color ->
                (fromIcon.background as GradientDrawable).setColor(color)
            }
            messageFrom.text = message.from
            messageDate.text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(message.date)
            messageTo.text = getString(R.string.message_to_prefix, message.to.joinToString("\n"))

            val adapter = RecyclerReceivedAttachmentsAdapter(message.attachmentParts)
            adapter.onDownloadClickListener = object: RecyclerReceivedAttachmentsAdapter.OnDownloadClickListener{
                override fun onDownloadClick(bodyPart: BodyPart) {
                    attachmentToSave = bodyPart
                    showFileCreateIntent()
                }
            }
            receivedAttachments?.adapter = adapter
            receivedAttachments?.layoutManager = LinearLayoutManager(this@MessageFragment.context)

            showMessageBody()
            loadingDialog.dismiss()
            checkRequestResponse()
            if (!message.seen)
                mainActivity.mailbox.markMessageSeen(message.uid)
        }
    }

    private suspend fun updateEncryptedInfo(){
        if (message.to.contains(mainActivity.activeUser.email) && message.encrypted){
            try{
                owlEncryption.visibility = View.VISIBLE
                owlEncryptionIcon.visibility = View.VISIBLE
                message.decrypt(mainActivity.settings.getPrivateKey(mainActivity.activeUser.email, ENCRYPT_KEY))
            }
            catch (e: InvalidKeyException){
                mainActivity.shortToast(getString(R.string.cannot_decrypt))
            }
        }
    }

    private suspend fun updateSignInfo(){
        if (message.to.contains(mainActivity.activeUser.email) && message.signed){
            owlSign.visibility = View.VISIBLE
            owlSignIcon.visibility = View.VISIBLE
            try {
                val publicKey = mainActivity.settings.getSubscriberPublicKey(
                    mainActivity.activeUser.email,
                    message.from,
                    SIGN_KEY
                )
                if (message.verify(publicKey)){
                    owlSign.text = getString(R.string.message_sign_verified)
                    owlSignIcon.setBackgroundResource(R.drawable.ic_verified)
                }
                else{
                    owlSign.text = getString(R.string.message_sign_unverified)
                    owlSignIcon.setBackgroundResource(R.drawable.ic_unverified)
                }
            }
            catch (e: SettingsException){
                mainActivity.shortToast(getString(R.string.no_subscriber_key, message.from))
            }
        }
        else {
            owlSign.visibility = View.GONE
            owlSignIcon.visibility = View.GONE
        }
    }

    private fun showMessageBody(){
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

    private fun setConfirmDialogButton() {
        acceptRequestDialog.setNegativeButton(getString(R.string.dialog_no)) { _: DialogInterface, _: Int ->
            if (message.isExchangeRequest) {
                onlyImportDialog.setMessage(
                    getString(
                        R.string.only_import_keys_dialog_message,
                        message.from
                    )
                )
                onlyImportDialog.setPositiveButton(getString(R.string.dialog_yes)) { _: DialogInterface, _: Int ->
                    mainActivity.settings.putSubscriberKeys(
                        mainActivity.activeUser.email,
                        message.from,
                        message.exchangeEncryptionKey,
                        message.exchangeSignKey
                    )
                }
                onlyImportDialog.show()
            }
        }
        acceptRequestDialog.setPositiveButton(getString(R.string.dialog_yes)) { _: DialogInterface, _: Int ->
            if (message.isExchangeResponse){ // only save subscriber keys
                mainActivity.settings.putSubscriberKeys(
                    mainActivity.activeUser.email,
                    message.from,
                    message.exchangeEncryptionKey,
                    message.exchangeSignKey
                )
                mainActivity.shortToast(getString(R.string.subscriber_keys_import_success, message.from))
            }
            else if (message.isExchangeRequest){ // save subscriber keys and send own keys
                CoroutineScope(Dispatchers.Main).launch {
                    loadingDialog.setTitle(getString(R.string.loading_title_response))
                    loadingDialog.show()
                    mainActivity.settings.putSubscriberKeys(
                        mainActivity.activeUser.email,
                        message.from,
                        message.exchangeEncryptionKey,
                        message.exchangeSignKey
                    )

                    val publicEncryptionKey = mainActivity.settings.getPublicKeyString(
                        mainActivity.activeUser.email,
                        ENCRYPT_KEY
                    )
                    val publicSignKey = mainActivity.settings.getPublicKeyString(
                        mainActivity.activeUser.email,
                        SIGN_KEY
                    )
                    val managerSMTP = SMTPManager(mainActivity.activeUser)
                    val message = managerSMTP.getSendExchangeResponseMessage(
                        message.from,
                        publicEncryptionKey,
                        publicSignKey
                    )
                    try {
                        managerSMTP.sendMessage(message)
                        mainActivity.shortToast(getString(R.string.response_sent))
                    }
                    catch (e: AuthenticationFailedException){
                        mainActivity.shortToast(getString(R.string.auth_error))
                    }
                    catch (e: MessagingException){
                        Snackbar.make(
                            mainActivity.fragment_container,
                            getString(R.string.internet_connection),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    finally {
                        loadingDialog.dismiss()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null){
            when (requestCode){
                SAVE_ATTACHMENT_REQUEST_CODE -> {
                    try {
                        val fis = attachmentToSave.inputStream ?: throw FileNotFoundException("")
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE)
            if ((grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                showFileCreateIntent()
            }
    }

    private fun showFileCreateIntent() {
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
            intent.type =
                attachmentToSave.contentType.substring(0 until attachmentToSave.contentType.indexOf(";"))
            intent.putExtra(Intent.EXTRA_TITLE, MimeUtility.decodeText(attachmentToSave.fileName))
            startActivityForResult(intent, SAVE_ATTACHMENT_REQUEST_CODE)
        }
    }
}