package com.example.owlpost.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


const val PERMISSIONS_REQUEST_CODE = 1
const val PICK_ATTACHMENT_REQUEST_CODE = 2
const val PICK_PUBLIC_KEYS_FILE_REQUEST_CODE = 3
const val PICK_PRIVATE_KEYS_FILE_REQUEST_CODE = 4
const val CREATE_PUBLIC_KEYS_FILE_REQUEST_CODE = 5
const val CREATE_PRIVATE_KEYS_FILE_REQUEST_CODE = 6
const val ADD_EMAIL_REQUEST_CODE = 7
const val SEND_EMAIL_REQUEST_CODE = 8


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
fun AppCompatActivity.shortToast(message: String){
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
}

fun showLoading(loadingDialog: LoadingDialog){
    loadingDialog.show()
}

fun hideLoading(loadingDialog: LoadingDialog){
    loadingDialog.dismiss()
}

fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}