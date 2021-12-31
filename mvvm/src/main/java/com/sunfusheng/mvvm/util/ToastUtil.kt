package com.sunfusheng.mvvm.util

import android.widget.Toast
import com.sunfusheng.mvvm.ContextHolder

/**
 * @author sunfusheng
 * @since  2021/12/31
 */
object ToastUtil {

  fun show(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(ContextHolder.context, text, duration).show()
  }

}