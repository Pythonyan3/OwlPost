package com.example.owlpost.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.owlpost.SendMailActivity
import com.example.owlpost.models.UriWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException


const val PERMISSIONS_REQUEST_CODE = 1
const val PICK_FILE_REQUEST_CODE = 2
const val ADD_EMAIL_REQUEST_CODE = 3
const val SEND_EMAIL_REQUEST_CODE = 4


/**
 * Makes an instance of UriWrapper
 * Gets some data by uri (filename, size)
 * Try to open InputStream
 */
suspend fun SendMailActivity.getAttachment(data: Intent): UriWrapper {
    val uri = UriWrapper(data.data as Uri, this)
    withContext(Dispatchers.IO){
        val fis = uri.getInputStream() ?: throw FileNotFoundException("")
        fis.close()
    }
    return uri
}

/**
 * Shows file chooser intent
 * Intent selects all of file types
 */
fun AppCompatActivity.showFilePickerIntent() {
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.type = "*/*"
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    startActivityForResult(
        Intent.createChooser(intent, "Choose File"),
        PICK_FILE_REQUEST_CODE
    )
}

/**
 * Shows Toast in UiThread
 * Function used in coroutines
 */
fun AppCompatActivity.shortToast(message: String){
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
}

fun AppCompatActivity.showLoading(loadingDialog: LoadingDialog){
    loadingDialog.show()
}

fun AppCompatActivity.hideLoading(loadingDialog: LoadingDialog){
    loadingDialog.dismiss()
}

fun AppCompatActivity.isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}