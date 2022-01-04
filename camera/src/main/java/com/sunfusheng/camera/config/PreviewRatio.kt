package com.sunfusheng.camera.config

import androidx.annotation.IntDef

/**
 * @author sunfusheng
 * @since  2022/01/04
 */
object PreviewRatio {
  const val RATIO_FULL_SCREEN = 0
  const val RATIO_16_9 = 1
  const val RATIO_4_3 = 2
  const val RATIO_1_1 = 3

  @IntDef(RATIO_FULL_SCREEN, RATIO_16_9, RATIO_4_3, RATIO_1_1)
  @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
  annotation class Ratio
}
