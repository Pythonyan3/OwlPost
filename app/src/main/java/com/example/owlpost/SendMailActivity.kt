package com.example.owlpost


import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.ParcelableSpan
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.toHtml
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.owlpost.models.*
import com.example.owlpost.models.cryptography.ENCRYPT_KEY
import com.example.owlpost.models.cryptography.SIGN_KEY
import com.example.owlpost.models.email.OwlMessage
import com.example.owlpost.models.email.SMTPManager
import com.example.owlpost.ui.*
import com.example.owlpost.ui.adapters.ColorSpinnerAdapter
import com.example.owlpost.ui.adapters.RecyclerSendAttachmentsAdapter
import com.example.owlpost.ui.widgets.LoadingDialog
import com.example.owlpost.ui.widgets.OnSelectionChangedListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_send_mail.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import javax.mail.AuthenticationFailedException
import javax.mail.MessagingException


class SendMailActivity : AppCompatActivity() {
    private lateinit var settings: Settings
    private lateinit var spanEditor: SpanEditor
    private lateinit var foregroundColors: Array<Int>
    private lateinit var backgroundColors: Array<Int>
    private lateinit var attachments: SendAttachments
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var confirmDialog: AlertDialog.Builder
    private lateinit var managerSMTP: SMTPManager
    private lateinit var user: User
    private var emailToRequest: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_mail)
        initFields()
        initViewsListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_ATTACHMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    loadingDialog.setTitle(getString(R.string.loading_title_attach))
                    loadingDialog.show()
                    val attachment = getAttachment(data)
                    attachments.add(attachment)
                    attachmentsRecycleView.adapter?.notifyDataSetChanged()
                } catch (e: UriSchemeException) {
                    shortToast(getString(R.string.cannot_attach))
                } catch (e: FileNotFoundException) {
                    shortToast(getString(R.string.cannot_attach))
                } catch (e: FileSizeException) {
                    e.message?.let { shortToast(it) }
                } catch (e: AttachmentsSizeException) {
                    e.message?.let { shortToast(it) }
                } finally {
                    loadingDialog.dismiss()
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
                        grantResults[0] == PackageManager.PERMISSION_GRANTED)
            ) {
                showFilePickerIntent(PICK_ATTACHMENT_REQUEST_CODE)
            }
    }

    private suspend fun getAttachment(data: Intent): UriAttachment {
        val uri = UriAttachment(data.data as Uri, this)
        withContext(Dispatchers.IO) {
            val fis = uri.getInputStream() ?: throw FileNotFoundException("")
            fis.close()
        }
        return uri
    }

    private fun initFields() {
        val intent = intent
        confirmDialog = createConfirmAlertDialog(this)
        user = User(
            intent.getStringExtra("email").toString(),
            intent.getStringExtra("password").toString()
        )
        managerSMTP = SMTPManager(user)
        settings = Settings(this)
        spanEditor = SpanEditor(messageBody.text as SpannableStringBuilder)
        loadingDialog = LoadingDialog(this)
        loadingDialog.setTitle(getString(R.string.loading_title_attach))
        foregroundColors =
            this.resources.getIntArray(R.array.foregroundColors).toList().toTypedArray()
        backgroundColors =
            this.resources.getIntArray(R.array.backgroundColors).toList().toTypedArray()
        attachments = SendAttachments(this)
    }

    private fun initViewsListeners() {
        confirmDialog.setPositiveButton(getString(R.string.dialog_yes)) { _: DialogInterface, _: Int ->
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    loadingDialog.setTitle(getString(R.string.loading_title_request))
                    loadingDialog.show()
                    val publicEncryptionKey = settings.getPublicKeyString(user.email, ENCRYPT_KEY)
                    val publicSignKey = settings.getPublicKeyString(user.email, SIGN_KEY)
                    val managerSMTP = SMTPManager(user)
                    val message = managerSMTP.getSendExchangeRequestMessage(
                        emailToRequest,
                        publicEncryptionKey,
                        publicSignKey
                    )
                    managerSMTP.sendMessage(message)
                    shortToast(getString(R.string.request_sent))
                } catch (e: AuthenticationFailedException) {
                    println(e.message)
                    shortToast(getString(R.string.auth_error))
                } catch (e: MessagingException) {
                    println(e.message)
                    Snackbar.make(
                        this@SendMailActivity.constraintLayout,
                        getString(R.string.internet_connection),
                        Snackbar.LENGTH_LONG
                    ).show()
                } finally {
                    loadingDialog.dismiss()
                }
            }
        }

        attachmentsRecycleView.layoutManager = LinearLayoutManager(this@SendMailActivity)
        attachmentsRecycleView.adapter = RecyclerSendAttachmentsAdapter(attachments)

        setSupportActionBar(sendmail_toolbar)

        sendmail_toolbar.setNavigationOnClickListener {
            this@SendMailActivity.finish()
        }

        doEcp.setOnCheckedChangeListener { _: CompoundButton, state: Boolean ->
            if (state)
                Toast.makeText(
                    this@SendMailActivity,
                    getString(R.string.sign_on),
                    Toast.LENGTH_SHORT
                ).show()
        }

        doEncrypt.setOnCheckedChangeListener { _: CompoundButton, state: Boolean ->
            if (state)
                Toast.makeText(
                    this@SendMailActivity,
                    getString(R.string.encryption_on),
                    Toast.LENGTH_SHORT
                ).show()
        }

        showFormatPanel.setOnCheckedChangeListener { _: CompoundButton, state: Boolean ->
            formatting_panel.visibility = if (state) View.VISIBLE else View.GONE
        }

        attach_button.setOnClickListener {
            this.showFilePickerIntent(PICK_ATTACHMENT_REQUEST_CODE)
        }

        send_button.setOnClickListener {
            messageBody.clearFocus()
            val toEmail = toEmails.text.toString()
            val subject = subject.text.toString()
            val plainText = messageBody.text.toString()
            val html = if (plainText.isNotEmpty()) messageBody.text?.toHtml().toString() else ""
            if (toEmail.isEmpty())
                shortToast(getString(R.string.no_recipient))
            else if (!isValidEmail(toEmail))
                shortToast(getString(R.string.recipient_email_error))
            else
                sendMessage(toEmail, subject, plainText, html)
        }

        messageBody.setOnFocusChangeListener { _: View, isFocused: Boolean ->
            if (isFocused)
                showFormatPanel.isEnabled = true
            else {
                showFormatPanel.isEnabled = false
                showFormatPanel.isChecked = false
            }
        }

        messageBody.onSelectionChangedListener = object : OnSelectionChangedListener {
            override fun onSelectionChanged(selectionStart: Int, selectionEnd: Int) {
                updateFormattingPanel(selectionStart, selectionEnd)
            }
        }

        bold_checkbox.setOnClickListener {
            if ((it as CheckBox).isChecked)
                setMessageSpan(StyleSpan(Typeface.BOLD))
            else
                removeMessageSpan(StyleSpan(Typeface.BOLD))
        }

        italic_checkbox.setOnClickListener {
            if ((it as CheckBox).isChecked)
                setMessageSpan(StyleSpan(Typeface.ITALIC))
            else
                removeMessageSpan(StyleSpan(Typeface.ITALIC))
        }

        underline_checkbox.setOnClickListener {
            if ((it as CheckBox).isChecked)
                setMessageSpan(UnderlineSpan())
            else
                removeMessageSpan(UnderlineSpan())
        }

        cancel_format.setOnClickListener {
            spanEditor.removeAllSpans(
                messageBody.selectionStart,
                messageBody.selectionEnd,
            )
            updateFormattingPanel(messageBody.selectionStart, messageBody.selectionEnd)
        }

        font_color_spinner.adapter = ColorSpinnerAdapter(this, foregroundColors)
        font_color_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, position: Int, id: Long) {
                if (position == 0)
                    removeMessageSpan(ForegroundColorSpan(foregroundColors[position]))
                else
                    setMessageSpan(ForegroundColorSpan(foregroundColors[position]))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        fill_color_spinner.adapter = ColorSpinnerAdapter(this, backgroundColors)
        fill_color_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, position: Int, id: Long) {
                if (position == 0)
                    removeMessageSpan(BackgroundColorSpan(foregroundColors[position]))
                else
                    setMessageSpan(BackgroundColorSpan(backgroundColors[position]))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        close_format_panel_btn.setOnClickListener {
            showFormatPanel.isChecked = false
            formatting_panel.visibility = View.GONE
        }
    }

    private fun sendMessage(
        toEmail: String,
        subject: String,
        plainText: String,
        html: String
    ) {
        loadingDialog.setTitle(getString(R.string.loading_title_sending))
        val mimeMessage = managerSMTP.getMimeMessage(toEmail, subject, attachments, plainText, html)
        CoroutineScope(Dispatchers.Main).launch {
            try {
                loadingDialog.show()
                val message = OwlMessage(
                    "${getExternalFilesDir(null)}/${user.email}",
                    mimeMessage
                )
                if (doEncrypt.isChecked)
                    message.encrypt(
                        settings.getSubscriberPublicKey(
                            user.email,
                            toEmail,
                            ENCRYPT_KEY
                        )
                    )
                if (doEcp.isChecked) {
                    settings.getSubscriberPublicKey(user.email, toEmail, SIGN_KEY)
                    message.sign(settings.getPrivateKey(user.email, SIGN_KEY))
                }
                managerSMTP.sendMessage(message.message)
                setResult(RESULT_OK)
                this@SendMailActivity.finish()
            } catch (e: AuthenticationFailedException) {
                shortToast(getString(R.string.auth_error))
            } catch (e: MessagingException) {
                Snackbar.make(
                    this@SendMailActivity.constraintLayout,
                    getString(R.string.internet_connection),
                    Snackbar.LENGTH_LONG
                ).show()
            } catch (e: SettingsException) {
                emailToRequest = toEmail
                confirmDialog.setMessage(getString(R.string.send_request_dialog_message, toEmail))
                confirmDialog.show()
            } finally {
                loadingDialog.dismiss()
            }
        }
    }

    private fun setMessageSpan(span: ParcelableSpan) {
        if (messageBody.isFocused)
            spanEditor.setSpan(
                span,
                messageBody.selectionStart,
                messageBody.selectionEnd
            )
    }

    private fun removeMessageSpan(span: ParcelableSpan) {
        if (messageBody.isFocused)
            spanEditor.removeSpan(
                messageBody.selectionStart,
                messageBody.selectionEnd,
                span
            )
    }

    /**
     * Updates UI elements states on Formatting Panel according to selection in message body edit
     * Update states of checkbox (Bold, Italic, Underline)
     * Update background colors of foreground and background colors spinners
     */
    private fun updateFormattingPanel(selectionStart: Int, selectionEnd: Int) {
        if (formatting_panel.isVisible) {
            var foregroundIndex = 0
            var backgroundIndex = 0
            var isBold = false
            var isItalic = false
            var isUnderline = false
            val builder = (messageBody.text as SpannableStringBuilder)
            val spans = builder.getSpans(selectionStart, selectionEnd, ParcelableSpan::class.java)

            for (span in spans) {
                if (span is StyleSpan) {
                    if (span.style == Typeface.BOLD)
                        isBold = true
                    else if (span.style == Typeface.ITALIC)
                        isItalic = true
                } else if (span is UnderlineSpan)
                    isUnderline = true
                else if (span is ForegroundColorSpan)
                    foregroundIndex = foregroundColors.indexOf(span.foregroundColor)
                else if (span is BackgroundColorSpan)
                    backgroundIndex = backgroundColors.indexOf(span.backgroundColor)
            }
            bold_checkbox.isChecked = isBold; italic_checkbox.isChecked = isItalic
            underline_checkbox.isChecked = isUnderline

            val foregroundSpinnerView = font_color_spinner.findViewById<TextView>(R.id.color_view)
            foregroundSpinnerView.setBackgroundColor(setTransparent(foregroundColors[foregroundIndex]))

            val backgroundSpinnerView = fill_color_spinner.findViewById<TextView>(R.id.color_view)
            backgroundSpinnerView.setBackgroundColor(setTransparent(backgroundColors[backgroundIndex]))
        }
    }
}