package com.sunfusheng.video.editor.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sunfusheng.mvvm.ktx.viewBinding
import com.sunfusheng.video.editor.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

  private val binding by viewBinding<ActivityMainBinding>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initView()
  }

  private fun initView() {
    binding.apply {
      vRecordVideo.apply {
        setTitle("视频录制")
        setDividerVisible(false)
        setOnClickListener {

        }
      }
    }
  }
}