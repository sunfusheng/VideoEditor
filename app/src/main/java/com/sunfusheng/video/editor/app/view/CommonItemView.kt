package com.sunfusheng.video.editor.app.view

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.sunfusheng.mvvm.ktx.gone
import com.sunfusheng.mvvm.ktx.visible
import com.sunfusheng.video.editor.app.R

/**
 * @author sunfusheng
 * @since  2021/12/31
 */
class CommonItemView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

  val vTitle: TextView
  val vSubtitle: TextView
  val vDivider: View

  init {
    LayoutInflater.from(context).inflate(R.layout.layout_common_item_view, this)
    vTitle = findViewById(R.id.v_title)
    vSubtitle = findViewById(R.id.v_subtitle)
    vDivider = findViewById(R.id.v_divider)
    vSubtitle.gone()
  }

  fun setTitle(title: String?) {
    vTitle.text = title
  }

  fun setSubTitle(subtitle: String?) {
    if (TextUtils.isEmpty(subtitle)) {
      vSubtitle.gone()
    } else {
      vSubtitle.visible()
      vSubtitle.text = subtitle
    }
  }

  fun setDividerVisible(visible: Boolean) = if (visible) vDivider.visible() else vDivider.gone()
}