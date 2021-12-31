package com.sunfusheng.mvvm.base

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity

/**
 * @author sunfusheng
 * @since  2021/12/31
 */
abstract class BaseActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  protected fun initActionBar(
    title: String,
    subtitle: String = "",
    showHomeAsUp: Boolean = true
  ) {
    val actionBar: ActionBar? = supportActionBar
    actionBar?.apply {
      this.title = title
      this.subtitle = subtitle
      this.setDisplayHomeAsUpEnabled(showHomeAsUp)
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        onBackPressed()
      }
    }
    return super.onOptionsItemSelected(item)
  }
}