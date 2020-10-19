package com.example.owlpost.ui

import android.content.Context
import android.util.AttributeSet


class SelectableEditText : androidx.appcompat.widget.AppCompatEditText {

    lateinit var onSelectionChangedListener: OnSelectionChangedListener

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)


    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        if (this::onSelectionChangedListener.isInitialized)
            onSelectionChangedListener.onSelectionChanged(selStart, selEnd)
    }
}

interface OnSelectionChangedListener {
    fun onSelectionChanged(selectionStart: Int, selectionEnd: Int)
}