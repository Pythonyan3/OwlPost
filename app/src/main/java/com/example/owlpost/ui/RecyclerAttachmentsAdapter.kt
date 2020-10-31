package com.example.owlpost.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.owlpost.R
import com.example.owlpost.R.drawable.ic_image
import com.example.owlpost.models.Attachments
import com.example.owlpost.models.UriWrapper
import kotlinx.android.synthetic.main.attachment_item_recyclerview.view.*

class RecyclerAttachmentsAdapter(private var attachments: Attachments): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UriViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.attachment_item_recyclerview,
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

        fun bind(attachments: Attachments, uri: UriWrapper, adapter: RecyclerAttachmentsAdapter){
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

        private fun loadImage(uri: UriWrapper){
            if (uri.type.contains("image")){
                imagePreview.visibility = View.VISIBLE
                val requestOptions = RequestOptions()
                    .placeholder(ic_image)
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

        private fun setIcon(uri: UriWrapper){
            if (uri.type.contains("image"))
                icon.setBackgroundResource(R.drawable.ic_image)
            else if (uri.type.contains("audio"))
                icon.setBackgroundResource(R.drawable.ic_audio)
            else if (uri.type.contains("video"))
                icon.setBackgroundResource(R.drawable.ic_video)
            else
                icon.setBackgroundResource(R.drawable.ic_file)
        }
    }
}