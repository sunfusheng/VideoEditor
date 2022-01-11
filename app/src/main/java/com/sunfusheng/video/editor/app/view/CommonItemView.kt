package com.sunfusheng.video.editor.app.view

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.sunfusheng.mvvm.ktx.gone
import com.sunfusheng.mvvm.ktx.viewBinding
import com.sunfusheng.mvvm.ktx.visible
import com.sunfusheng.video.editor.app.databinding.LayoutCommonItemViewBinding

/**
 * @author sunfusheng
 * @since  2021/12/31
 */
class CommonItemView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

  val binding = viewBinding<LayoutCommonItemViewBinding>()

  init {
    binding.apply {
      vTitle.visible()
      vSubtitle.gone()
      vDivider.visible()
    }
  }

  fun setTitle(title: String?) {
    binding.vTitle.text = title
  }

  fun setSubTitle(subtitle: String?) {
    binding.apply {
      if (TextUtils.isEmpty(subtitle)) {
        vSubtitle.gone()
      } else {
        vSubtitle.visible()
        vSubtitle.text = subtitle
      }
    }
  }

  fun setDividerVisible(visible: Boolean) {
    binding.apply {
      if (visible) {
        vDivider.visible()
      } else {
        vDivider.gone()
      }
    }
  }
}