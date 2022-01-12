package com.sunfusheng.camera.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.sunfusheng.camera.databinding.LayoutCaptureIndexViewBinding
import com.sunfusheng.mvvm.ktx.viewBinding

/**
 * @author sunfusheng
 * @since  2022/01/11
 */
class CaptureIndexView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  private val binding = viewBinding<LayoutCaptureIndexViewBinding>()
  private val list = listOf(VIDEO_CAPTURE, IMAGE_CAPTURE)

  companion object {
    const val IMAGE_CAPTURE = "拍照"
    const val VIDEO_CAPTURE = "录像"
  }

  init {
    initTabLayout()
  }

  private fun initTabLayout() {
    binding.apply {
      for (index in list) {
        val tab = vTabLayout.newTab()
        tab.text = index
        vTabLayout.addTab(tab)
      }
    }
  }
}