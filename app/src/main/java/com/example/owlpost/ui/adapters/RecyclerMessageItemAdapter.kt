package com.example.owlpost.ui.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.owlpost.MainActivity
import com.example.owlpost.R
import com.example.owlpost.models.email.OwlMessage
import com.example.owlpost.ui.randomColor
import kotlinx.android.synthetic.main.message_item_load_more.view.*
import kotlinx.android.synthetic.main.message_item_recyclerview.view.*
import java.text.DateFormat
import java.util.*


class RecyclerMessageItemAdapter(
    private val messages: ArrayList<OwlMessage?>,
    private val colors: MutableMap<String, Int>,
    private val context: Context
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var onMessageItemClickListener: OnMessageItemClickListener
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
                messages[position]?.let { message ->
                    holder.bind(message, colors, context)
                }

                holder.itemView.setOnClickListener{
                    if (this::onMessageItemClickListener.isInitialized)
                        messages[position]?.let { message ->
                            onMessageItemClickListener.onItemClick(message)
                        }
                }

                holder.itemView.setOnLongClickListener {
                    //TODO Select message
                    true
                }
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
        private val isEncrypted = itemView.isEncrypted
        private val isSigned = itemView.isSigned
        private val text = itemView.message_text
        private val date = itemView.message_date

        fun bind(message: OwlMessage, colors: MutableMap<String, Int>, context: Context) {
            icon.text = message.from.subSequence(0 until 1)
            icon.setTextColor(Color.WHITE)

            var visibility = if (message.encrypted) View.VISIBLE else View.GONE
            isEncrypted.visibility = visibility

            visibility = if (message.signed) View.VISIBLE else View.GONE
            isSigned.visibility = visibility

            from.text = message.from
            subject.text =
                if (message.subject.isNotEmpty()) message.subject else context.getString(R.string.no_subject)
            date.text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(message.date)
            text.text = message.text

            if (!colors.containsKey(message.from))
                colors[message.from] = randomColor()
            colors[message.from]?.let { (icon.background as GradientDrawable).setColor(it) }

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

interface OnMessageItemClickListener {
    fun onItemClick(message: OwlMessage)
}