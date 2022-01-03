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

  private const val PHOTO_DIR = "images"
  private const val VIDEO_DIR = "videos"

  private const val PHOTO_EXTENSION = ".jpg"
  private const val VIDEO_EXTENSION = ".mp4"

  private val PHOTO_FILENAME_FORMAT = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.CHINA)
  private val VIDEO_FILENAME_FORMAT = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.CHINA)

  private fun getOutputDirectory(dirName: String): File {
    val context = ContextHolder.context
    val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
      File(it, dirName).apply { mkdirs() }
    }
    return if (mediaDir != null && mediaDir.exists())
      mediaDir else context.filesDir
  }

  fun getImageFile(): File {
    return File(
      getOutputDirectory(PHOTO_DIR),
      "IMG_" + PHOTO_FILENAME_FORMAT.format(System.currentTimeMillis()) + PHOTO_EXTENSION
    )
  }

  fun getVideoFile(): File {
    return File(
      getOutputDirectory(VIDEO_DIR),
      "VID_" + VIDEO_FILENAME_FORMAT.format(System.currentTimeMillis()) + VIDEO_EXTENSION
    )
  }
}