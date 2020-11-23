package com.example.owlpost.ui.adapters

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.owlpost.R
import com.example.owlpost.models.email.OwlMessage
import kotlinx.android.synthetic.main.message_item_load_more.view.*
import kotlinx.android.synthetic.main.message_item_recyclerview.view.*
import java.text.DateFormat
import java.util.*


class RecyclerMessageItemAdapter(private val messages: ArrayList<OwlMessage?>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val colors = mutableMapOf<String, Int>()
    private val VIEW_ITEM = 1
    private val VIEW_PROG = 0

    override fun getItemViewType(position: Int): Int {
        return if (messages[position] != null) VIEW_ITEM else VIEW_PROG
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_ITEM)
            return MessageViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.message_item_recyclerview,
                    parent,
                    false
                )
            )
        else
            return ProgressViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.message_item_load_more,
                    parent,
                    false
                )
            )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MessageViewHolder -> {
                holder.bind(messages[position]!!, colors)
            }
            is ProgressViewHolder -> {
                holder.progressBar.isIndeterminate = true
            }
        }
    }

    override fun getItemCount(): Int = messages.size

    class MessageViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon = itemView.user_icon_text
        private val from = itemView.message_from
        private val subject = itemView.message_subject
        val text = itemView.message_text
        val date = itemView.message_date

        fun bind(message: OwlMessage, colors: MutableMap<String, Int>) {
            icon.text = message.from.subSequence(0 until 1)
            icon.setTextColor(Color.WHITE)
            from.text = message.from
            subject.text = message.subject
            val msgDate = message.date
            date.text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(msgDate)

            if (!colors.containsKey(message.from)) {
                val rnd = Random()
                val color = Color.argb( 160 ,rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
                colors[message.from] = color
            }
            val gradientDrawable = icon.background as GradientDrawable
            colors[message.from]?.let { gradientDrawable.setColor(it) }

            val typeface = if (!message.seen) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            from.typeface = typeface
            subject.typeface = typeface
            date.typeface = typeface
        }
    }

    class ProgressViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val progressBar = itemView.progressBar1
    }
}