package com.sunfusheng.mvvm

import android.annotation.SuppressLint
import android.content.Context
import androidx.startup.Initializer

/**
 * @author sunfusheng
 * @since  2022/01/10
 */
class ContextHolderInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    ContextHolder.context = context
  }

  override fun dependencies(): MutableList<Class<out Initializer<*>>> {
    return mutableListOf()
  }
}

@SuppressLint("StaticFieldLeak")
object ContextHolder {
  lateinit var context: Context
}