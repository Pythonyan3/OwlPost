package com.example.owlpost.ui

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.attachment_item_recyclerview.view.*

class RecyclerAttachmentsAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var attachments: List<Uri> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int = attachments.size

    class UriViewHolder constructor(itemView: View): RecyclerView.ViewHolder(itemView){
        val icon = itemView.attachment_icon
        val filename = itemView.attachment_name
        val deleteBtn = itemView.delete_attachment

        fun bind(uri: Uri){

        }
    }
}