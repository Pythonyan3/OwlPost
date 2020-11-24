package com.example.owlpost.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.owlpost.R
import com.example.owlpost.models.FilenameAttachment
import com.example.owlpost.models.UriAttachment
import kotlinx.android.synthetic.main.received_attachment_item_recyclerview.view.*
import java.io.File

class RecyclerReceivedAttachmentsAdapter(private var attachments: Array<FilenameAttachment>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    var onDownloadClickListener = object: OnDownloadClickListener{
        override fun onDownloadClick(attachment: FilenameAttachment) {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AttachmentViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.received_attachment_item_recyclerview,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is AttachmentViewHolder -> {
                holder.bind(attachments[position], onDownloadClickListener)
            }
        }
    }

    override fun getItemCount(): Int = attachments.size

    class AttachmentViewHolder constructor(itemView: View): RecyclerView.ViewHolder(itemView){
        private val icon = itemView.attachment_icon
        private val filename = itemView.attachment_name
        private val downloadButton = itemView.attachment_download_button

        fun bind(_attachment: FilenameAttachment, listener: OnDownloadClickListener){
            filename.text = _attachment.filename
            setIcon(_attachment.mimeType)
            downloadButton.setOnClickListener {
                listener.onDownloadClick(_attachment)
            }
        }

        private fun setIcon(mimeType: String){
            when {
                mimeType.contains("image") -> icon.setBackgroundResource(R.drawable.ic_image)
                mimeType.contains("audio") -> icon.setBackgroundResource(R.drawable.ic_audio)
                mimeType.contains("video") -> icon.setBackgroundResource(R.drawable.ic_video)
                else -> icon.setBackgroundResource(R.drawable.ic_file)
            }
        }
    }

    interface OnDownloadClickListener{
        fun onDownloadClick(attachment: FilenameAttachment)
    }
}