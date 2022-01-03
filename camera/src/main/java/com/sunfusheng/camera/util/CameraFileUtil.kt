package com.sunfusheng.camera.util

import com.sunfusheng.mvvm.ContextHolder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author sunfusheng
 * @since  2022/01/03
 */
object CameraFileUtil {

  private const val PHOTO_FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
  private const val PHOTO_EXTENSION = ".jpg"

  fun getOutputDirectory(): File {
    val context = ContextHolder.context
    val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
      File(it, "VideoEditor").apply { mkdirs() }
    }
    return if (mediaDir != null && mediaDir.exists())
      mediaDir else context.filesDir
  }

  private fun createFile(baseFolder: File, format: String, extension: String): File {
    return File(
      baseFolder,
      SimpleDateFormat(format, Locale.CHINA).format(System.currentTimeMillis()) + extension
    )
  }

  fun getTakeCaptureFile(): File {
    return createFile(getOutputDirectory(), PHOTO_FILENAME, PHOTO_EXTENSION)
  }
}