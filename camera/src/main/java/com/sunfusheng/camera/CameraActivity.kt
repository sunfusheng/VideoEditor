package com.sunfusheng.camera

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
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
import com.sunfusheng.mvvm.ktx.invisible
import com.sunfusheng.mvvm.ktx.viewBinding
import com.sunfusheng.mvvm.ktx.visible
import com.sunfusheng.mvvm.util.ScreenUtil
import com.sunfusheng.mvvm.util.ToastUtil
import java.util.concurrent.Executors

/**
 * @author sunfusheng
 * @since  2022/01/01
 */
@SuppressLint("RestrictedApi")
class CameraActivity : BaseActivity() {

  private val binding by viewBinding<ActivityCameraBinding>()

  private var mCameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
  private var mCameraProvider: ProcessCameraProvider? = null
  private var mPreview: Preview? = null
  private var mCameraSelector: CameraSelector? = null
  private var mImageAnalysis: ImageAnalysis? = null
  private var mImageCapture: ImageCapture? = null
  private var mVideoCapture: VideoCapture? = null
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
  private var mLastPreviewHeight = mPreviewHeight
  private var mTopMargin = 0
  private var mLastTopMargin = 0
  private var mRotation = Surface.ROTATION_0
  private var isTakeCapture = true
  private var isVideoRecording = false

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
    mPreviewWidth = mScreenWidth
    mPreviewHeight = getPreviewHeightByRatio(mPreviewRatio)
    mLastPreviewHeight = mPreviewHeight
    mTopMargin = getPreviewContainerTopMargin(mPreviewRatio)
    mLastTopMargin = mTopMargin
  }

  private fun initView() {
    resizePreviewHeight(false)
    binding.apply {
      vTakePicture.setOnClickListener {
        if (!isTakeCapture) {
          isTakeCapture = true
          bindCamera()
        }
        takePicture()
      }
      vSwitchCamera.setOnClickListener {
        showPlaceholderImage(true)
        switchCamera()
      }
      vChangePreviewRatio.setOnClickListener {
        changePreviewRatio()
      }
      vVideoCapture.setOnClickListener {
        if (isTakeCapture) {
          isTakeCapture = false
          bindCamera()
        }
        if (isVideoRecording) {
          vVideoCapture.text = "开始录像"
          stopRecording()
        } else {
          vVideoCapture.text = "停止录像"
          startRecording()
        }
      }
    }
  }

  var startTime = 0L

  private fun showPlaceholderImage(show: Boolean) {
    binding.apply {
      if (show) {
        startTime = System.currentTimeMillis()
        val bitmap = vPreviewView.bitmap
        vPlaceholder.visible()
        vPlaceholder.setImageBitmap(bitmap)
        vPreviewView.invisible()
      } else {
        Log.e(TAG, "[sfs] switch camera time: ${System.currentTimeMillis() - startTime}")
        resizePreviewHeight(true)
      }
    }
  }

  private var hasObserveCameraState = false

  private fun resizePreviewHeight(anim: Boolean = false) {
    Log.d(TAG, "[sfs] anim:$anim")
    if (anim) {
      Log.d(TAG, "[sfs] mLastPreviewHeight:$mLastPreviewHeight, mPreviewHeight:$mPreviewHeight")
      if (mLastPreviewHeight != mPreviewHeight) {
        val heightAnim = ValueAnimator.ofInt(mLastPreviewHeight, mPreviewHeight)
        heightAnim.addUpdateListener {
          val animatedValue = it.animatedValue as Int
          mLastPreviewHeight = animatedValue
          Log.d(TAG, "[sfs] animatedValue:$animatedValue")
          binding.vPreviewContainer.apply {
            val params = layoutParams as RelativeLayout.LayoutParams
            params.height = animatedValue
            layoutParams = params
          }
          if (mPreviewHeight > mLastPreviewHeight) {
            if (animatedValue >= mPreviewHeight) {
              binding.apply {
                vPlaceholder.gone()
                vPlaceholder.setImageBitmap(null)
                vPreviewView.visible()
              }
            }
          } else {
            if (animatedValue <= mPreviewHeight) {
              binding.apply {
                vPlaceholder.gone()
                vPlaceholder.setImageBitmap(null)
                vPreviewView.visible()
              }
            }
          }
        }
        heightAnim.duration = 300
        heightAnim.start()
      }

      Log.d(TAG, "[sfs] mLastTopMargin:$mLastTopMargin, mTopMargin:$mTopMargin")
      if (mLastTopMargin != mTopMargin) {
        val topMargin = ValueAnimator.ofInt(mLastTopMargin, mTopMargin)
        topMargin.addUpdateListener {
          val animatedValue = it.animatedValue as Int
          mLastTopMargin = animatedValue
          binding.vPreviewContainer.apply {
            val params = layoutParams as RelativeLayout.LayoutParams
            params.topMargin = animatedValue
            layoutParams = params
          }
        }
        topMargin.duration = 300
        topMargin.start()
      }
    } else {
      binding.vPreviewContainer.apply {
        val params = layoutParams as RelativeLayout.LayoutParams
        params.height = mPreviewHeight
        params.topMargin = mTopMargin
        layoutParams = params
      }
    }
  }

  private fun getPreviewContainerTopMargin(@PreviewRatio.Ratio ratio: Int): Int {
    return when (ratio) {
      PreviewRatio.RATIO_FULL_SCREEN -> 0
      PreviewRatio.RATIO_16_9 -> (mScreenHeight - mScreenHeight_16_9) * 3 / 5
      PreviewRatio.RATIO_4_3 -> (mScreenHeight - mScreenHeight_16_9) * 3 / 5
      PreviewRatio.RATIO_1_1 -> (mScreenHeight - mScreenWidth) / 3
      else -> (mScreenHeight - mPreviewHeight) / 2
    }
  }

  private fun changePreviewRatio() {
    mLastPreviewHeight = getPreviewHeightByRatio(mPreviewRatio)
    mLastTopMargin = getPreviewContainerTopMargin(mPreviewRatio)
    when (mPreviewRatio) {
      PreviewRatio.RATIO_FULL_SCREEN -> {
        mPreviewRatio = PreviewRatio.RATIO_16_9
        binding.vChangePreviewRatio.text = "16:9"
      }
      PreviewRatio.RATIO_16_9 -> {
        mPreviewRatio = PreviewRatio.RATIO_4_3
        binding.vChangePreviewRatio.text = "4:3"
      }
      PreviewRatio.RATIO_4_3 -> {
        mPreviewRatio = PreviewRatio.RATIO_1_1
        binding.vChangePreviewRatio.text = "1:1"
      }
      PreviewRatio.RATIO_1_1 -> {
        mPreviewRatio = PreviewRatio.RATIO_FULL_SCREEN
        binding.vChangePreviewRatio.text = "全屏"
      }
    }
    mPreviewHeight = getPreviewHeightByRatio(mPreviewRatio)
    mTopMargin = getPreviewContainerTopMargin(mPreviewRatio)
    showPlaceholderImage(true)
    bindCamera()
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

  @SuppressLint("MissingPermission")
  fun startRecording() {
    val videoCapture = mVideoCapture ?: return
    isVideoRecording = true
    val outputFileOptions = VideoCapture.OutputFileOptions
      .Builder(CameraFileUtil.getVideoFile())
      .build()
    videoCapture.startRecording(outputFileOptions, mCameraExecutor,

      object : VideoCapture.OnVideoSavedCallback {
        override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
          runOnUiThread {
            ToastUtil.toast("${outputFileResults.savedUri}")
          }
        }

        override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
          Log.e(TAG, "[sfs] start recording error: ${cause?.message}")
        }
      })
  }

  fun stopRecording() {
    val videoCapture = mVideoCapture ?: return
    isVideoRecording = false
    videoCapture.stopRecording()
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

    initCameraConfig()
    val preview = getPreview()
    val cameraSelector = getCameraSelector()
    val imageAnalysis = getImageAnalysis()
    val imageCapture = getImageCapture()
    val videoCapture = getVideoCapture()
    val captureUseCase = if (isTakeCapture) imageCapture else videoCapture
    cameraProvider.unbindAll()
    mCamera = cameraProvider.bindToLifecycle(
      this as LifecycleOwner,
      cameraSelector,
      preview,
      imageAnalysis,
      captureUseCase
    )
    observeCameraState(mCamera?.cameraInfo)
  }

  private fun getPreviewHeightByRatio(@PreviewRatio.Ratio ratio: Int): Int {
    return when (ratio) {
      PreviewRatio.RATIO_16_9 -> mScreenHeight_16_9
      PreviewRatio.RATIO_4_3 -> mScreenHeight_4_3
      PreviewRatio.RATIO_1_1 -> mScreenWidth
      else -> mScreenHeight
    }
  }

  private fun initCameraConfig() {
    mPreviewWidth = mScreenWidth
    mPreviewHeight = getPreviewHeightByRatio(mPreviewRatio)
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

  private fun getVideoCapture(): VideoCapture {
    mVideoCapture = VideoCapture.Builder()
      .setTargetResolution(Size(mPreviewWidth, mPreviewHeight))
      .setTargetRotation(mRotation)
      .setBitRate(1024 * 1024)
      .setAudioBitRate(1024 * 512)
      .setVideoFrameRate(50)
      .build()
    return mVideoCapture!!
  }

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