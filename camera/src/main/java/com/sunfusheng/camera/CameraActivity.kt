package com.sunfusheng.camera

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Display
import android.view.Surface
import android.widget.RelativeLayout
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.permissionx.guolindev.PermissionX
import com.sunfusheng.camera.config.PreviewRatio
import com.sunfusheng.camera.databinding.ActivityCameraBinding
import com.sunfusheng.camera.util.CameraFileUtil
import com.sunfusheng.mvvm.base.BaseActivity
import com.sunfusheng.mvvm.ktx.gone
import com.sunfusheng.mvvm.ktx.viewBinding
import com.sunfusheng.mvvm.ktx.visible
import com.sunfusheng.mvvm.util.ScreenUtil
import com.sunfusheng.mvvm.util.ToastUtil
import java.util.concurrent.Executors

/**
 * @author sunfusheng
 * @since  2022/01/01
 */
class CameraActivity : BaseActivity() {

  private val binding by viewBinding<ActivityCameraBinding>()

  private var mCameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
  private var mCameraProvider: ProcessCameraProvider? = null
  private var mPreview: Preview? = null
  private var mCameraSelector: CameraSelector? = null
  private var mImageAnalysis: ImageAnalysis? = null
  private var mImageCapture: ImageCapture? = null
  private var mCamera: Camera? = null
  private val mCameraExecutor by lazy { Executors.newSingleThreadExecutor() }
  private var isFrontCamera = false
  private val mScreenWidth by lazy { ScreenUtil.getRealScreenWidth() }
  private val mScreenHeight by lazy { ScreenUtil.getRealScreenHeight() }
  private val mScreenHeight_16_9 = mScreenWidth * 16 / 9
  private val mScreenHeight_4_3 = mScreenWidth * 4 / 3
  private var mPreviewRatio = PreviewRatio.RATIO_FULL_SCREEN
  private var mPreviewWidth = mScreenWidth
  private var mPreviewHeight = mScreenHeight
  private var mRotation = Surface.ROTATION_0

  companion object {
    private const val TAG = "CameraActivity"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requestCameraPermission {
      initData()
      initView()
      setupCamera()
    }
  }

