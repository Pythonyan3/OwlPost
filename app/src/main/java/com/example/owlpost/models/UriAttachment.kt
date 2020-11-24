package com.example.owlpost.models

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.example.owlpost.R
import java.io.File
import java.io.InputStream
import java.net.URI
import java.net.URLConnection
import java.nio.file.Files


@SuppressLint("Recycle")
class UriAttachment(val uri: Uri, private val context: Context) {

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

                if (cursor != null && cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                    filename = cursor.getString(nameIndex)
                    size = cursor.getLong(sizeIndex)
                }
                cursor!!.close()
            }
            "file" -> {
                val file = File(URI(uri.toString()))
                filename = file.name
                size = file.length()
            }
            else -> {
                throw UriSchemeException(context.getString(R.string.uri_scheme, uri.scheme))
            }
        }
        if (size / 1000000.0f > 25)
            throw FileSizeException(
                context.getString(R.string.attachment_size, MAX_ATTACHMENTS_SIZE_MB)
            )
    }

    fun getType(): String {
        return if (uri.scheme == "content")
            context.contentResolver.getType(uri).toString()
        else{
            val file = File(URI(uri.toString()))
            URLConnection.guessContentTypeFromName(file.name)
        }
    }

    fun getInputStream(): InputStream? {
        return context.contentResolver.openInputStream(uri)
    }

    fun formattedSize(): String{
        return if (size >= 1000000)
            "${String.format("%.2f", size / 1000000f)} Mb"
        else
            "${String.format("%.2f", size / 1000f)} Kb"
    }
}