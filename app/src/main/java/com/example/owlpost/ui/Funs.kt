package com.example.owlpost.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.owlpost.PICK_FILE_REQUEST_CODE
import com.example.owlpost.SendMailActivity
import com.example.owlpost.models.UriWrapper
import java.io.FileNotFoundException


/**
 * Makes an instance of UriWrapper
 * Gets some data by uri (filename, size)
 * Try to open InputStream
 */
fun SendMailActivity.getAttachment(data: Intent): UriWrapper {
    val uri = UriWrapper(data.data as Uri, this)
    val fis = uri.getInputStream() ?: throw FileNotFoundException("")
    fis.close()
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
    runOnUiThread {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}