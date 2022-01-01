package com.sunfusheng.video.editor.app

import android.content.Intent
import android.os.Bundle
import com.sunfusheng.camera.CameraActivity
import com.sunfusheng.mvvm.base.BaseActivity
import com.sunfusheng.mvvm.ktx.viewBinding
import com.sunfusheng.video.editor.app.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

  private val binding by viewBinding<ActivityMainBinding>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initView()
  }

  private fun initView() {
    binding.apply {
      vRecordVideo.apply {
        setTitle("拍照/录视频")
        setDividerVisible(false)
        setOnClickListener {
          startActivity(Intent(this@MainActivity, CameraActivity::class.java))
        }
      }
    }
  }
}