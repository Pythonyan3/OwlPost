package com.example.owlpost.models

import android.content.Context
import com.example.owlpost.R
import kotlin.math.pow


const val MAX_ATTACHMENTS_SIZE_MB = 25
val MAX_ATTACHMENTS_SIZE_BYTES = MAX_ATTACHMENTS_SIZE_MB * 10.0.pow(6).toInt()


class Attachments{
    private val attachments: ArrayList<UriManager>
    private val context: Context
    val size: Int
    get() = attachments.size
    private var totalAttachmentsSize: Int = 0

    constructor(_context: Context){
        context = _context
        attachments = ArrayList()
    }

    constructor(_context: Context, _attachments: ArrayList<UriManager>){
        context = _context
        val totalSize = calcTotalAttachmentsSize(_attachments)
        if (totalSize > MAX_ATTACHMENTS_SIZE_BYTES)
            throw AttachmentsSizeException(
                context.getString(R.string.total_attachments_size, MAX_ATTACHMENTS_SIZE_MB)
            )
        attachments = _attachments
        totalAttachmentsSize = totalSize.toInt()
    }

    operator fun get(index: Int): UriManager{
        return attachments[index]
    }

    fun add(element: UriManager){
        if (totalAttachmentsSize + element.size > MAX_ATTACHMENTS_SIZE_BYTES)
            throw AttachmentsSizeException(
                context.getString(R.string.total_attachments_size, MAX_ATTACHMENTS_SIZE_MB)
            )
        attachments.add(element)
        totalAttachmentsSize += element.size.toInt()
    }

    fun removeAt(index: Int){
        val element = attachments[index]
        attachments.removeAt(index)
        totalAttachmentsSize -= element.size.toInt()
    }

    fun indexOf(element: UriManager): Int{
        return attachments.indexOf(element)
    }

    private fun calcTotalAttachmentsSize(_attachments: ArrayList<UriManager>): Long{
        var totalSize = 0L
        _attachments.forEach {
            totalSize += it.size
        }
        return totalSize
    }
}