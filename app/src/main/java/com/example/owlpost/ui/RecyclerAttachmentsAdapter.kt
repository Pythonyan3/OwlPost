package com.example.owlpost.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.owlpost.R
import com.example.owlpost.models.UriWrapper
import kotlinx.android.synthetic.main.attachment_item_recyclerview.view.*

class RecyclerAttachmentsAdapter(private var attachments: MutableList<UriWrapper>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

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

                holder.filename.text = uri.filename
                holder.size.text = uri.formattedSize()

                holder.deleteButton.setOnClickListener{
                    val pos = attachments.indexOf(uri)
                    attachments.removeAt(pos)
                    this.notifyItemRemoved(pos)
                }
            }
        }
    }

    override fun getItemCount(): Int = attachments.size

    class UriViewHolder constructor(itemView: View): RecyclerView.ViewHolder(itemView){
        val icon = itemView.attachment_icon
        val filename = itemView.attachment_name
        val size = itemView.attachment_size
        val deleteButton = itemView.attachment_delete_button
    }
}