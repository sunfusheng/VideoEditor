package com.sunfusheng.video.editor.app

import android.app.Application
import com.sunfusheng.mvvm.ContextHolder

/**
 * @author sunfusheng
 * @since  2021/12/31
 */
class VideoEditorApp : Application() {

  override fun onCreate() {
    super.onCreate()
    ContextHolder.init(this)
  }
}