package com.sunfusheng.video.editor.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sunfusheng.video.editor.NativeLib
import com.sunfusheng.video.editor.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val nativeLib = NativeLib()
    binding.sampleText.text = nativeLib.stringFromJNI()
  }

}