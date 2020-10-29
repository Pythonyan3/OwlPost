package com.example.owlpost.models

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.InputStream
import java.net.URI


@SuppressLint("Recycle")
class UriWrapper(val uri: Uri, private val context: Context) {

    var filename: String = ""
    var size: Long = 0L

    init {
        when (uri.scheme) {
            "content" -> {
                val cursor: Cursor? = context.contentResolver?.query(
                    uri,
                    null,
                    null,
                    null,
                    null,
                    null
                )
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                        filename = cursor.getString(nameIndex)
                        size = cursor.getLong(sizeIndex)
                    }
                } finally {
                    cursor!!.close()
                }
            }
            "file" -> {
                val file = File(URI(uri.toString()))
                filename = file.name
                size = file.length()
            }
            else -> {
                throw UriWrapperException("Don't support current uri scheme (${uri.scheme})")
            }
        }
        if (size / 1000000 > 25L) throw FileSizeException("File is too big. Maximum size is 25Mb")
    }

    fun getInputStream(): InputStream? {
        return when (uri.scheme) {
            "content" -> {
                context.contentResolver.openInputStream(uri)
            }
            else -> {
                val file = File(URI(uri.toString()))
                file.inputStream()
            }
        }
    }
}