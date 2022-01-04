package com.sunfusheng.mvvm.util

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import com.sunfusheng.mvvm.ContextHolder


/**
 * @author sunfusheng
 * @since  2022/01/04
 */
object ScreenUtil {

  private val mWindowManager by lazy {
    ContextHolder.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
  }

  fun getScreenWidth(): Int {
    return ResourceUtil.getResources().displayMetrics.widthPixels
  }

  fun getScreenHeight(): Int {
    return ResourceUtil.getResources().displayMetrics.heightPixels
  }

  fun getRealScreenWidth(): Int {
    val wm = mWindowManager ?: return getScreenWidth()
    val outMetrics = DisplayMetrics()
    wm.defaultDisplay.getRealMetrics(outMetrics)
    return outMetrics.widthPixels
  }

  fun getRealScreenHeight(): Int {
    val wm = mWindowManager ?: return getScreenHeight()
    val outMetrics = DisplayMetrics()
    wm.defaultDisplay.getRealMetrics(outMetrics)
    return outMetrics.heightPixels
  }
}