package com.example.owlpost.ui.widgets

import android.app.Activity
import android.app.AlertDialog
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import com.example.owlpost.R
import kotlinx.android.synthetic.main.loading_dialog.view.*

class LoadingDialog(activity: Activity) {
    private val dialog: AlertDialog
    private val view: View

    init {
        val builder = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.AppTheme))
        val inflater = activity.layoutInflater
        view = inflater.inflate(R.layout.loading_dialog, null)
        builder.setView(view)
        builder.setCancelable(false)
        dialog = builder.create()
    }

    fun show(){
        dialog.show()
    }

    fun dismiss(){
        dialog.dismiss()
    }

    fun setTitle(title: String){
        view.loading_title.text = title
    }
}