  override fun onDestroy() {
    mCameraExecutor.shutdown()
    super.onDestroy()
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
              ToastUtil.toast("权限未通过，已退出")
              finish()
            }
          }
        }
    } else {
      grantedCallback?.invoke()
    }
  }

  private fun initData() {

  }

  private fun initView() {
    resizePreviewHeight()
    binding.vTakePicture.setOnClickListener {
      takePicture()
    }
    binding.vSwitchCamera.setOnClickListener {
      showPlaceholderImage(true)
      switchCamera()
    }
    binding.vChangePreviewRatio.setOnClickListener {
      changePreviewRatio()
    }
  }

  private fun resizePreviewHeight() {
    initPreviewRatio()
    val topMargin = when (mPreviewRatio) {
      PreviewRatio.RATIO_FULL_SCREEN -> 0
      PreviewRatio.RATIO_16_9 -> (mScreenHeight - mScreenHeight_16_9) * 3 / 5
      PreviewRatio.RATIO_4_3 -> (mScreenHeight - mScreenHeight_16_9) * 3 / 5
      PreviewRatio.RATIO_1_1 -> (mScreenHeight - mScreenWidth) / 3
      else -> (mScreenHeight - mPreviewHeight) / 2
    }
    Log.d(TAG, "[sfs] topMargin:$topMargin")
    binding.vPreviewContainer.apply {
      val params = layoutParams as RelativeLayout.LayoutParams
      params.height = mPreviewHeight
      params.topMargin = topMargin
      layoutParams = params
    }
  }

  private fun changePreviewRatio() {
    when (mPreviewRatio) {
      PreviewRatio.RATIO_FULL_SCREEN -> {
        mPreviewRatio = PreviewRatio.RATIO_16_9
        binding.vChangePreviewRatio.text = "16:9"
        showPlaceholderImage(true)
        resizePreviewHeight()
        bindCamera()
      }
      PreviewRatio.RATIO_16_9 -> {
        mPreviewRatio = PreviewRatio.RATIO_4_3
        binding.vChangePreviewRatio.text = "4:3"
        showPlaceholderImage(true)
        resizePreviewHeight()
        bindCamera()
      }
      PreviewRatio.RATIO_4_3 -> {
        mPreviewRatio = PreviewRatio.RATIO_1_1
        binding.vChangePreviewRatio.text = "1:1"
        showPlaceholderImage(true)
        resizePreviewHeight()
        bindCamera()
      }
      PreviewRatio.RATIO_1_1 -> {
        mPreviewRatio = PreviewRatio.RATIO_FULL_SCREEN
        binding.vChangePreviewRatio.text = "全屏"
        showPlaceholderImage(true)
        resizePreviewHeight()
        bindCamera()
      }
    }
  }

  fun takePicture() {
    val imageCapture = mImageCapture ?: return
    val outputFileOptions = ImageCapture.OutputFileOptions
      .Builder(CameraFileUtil.getImageFile())
      .build()
    imageCapture.takePicture(outputFileOptions, mCameraExecutor,
      object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
          runOnUiThread {
            ToastUtil.toast("${outputFileResults.savedUri}")
            flash()
          }
        }

        override fun onError(exception: ImageCaptureException) {
          Log.e(TAG, "[sfs] take picture error: ${exception.message}")
        }
      })
  }

  private fun flash() {
    binding.vFlash.visible()
    binding.vFlash.postDelayed({ binding.vFlash.gone() }, 50)
  }

  fun switchCamera() {
    isFrontCamera = !isFrontCamera
    bindCamera()
  }

  private fun setupCamera() {
    mCameraProviderFuture = ProcessCameraProvider.getInstance(this)
    mCameraProviderFuture?.addListener({
      mCameraProvider = mCameraProviderFuture?.get()
      bindCamera()
    }, ContextCompat.getMainExecutor(this))
  }

  private fun bindCamera() {
    val cameraProvider = mCameraProvider
    if (cameraProvider == null) {
      Log.e(TAG, "[sfs] bindCamera() Camera init failed")
      return
    }

    initPreviewRatio()
    initRotation()
    val preview = getPreview()
    val cameraSelector = getCameraSelector()
    val imageAnalysis = getImageAnalysis()
    val imageCapture = getImageCapture()
    cameraProvider.unbindAll()
    mCamera = cameraProvider.bindToLifecycle(
      this as LifecycleOwner,
      cameraSelector,
      preview,
      imageAnalysis,
      imageCapture
    )
    observeCameraState(mCamera?.cameraInfo)
  }

  private fun initPreviewRatio() {
    mPreviewWidth = mScreenWidth
    mPreviewHeight = when (mPreviewRatio) {
      PreviewRatio.RATIO_16_9 -> mScreenHeight_16_9
      PreviewRatio.RATIO_4_3 -> mScreenHeight_4_3
      PreviewRatio.RATIO_1_1 -> mScreenWidth
      else -> mScreenHeight
    }
  }

  private fun initRotation() {
    val display: Display? = binding.vPreviewView.display
    mRotation = display?.rotation ?: Surface.ROTATION_0
  }

  private fun getPreview(): Preview {
    mPreview = Preview.Builder()
      .setTargetResolution(Size(mPreviewWidth, mPreviewHeight))
      .setTargetRotation(mRotation)
      .build()
      .also {
        it.setSurfaceProvider(binding.vPreviewView.surfaceProvider)
      }
    return mPreview!!
  }

  private fun getCameraSelector(): CameraSelector {
    var lensFacing = CameraSelector.LENS_FACING_BACK
    if (isFrontCamera) {
      lensFacing = CameraSelector.LENS_FACING_FRONT
    }
    mCameraSelector = CameraSelector.Builder()
      .requireLensFacing(lensFacing)
      .build()
    return mCameraSelector!!
  }

  private fun getImageAnalysis(): ImageAnalysis {
    mImageAnalysis = ImageAnalysis.Builder()
      .setTargetResolution(Size(mPreviewWidth, mPreviewHeight))
      .setTargetRotation(mRotation)
      .build()
      .also {
        it.setAnalyzer(mCameraExecutor, { imageProxy ->
          imageProxy.close()
        })
      }
    return mImageAnalysis!!
  }

  private fun getImageCapture(): ImageCapture {
    mImageCapture = ImageCapture.Builder()
      .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
      .setTargetResolution(Size(mPreviewWidth, mPreviewHeight))
      .setTargetRotation(mRotation)
      .build()
    return mImageCapture!!
  }

  var startTime = 0L

  private fun showPlaceholderImage(show: Boolean) {
    binding.apply {
      if (show) {
        startTime = System.currentTimeMillis()
        val bitmap = vPreviewView.bitmap//BitmapBlurUtil.blur(vPreviewView.bitmap)
        Log.e(TAG, "[sfs] get bitmap time: ${System.currentTimeMillis() - startTime}")
        vPlaceholder.visible()
        vPlaceholder.setImageBitmap(bitmap)
        vFlash.visible()
        vFlash.setBackgroundColor(resources.getColor(R.color.transparent30_white))
      } else {
        Log.e(TAG, "[sfs] switch camera time: ${System.currentTimeMillis() - startTime}")
        vPlaceholder.gone()
        vPlaceholder.setImageBitmap(null)
        vFlash.gone()
        vFlash.setBackgroundColor(resources.getColor(R.color.white))
      }
    }
  }

  private var hasObserveCameraState = false

  private fun observeCameraState(cameraInfo: CameraInfo?) {
    if (hasObserveCameraState) {
      return
    }
    if (cameraInfo == null) {
      Log.e(TAG, "[sfs] CameraState: cameraInfo is null")
      return
    }
    hasObserveCameraState = true
    binding.vPreviewView.previewStreamState.observe(this as LifecycleOwner) { previewStreamState ->
      previewStreamState?.also {
        when (it) {
          PreviewView.StreamState.IDLE -> {
            Log.d(TAG, "[sfs] PreviewStreamState: Idle")
          }
          PreviewView.StreamState.STREAMING -> {
            Log.d(TAG, "[sfs] PreviewStreamState: Streaming")
            showPlaceholderImage(false)
          }
        }
      }
    }
    cameraInfo.cameraState.observe(this as LifecycleOwner) { cameraState ->
      run {
        when (cameraState.type) {
          CameraState.Type.PENDING_OPEN -> {
            Log.d(TAG, "[sfs] CameraState: Pending Open")
          }
          CameraState.Type.OPENING -> {
            Log.d(TAG, "[sfs] CameraState: Opening")
          }
          CameraState.Type.OPEN -> {
            Log.d(TAG, "[sfs] CameraState: Open")
          }
          CameraState.Type.CLOSING -> {
            Log.d(TAG, "[sfs] CameraState: Closing")
          }
          CameraState.Type.CLOSED -> {
            Log.d(TAG, "[sfs] CameraState: Closed")
          }
        }
      }

      cameraState.error?.let { error ->
        when (error.code) {
          // Open errors
          CameraState.ERROR_STREAM_CONFIG -> {
            Log.e(TAG, "[sfs] CameraErrorState: Stream config error")
          }
          // Opening errors
          CameraState.ERROR_CAMERA_IN_USE -> {
            Log.e(TAG, "[sfs] CameraErrorState: Camera in use")
          }
          CameraState.ERROR_MAX_CAMERAS_IN_USE -> {
            Log.e(TAG, "[sfs] CameraErrorState: Max cameras in use")
          }
          CameraState.ERROR_OTHER_RECOVERABLE_ERROR -> {
            Log.e(TAG, "[sfs] CameraErrorState: Other recoverable error")
          }
          // Closing errors
          CameraState.ERROR_CAMERA_DISABLED -> {
            Log.e(TAG, "[sfs] CameraErrorState: Camera disabled")
          }
          CameraState.ERROR_CAMERA_FATAL_ERROR -> {
            Log.e(TAG, "[sfs] CameraErrorState: Fatal error")
          }
          // Closed errors
          CameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED -> {
            Log.e(TAG, "[sfs] CameraErrorState: Do not disturb mode enabled")
          }
        }
      }
    }
  }
}