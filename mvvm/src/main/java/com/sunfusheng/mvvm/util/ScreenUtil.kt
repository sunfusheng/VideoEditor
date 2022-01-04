package com.sunfusheng.mvvm.util

/**
 * @author sunfusheng
 * @since  2022/01/04
 */
object ScreenUtil {

  fun getScreenWidth(): Int {
    return ResourceUtil.getResources().displayMetrics.widthPixels
  }

  fun getScreenHeight(): Int {
    return ResourceUtil.getResources().displayMetrics.heightPixels
  }
}