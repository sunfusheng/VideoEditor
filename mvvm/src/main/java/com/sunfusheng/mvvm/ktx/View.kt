package com.sunfusheng.mvvm.ktx

import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

/**
 * @author sunfusheng
 * @since  2021/12/31
 */

fun View.visible() {
  if (visibility != View.VISIBLE) {
    visibility = View.VISIBLE
  }
}

fun View.invisible() {
  if (visibility != View.INVISIBLE) {
    visibility = View.INVISIBLE
  }
}

fun View.gone() {
  if (visibility != View.GONE) {
    visibility = View.GONE
  }
}

fun TextView.setLeftDrawable(@DrawableRes id: Int) {
  setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, id), null, null, null)
}

fun TextView.setTopDrawable(@DrawableRes id: Int) {
  setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(context, id), null, null)
}

fun TextView.setRightDrawable(@DrawableRes id: Int) {
  setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, id), null)
}

fun TextView.setBottomDrawable(@DrawableRes id: Int) {
  setCompoundDrawablesWithIntrinsicBounds(null, null, null, ContextCompat.getDrawable(context, id))
}

fun TextView.removeAllDrawables() {
  setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
}