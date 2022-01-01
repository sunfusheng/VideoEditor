package com.sunfusheng.camera

import android.Manifest
import android.os.Bundle
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.permissionx.guolindev.PermissionX
import com.sunfusheng.camera.databinding.ActivityCameraBinding
import com.sunfusheng.mvvm.base.BaseActivity
import com.sunfusheng.mvvm.ktx.viewBinding
import com.sunfusheng.mvvm.util.ToastUtil

/**
 * @author sunfusheng
 * @since  2022/01/01
 */
class CameraActivity : BaseActivity() {

  private val binding by viewBinding<ActivityCameraBinding>()

  private var mCameraProvider: ProcessCameraProvider? = null
  private var mPreview: Preview? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requestCameraPermission {
      setupCamera()
    }
  }

  private fun requestCameraPermission(grantedCallback: (() -> Unit)? = null) {
    if (!PermissionX.isGranted(this, Manifest.permission.CAMERA)) {
      PermissionX.init(this)
        .permissions(Manifest.permission.CAMERA)
        .request { allGranted, _, _ ->
          run {
            if (allGranted) {
              grantedCallback?.invoke()
            } else {
              ToastUtil.show("权限未通过，已退出")
              finish()
            }
          }
        }
    } else {
      grantedCallback?.invoke()
    }
  }

  private fun setupCamera() {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
    cameraProviderFuture.addListener({
      mCameraProvider = cameraProviderFuture.get()
      mPreview = Preview.Builder().build()
        .apply {
          setSurfaceProvider(binding.vPreviewView.surfaceProvider)
        }
      val cameraSelector =
        CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
      mCameraProvider?.apply {
        unbindAll()
        bindToLifecycle(this@CameraActivity, cameraSelector, mPreview)
      }
    }, ContextCompat.getMainExecutor(this))
  }
}