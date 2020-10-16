package com.example.owlpost


import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.ParcelableSpan
import android.text.SpannableStringBuilder
import android.text.style.*
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.owlpost.models.*
import com.example.owlpost.ui.ColorSpinnerAdapter
import kotlinx.android.synthetic.main.activity_send_mail.*


class SendMail : AppCompatActivity() {
    private val settings = Settings()
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_mail)

        initVariables()
        initViewsListeners()
    }

    private fun initVariables() {
        //settings.init(this)
        //currentUser = settings.getCurrentUser()
    }

    private fun initViewsListeners(){

        back_button.setOnClickListener {
            this.finish()
        }

        showFormatPanel.setOnCheckedChangeListener{ it: CompoundButton, state: Boolean ->
            bottom_panel.visibility = if (state) View.VISIBLE else View.GONE
        }

        attach_button.setOnClickListener {
            Toast.makeText(this, "ATTACH FILE", Toast.LENGTH_SHORT).show()
        }

        send_button.setOnClickListener {
            Toast.makeText(this, "SENDING MAIL", Toast.LENGTH_SHORT).show()
        }

        messageBody.setOnFocusChangeListener{ view: View, isFocused: Boolean ->
            if (isFocused)
                showFormatPanel.isEnabled = true
            else{
                showFormatPanel.isEnabled = false
                showFormatPanel.isChecked = false
            }
        }

        bold_btn.setOnClickListener{
            if ((it as CheckBox).isChecked)
                setMessageSpan(StyleSpan(Typeface.BOLD))
            else
                removeMessageSpan(Typeface.BOLD)
        }

        italic_btn.setOnClickListener{
            if ((it as CheckBox).isChecked)
                setMessageSpan(StyleSpan(Typeface.ITALIC))
            else
                removeMessageSpan(Typeface.ITALIC)
        }

        underline_btn.setOnClickListener{
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
            bold_btn.isChecked = false; italic_btn.isChecked = false
            underline_btn.isChecked = false
        }

        val foregroundColors = this.resources.getIntArray(R.array.formattingColors).toList().toTypedArray()
        font_color_spinner.adapter = ColorSpinnerAdapter(this, foregroundColors)

        font_color_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p: AdapterView<*>?, v: View?, position: Int, id: Long) {
                setMessageSpan(ForegroundColorSpan(foregroundColors[position]))
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val backgroundColors = this.resources.getIntArray(R.array.formattingColors).toList().toTypedArray()
        backgroundColors[0] = Color.WHITE
        fill_color_spinner.adapter = ColorSpinnerAdapter(this, backgroundColors)

        fill_color_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p: AdapterView<*>?, v: View?, position: Int, id: Long) {
                setMessageSpan(BackgroundColorSpan(backgroundColors[position]))
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        close_btn.setOnClickListener {
            showFormatPanel.isChecked = false
            bottom_panel.visibility = View.GONE
        }
    }

    private fun setMessageSpan(span: ParcelableSpan){
        if (messageBody.isFocused)
            setSpan(
                messageBody.text as SpannableStringBuilder,
                span,
                messageBody.selectionStart,
                messageBody.selectionEnd
            )
    }

    private fun removeMessageSpan(typeface: Int){
        if (messageBody.isFocused)
            removeSpan(
                messageBody.text as SpannableStringBuilder,
                messageBody.selectionStart,
                messageBody.selectionEnd,
                typeface
            )
    }

    private fun removeMessageSpan(span: ParcelableSpan){
        if (messageBody.isFocused)
            removeSpan(
                messageBody.text as SpannableStringBuilder,
                messageBody.selectionStart,
                messageBody.selectionEnd,
                span
            )
    }
}