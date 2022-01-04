package com.sunfusheng.mvvm.util

import android.content.res.Resources
import com.sunfusheng.mvvm.ContextHolder

/**
 * @author sunfusheng
 * @since  2022/01/04
 */
object ResourceUtil {

  fun getResources(): Resources {
    return ContextHolder.context.resources
  }
}