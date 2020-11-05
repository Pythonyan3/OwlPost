package com.example.owlpost.models

import android.graphics.Color
import android.text.ParcelableSpan
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.*
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

/**
 * Sets style span to spannable string builder
 */
fun setSpan(
    builder: SpannableStringBuilder,
    spanToSet: ParcelableSpan,
    selectionStart: Int,
    selectionEnd: Int
){
    if (selectionStart == selectionEnd)
        builder.setSpan(spanToSet, selectionStart, selectionEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
    else
        builder.setSpan(spanToSet, selectionStart, selectionEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
}

/**
 *  Removes Underline span in spannable string builder in selectionStart-selectionEnd range
 *  If size to remove less than selection range then makes new spans
 */
fun removeSpan(
    builder: SpannableStringBuilder,
    selectionStart: Int,
    selectionEnd: Int,
    spanToRemove: ParcelableSpan
){
    val spans = builder.getSpans(selectionStart, selectionEnd, spanToRemove::class.java)
    for (span in spans){
        if (isEquals(spanToRemove, span)){
            val spanStart = builder.getSpanStart(span); val spanEnd = builder.getSpanEnd(span)
            builder.removeSpan(span)
            if (spanStart < selectionStart)
                builder.setSpan(span, spanStart, selectionStart, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            if (spanEnd > selectionEnd)
                builder.setSpan(copySpan(span), selectionEnd, spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        }
    }
}

/**
 *  Removes all spans in spannable string builder in selectionStart-selectionEnd range
 *  If size to remove less than selection range then makes new spans
 */
fun removeAllSpans(builder: SpannableStringBuilder, selectionStart: Int, selectionEnd: Int){
    val spans = builder.getSpans(selectionStart, selectionEnd, ParcelableSpan::class.java)
    for (span in spans){
        removeSpan(builder, selectionStart, selectionEnd, span)
    }
}

/**
 * Creates new spannable object
 * If span is instance of StyleSpan then copy Typeface
 * If span is instance of ForegroundColorSpan or BackgroundColorSpan then copy color
 */
fun copySpan(span: ParcelableSpan): ParcelableSpan{
    if (span is StyleSpan)
        return StyleSpan(span.style)
    if (span is ForegroundColorSpan)
        return ForegroundColorSpan(span.foregroundColor)
    if (span is BackgroundColorSpan)
        return BackgroundColorSpan(span.backgroundColor)
    else (span is UnderlineSpan)
        return UnderlineSpan()
}

fun spansExist(builder: SpannableStringBuilder): Boolean{
    val styleSpans = builder.getSpans(0, builder.length, StyleSpan::class.java)
    val foregroundColorSpans = builder.getSpans(0, builder.length, ForegroundColorSpan::class.java)
    val backgroundColorSpans = builder.getSpans(0, builder.length, ForegroundColorSpan::class.java)
    return styleSpans.isNotEmpty() || foregroundColorSpans.isNotEmpty() || backgroundColorSpans.isNotEmpty()
}

/**
 * Checks equality of two spans
 * Spans, instances of StyleSpan, should be equals by 'style' property
 */
private fun isEquals(firstSpan: ParcelableSpan, secondSpan: ParcelableSpan): Boolean{
    if (firstSpan is StyleSpan && secondSpan is StyleSpan)
        return  firstSpan.style == secondSpan.style
    if (firstSpan is UnderlineSpan && secondSpan is UnderlineSpan)
        return true
    if (firstSpan is ForegroundColorSpan && secondSpan is ForegroundColorSpan)
        return true
    if (firstSpan is BackgroundColorSpan && secondSpan is BackgroundColorSpan)
        return true
    return false
}

/**
 * Adds alpha channel (0x99 - 60%) to color
 */
fun setTransparent(color: Int): Int {
    return Color.argb(
        0x99,
        color.red,
        color.green,
        color.blue
    )
}