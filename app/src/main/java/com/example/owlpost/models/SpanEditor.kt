package com.example.owlpost.models

import android.text.ParcelableSpan
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.*


val USED_SPANS = arrayOf(
    StyleSpan::class.java,
    UnderlineSpan::class.java,
    ForegroundColorSpan::class.java,
    BackgroundColorSpan::class.java
)

/**
 * Sets style span to spannable string builder,
 * if style span already exists in selectionStart-selectionEnd range then doesn't
 */
fun setSpan(
    builder: SpannableStringBuilder,
    span: ParcelableSpan,
    selectionStart: Int,
    selectionEnd: Int
){
    if (selectionStart == selectionEnd)
        builder.setSpan(span, selectionStart, selectionEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
    else
        builder.setSpan(span, selectionStart, selectionEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
}

/**
 *  Removes StyleSpan in spannable string builder in selectionStart-selectionEnd range
 *  If size to remove less than selection range then makes new spans
 */
fun removeSpan(
    builder: SpannableStringBuilder,
    selectionStart: Int,
    selectionEnd: Int,
    typeface: Int
){
    val spans = builder.getSpans(selectionStart, selectionEnd, StyleSpan::class.java)
    for (span in spans){
        if (span.style == typeface){
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
 *  Removes Underline span in spannable string builder in selectionStart-selectionEnd range
 *  If size to remove less than selection range then makes new spans
 */
fun removeSpan(
    builder: SpannableStringBuilder,
    selectionStart: Int,
    selectionEnd: Int,
    pSpan: ParcelableSpan
){
    val spans = builder.getSpans(selectionStart, selectionEnd, pSpan::class.java)
    for (span in spans){
        val spanStart = builder.getSpanStart(span); val spanEnd = builder.getSpanEnd(span)
        builder.removeSpan(span)
        if (spanStart < selectionStart)
            builder.setSpan(span, spanStart, selectionStart, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        if (spanEnd > selectionEnd)
            builder.setSpan(copySpan(span), selectionEnd, spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    }
}

/**
 *  Removes all spans in spannable string builder in selectionStart-selectionEnd range
 *  If size to remove less than selection range then makes new spans
 */
fun removeAllSpans(builder: SpannableStringBuilder, selectionStart: Int, selectionEnd: Int){
    val spans = builder.getSpans(selectionStart, selectionEnd, ParcelableSpan::class.java)
    for (span in spans){
        if (!USED_SPANS.contains(span::class.java)) // skip Spell Check Span
            continue
        val spanStart = builder.getSpanStart(span); val spanEnd = builder.getSpanEnd(span)
        builder.removeSpan(span)
        if (spanStart < selectionStart)
            builder.setSpan(copySpan(span), spanStart, selectionStart, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        if (spanEnd > selectionEnd)
            builder.setSpan(copySpan(span), selectionEnd, spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    }
}

/**
 * Creates new span object
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