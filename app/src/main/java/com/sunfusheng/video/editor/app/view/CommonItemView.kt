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
  val vStatus: TextView
  val vDivider: View

  init {
    LayoutInflater.from(context).inflate(R.layout.layout_common_item_view, this)
    vTitle = findViewById(R.id.vTitle)
    vSubtitle = findViewById(R.id.vSubtitle)
    vStatus = findViewById(R.id.vStatus)
    vDivider = findViewById(R.id.vDivider)

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

  fun setStatus(status: String?) {
    if (TextUtils.isEmpty(status)) {
      vStatus.gone()
    } else {
      vStatus.visible()
      vStatus.text = status
    }
  }

  fun setDividerVisible(visible: Boolean) = if (visible) vDivider.visible() else vDivider.gone()
}