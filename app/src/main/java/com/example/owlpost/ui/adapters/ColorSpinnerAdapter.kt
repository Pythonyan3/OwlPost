package com.example.owlpost.ui.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.example.owlpost.R


class ColorSpinnerAdapter(private val context: Context, private val colors: Array<Int>) : BaseAdapter() {
    private val mInflate: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder: ViewHolder
        var view = convertView

        if (view == null) {
            view = mInflate.inflate(R.layout.spinner_selected_item_view, parent, false)
            viewHolder = ViewHolder(view)
        } else {
            viewHolder = view.tag as ViewHolder
        }

        view?.tag = viewHolder

        val color = Color.argb(
            0x99,
            colors[position].red,
            colors[position].green,
            colors[position].blue
        )
        viewHolder.color_view.setBackgroundColor(color)

        return view!!
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder: DropdownViewHolder
        var view = convertView

        if (view == null) {
            view = mInflate.inflate(R.layout.spinner_item, parent, false)
            viewHolder = DropdownViewHolder(view)
        } else {
            viewHolder = view.tag as DropdownViewHolder
        }

        view?.tag = viewHolder

        val shape = viewHolder.color_view.background as GradientDrawable

        shape.setColor(colors[position])
        shape.setStroke(if (colors[position] == Color.WHITE) 1 else 0, Color.BLACK)

        return view!!
    }

    override fun getItem(position: Int): Any = colors[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = colors.size

    class ViewHolder(view: View){
        var color_view: TextView = view.findViewById(R.id.color_view)
    }

    class DropdownViewHolder(view: View){
        var color_view: TextView = view.findViewById(R.id.color_view)
    }
}