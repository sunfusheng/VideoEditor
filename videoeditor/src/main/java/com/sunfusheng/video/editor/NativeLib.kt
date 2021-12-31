package com.sunfusheng.video.editor

class NativeLib {

  companion object {
    init {
      System.loadLibrary("editor")
    }
  }

  external fun stringFromJNI(): String
}