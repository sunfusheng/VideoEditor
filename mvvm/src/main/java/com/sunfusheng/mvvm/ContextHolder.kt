package com.sunfusheng.mvvm

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * @author sunfusheng
 * @since  2021/12/31
 */
@SuppressLint("StaticFieldLeak")
object ContextHolder {
  lateinit var context: Context
  lateinit var application: Application

  fun init(app: Application) {
    context = app
    application = app
  }
}