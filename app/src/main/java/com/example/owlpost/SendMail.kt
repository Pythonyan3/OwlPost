package com.example.owlpost


import android.Manifest
import android.app.Activity
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.owlpost.models.*
import com.example.owlpost.ui.ColorSpinnerAdapter
import com.example.owlpost.ui.OnSelectionChangedListener
import com.example.owlpost.ui.RecyclerAttachmentsAdapter
import kotlinx.android.synthetic.main.activity_send_mail.*
import kotlinx.coroutines.*
import java.io.FileNotFoundException


private const val PERMISSIONS_REQUEST_CODE = 1
private const val PICK_FILE_REQUEST_CODE = 2


class SendMail : AppCompatActivity() {
    private val settings = Settings()
    private lateinit var currentUser: User
    private lateinit var foregroundColors: Array<Int>
    private lateinit var backgroundColors: Array<Int>
    private lateinit var attachments: Attachments
    private var attachFileJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_mail)
    }

    override fun onStart() {
        super.onStart()
        initFields()
        initViewsListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            attachFileJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    val attachment = getAttachment(data)
                    attachments.add(attachment)
                    runOnUiThread {
                        attachmentsRecycleView.adapter?.notifyDataSetChanged()
                    }
                } catch (e: UriSchemeException) {
                    shortToast(getString(R.string.cant_attach_msg))
                } catch (e: FileNotFoundException) {
                    shortToast(getString(R.string.cant_attach_msg))
                } catch (e: FileSizeException) {
                    e.message?.let { shortToast(it) }
                } catch (e: AttachmentsSizeException) {
                    e.message?.let { shortToast(it) }
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
        showFilePickerIntent()
    }

    private fun initFields() {
        foregroundColors =
            this.resources.getIntArray(R.array.foregroundColors).toList().toTypedArray()
        backgroundColors =
            this.resources.getIntArray(R.array.backgroundColors).toList().toTypedArray()
        attachments = Attachments(this)
        //settings.init(this)
        //currentUser = settings.getCurrentUser()
    }

    private fun initViewsListeners() {
        attachmentsRecycleView.layoutManager = LinearLayoutManager(this@SendMail)
        attachmentsRecycleView.adapter = RecyclerAttachmentsAdapter(attachments)

        back_button.setOnClickListener {
            this.finish()
        }

        doEcp.setOnCheckedChangeListener { _: CompoundButton, state: Boolean ->
            if (state)
                Toast.makeText(this, "ЭЦП письма включена", Toast.LENGTH_SHORT).show()
        }

        doEncrypt.setOnCheckedChangeListener { _: CompoundButton, state: Boolean ->
            if (state)
                Toast.makeText(this, "Шифрование письма включено", Toast.LENGTH_SHORT).show()
        }

        showFormatPanel.setOnCheckedChangeListener { _: CompoundButton, state: Boolean ->
            formattingPanel.visibility = if (state) View.VISIBLE else View.GONE
        }

        attach_button.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this@SendMail,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            } else {
                showFilePickerIntent()
            }
        }

        send_button.setOnClickListener {
            Toast.makeText(this, "SENDING MAIL", Toast.LENGTH_SHORT).show()
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
            removeAllSpans(
                messageBody.text as SpannableStringBuilder,
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
            formattingPanel.visibility = View.GONE
        }
    }

    private fun setMessageSpan(span: ParcelableSpan) {
        if (messageBody.isFocused)
            setSpan(
                messageBody.text as SpannableStringBuilder,
                span,
                messageBody.selectionStart,
                messageBody.selectionEnd
            )
    }

    private fun removeMessageSpan(span: ParcelableSpan) {
        if (messageBody.isFocused)
            removeSpan(
                messageBody.text as SpannableStringBuilder,
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
        if (formattingPanel.isVisible){
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

    /**
     * Shows file chooser intent
     * Intent selects all of file types
     */
    private fun showFilePickerIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(
            Intent.createChooser(intent, "Выберите файл"),
            PICK_FILE_REQUEST_CODE
        )
    }

    /**
     * Makes an instance of UriWrapper
     * Gets some data by uri (filename, size)
     * Try to open InputStream
     */
    private fun getAttachment(data: Intent): UriWrapper {
        val uri = UriWrapper(data.data as Uri, this@SendMail)
        val fis = uri.getInputStream() ?: throw FileNotFoundException("")
        fis.close()
        return uri
    }

    /**
     * Shows Toast in UiThread
     * Function used in coroutines
     */
    private fun shortToast(message: String) {
        runOnUiThread {
            Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}