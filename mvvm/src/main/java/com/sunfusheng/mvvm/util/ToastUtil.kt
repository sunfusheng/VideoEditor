package com.sunfusheng.mvvm.util

import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import com.sunfusheng.mvvm.ContextHolder

/**
 * @author sunfusheng
 * @since  2021/12/31
 */
object ToastUtil {

  fun toast(text: String) {
    toast(ContextHolder.context, text)
  }

  fun toastLong(text: String) {
    toast(ContextHolder.context, text, Toast.LENGTH_LONG)
  }

  private fun toast(context: Context?, text: String?, duration: Int = Toast.LENGTH_SHORT) {
    context ?: return
    if (TextUtils.isEmpty(text)) {
      return
    }
    Toast.makeText(context, text, duration).show()
  }
}