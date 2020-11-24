package com.example.owlpost.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.owlpost.R
import com.example.owlpost.models.SendAttachments
import com.example.owlpost.models.UriAttachment
import kotlinx.android.synthetic.main.send_attachment_item_recyclerview.view.*

class RecyclerSendAttachmentsAdapter(private var attachments: SendAttachments): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UriViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.send_attachment_item_recyclerview,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is UriViewHolder -> {
                val uri = attachments[position]
                holder.bind(attachments, uri, this)
            }
        }
    }

    override fun getItemCount(): Int = attachments.size

    class UriViewHolder constructor(itemView: View): RecyclerView.ViewHolder(itemView){
        val icon = itemView.attachment_icon
        val imagePreview = itemView.imagePreview
        val filename = itemView.attachment_name
        val size = itemView.attachment_size
        val deleteButton = itemView.attachment_delete_button

        fun bind(attachments: SendAttachments, uri: UriAttachment, adapter: RecyclerSendAttachmentsAdapter){
            filename.text = uri.filename
            size.text = uri.formattedSize()
            loadImage(uri)
            setIcon(uri)

            deleteButton.setOnClickListener{
                val pos = attachments.indexOf(uri)
                attachments.removeAt(pos)
                adapter.notifyItemRemoved(pos)
            }
        }

        private fun loadImage(uri: UriAttachment){
            if (uri.getType().contains("image")){
                imagePreview.visibility = View.VISIBLE
                val requestOptions = RequestOptions()
                    .placeholder(R.drawable.ic_image)
                    .error(R.drawable.ic_image_error)
                Glide.with(itemView.context)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(uri.uri)
                    .into(imagePreview)
            }
            else{
                imagePreview.visibility = View.GONE
            }
        }

        private fun setIcon(uri: UriAttachment){
            when {
                uri.getType().contains("image") -> icon.setBackgroundResource(R.drawable.ic_image)
                uri.getType().contains("audio") -> icon.setBackgroundResource(R.drawable.ic_audio)
                uri.getType().contains("video") -> icon.setBackgroundResource(R.drawable.ic_video)
                else -> icon.setBackgroundResource(R.drawable.ic_file)
            }
        }
    }
}