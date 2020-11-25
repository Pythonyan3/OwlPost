package com.example.owlpost.ui

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.example.owlpost.R
import com.example.owlpost.ui.widgets.LoadingDialog
import java.util.*


const val PERMISSIONS_REQUEST_CODE = 1
const val PICK_ATTACHMENT_REQUEST_CODE = 2
const val PICK_PUBLIC_KEYS_FILE_REQUEST_CODE = 3
const val PICK_PRIVATE_KEYS_FILE_REQUEST_CODE = 4
const val CREATE_PUBLIC_KEYS_FILE_REQUEST_CODE = 5
const val CREATE_PRIVATE_KEYS_FILE_REQUEST_CODE = 6
const val ADD_EMAIL_REQUEST_CODE = 7
const val SEND_EMAIL_REQUEST_CODE = 8
const val SAVE_ATTACHMENT_REQUEST_CODE = 9


/**
 * Shows file chooser intent
 * Intent selects all of file types
 */
fun AppCompatActivity.showFilePickerIntent(requestCode: Int, type: String = "*/*") {
    if (ContextCompat.checkSelfPermission(
            this,
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
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = type
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(
            Intent.createChooser(intent, "Choose File"),
            requestCode
        )
    }
}

/**
 * Shows Toast
 */
fun AppCompatActivity.shortToast(message: String) {
    Toast.makeText(
        this,
        message,
        Toast.LENGTH_SHORT
    ).show()
}

fun createConfirmAlertDialog(activity: AppCompatActivity): AlertDialog.Builder {
    val builder = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.AppTheme))
    builder.setCancelable(false)
    builder.setTitle(activity.getString(R.string.dialog_title))
    builder.setNegativeButton(activity.getString(R.string.dialog_no)) { _: DialogInterface, _: Int ->}
    builder.create()
    return builder
}

fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

/**
 * Adds alpha channel (0x99 - 60%) to color
 */
fun setTransparent(color: Int): Int {
    return Color.argb(
        0x99,
        color.red,
        color.green,
        color.blue
    )
}

fun randomColor(): Int {
    val random = Random()
    val r = random.nextInt(220)
    val g = random.nextInt(220)
    val b = random.nextInt(220)
    return Color.argb(160, r, g, b)
}

fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") {
        it.toLowerCase(Locale.ROOT).capitalize(Locale.ROOT)
    }