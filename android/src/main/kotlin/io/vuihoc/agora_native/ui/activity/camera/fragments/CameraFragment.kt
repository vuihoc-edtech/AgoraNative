//package io.vuihoc.agora_native.ui.activity.camera.fragments
//
//import android.content.*
//import android.content.res.Configuration
//import android.graphics.Color
//import android.graphics.drawable.ColorDrawable
//import android.hardware.display.DisplayManager
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.provider.MediaStore
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.camera.core.*
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.concurrent.futures.await
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.lifecycleScope
//import androidx.navigation.Navigation
//import androidx.window.layout.WindowMetricsCalculator
//// import dagger.hilt.android.AndroidEntryPoint
//import io.vuihoc.agora_native.R
//import io.vuihoc.agora_native.databinding.CameraUiContainerBinding
//import io.vuihoc.agora_native.databinding.FragmentCameraBinding
//import io.vuihoc.agora_native.di.interfaces.Logger
//import io.vuihoc.agora_native.util.ANIMATION_FAST_MILLIS
//import io.vuihoc.agora_native.util.ANIMATION_SLOW_MILLIS
//import io.vuihoc.agora_native.util.padWithDisplayCutout
//import io.vuihoc.agora_native.util.showToast
//import kotlinx.coroutines.launch
//import java.text.SimpleDateFormat
//import java.util.*
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//
//import kotlin.math.abs
//import kotlin.math.max
//import kotlin.math.min
//
//
//class CameraFragment : Fragment() {
//    private var _fragmentCameraBinding: FragmentCameraBinding? = null
//
//    private val fragmentCameraBinding get() = _fragmentCameraBinding!!
//
//    private var cameraUiContainerBinding: CameraUiContainerBinding? = null
//
//    private var displayId: Int = -1
//    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
//    private var preview: Preview? = null
//    private var imageCapture: ImageCapture? = null
//    private var imageAnalyzer: ImageAnalysis? = null
//    private var camera: Camera? = null
//    private var cameraProvider: ProcessCameraProvider? = null
//
//    private val displayManager by lazy {
//        requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
//    }
//
//    private lateinit var cameraExecutor: ExecutorService
//
//    /**
//     * We need a display listener for orientation changes that do not trigger a configuration
//     * change, for example if we choose to override config change in manifest or for 180-degree
//     * orientation changes.
//     */
//    private val displayListener = object : DisplayManager.DisplayListener {
//        override fun onDisplayAdded(displayId: Int) = Unit
//        override fun onDisplayRemoved(displayId: Int) = Unit
//        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
//            if (displayId == this@CameraFragment.displayId) {
//                Log.d("Vuihoc_Log","[$TAG] Rotation changed: ${view.display.rotation}")
//                imageCapture?.targetRotation = view.display.rotation
//                imageAnalyzer?.targetRotation = view.display.rotation
//            }
//        } ?: Unit
//    }
//
//    override fun onResume() {
//        super.onResume()
//        // Make sure that all permissions are still present, since the
//        // user could have removed them while the app was in paused state.
//        if (!PermissionsFragment.hasPermissions(requireContext())) {
//            Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
//                CameraFragmentDirections.actionCameraToPermissions()
//            )
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)
//        return fragmentCameraBinding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        cameraExecutor = Executors.newSingleThreadExecutor()
//
//        displayManager.registerDisplayListener(displayListener, null)
//
//        // Wait for the views to be properly laid out
//        fragmentCameraBinding.viewFinder.post {
//
//            // Keep track of the display in which this view is attached
//            displayId = fragmentCameraBinding.viewFinder.display.displayId
//
//            // Build UI controls
//            updateCameraUi()
//
//            // Set up the camera and its use cases
//            lifecycleScope.launch {
//                setUpCamera()
//            }
//        }
//
//        fragmentCameraBinding.close.setOnClickListener {
//            activity?.finish()
//        }
//
//        // Make sure that the cutout "safe area" avoids the screen notch if any
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            // Use extension method to pad "inside" view containing UI using display cutout's bounds
//            fragmentCameraBinding.cutoutSafeArea.padWithDisplayCutout()
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//
//        _fragmentCameraBinding = null
//
//        cameraExecutor.shutdown()
//
//        displayManager.unregisterDisplayListener(displayListener)
//    }
//
//    /**
//     * Inflate camera controls and update the UI manually upon config changes to avoid removing
//     * and re-adding the view finder from the view hierarchy; this provides a seamless rotation
//     * transition on devices that support it.
//     *
//     * NOTE: The flag is supported starting in Android 8 but there still is a small flash on the
//     * screen for devices that run Android 9 or below.
//     */
//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//
//        updateCameraUi()
//
//        // Rebind the camera with the updated display metrics
//        bindCameraUseCases()
//
//        // Enable or disable switching between cameras
//        updateCameraSwitchButton()
//    }
//
//    /** Initialize CameraX, and prepare to bind the camera use cases  */
//    private suspend fun setUpCamera() {
//        cameraProvider = ProcessCameraProvider.getInstance(requireContext()).await()
//
//        // Select lensFacing depending on the available cameras
//        lensFacing = when {
//            hasBackCamera() -> CameraSelector.LENS_FACING_BACK
//            hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
//            else -> throw IllegalStateException("Back and front camera are unavailable")
//        }
//
//        updateCameraSwitchButton()
//
//        bindCameraUseCases()
//    }
//
//    /** Declare and bind preview, capture and analysis use cases */
//    private fun bindCameraUseCases() {
//
//        // Get screen metrics used to setup camera for full screen resolution
//        val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(requireActivity()).bounds
//        Log.d("Vuihoc_Log","[$TAG] Screen metrics: ${metrics.width()} x ${metrics.height()}")
//
//        val screenAspectRatio = aspectRatio(metrics.width(), metrics.height())
//        Log.d("Vuihoc_Log","[$TAG] Preview aspect ratio: $screenAspectRatio")
//
//        val rotation = fragmentCameraBinding.viewFinder.display.rotation
//
//        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")
//
//        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
//
//        preview = Preview.Builder()
//            .setTargetAspectRatio(screenAspectRatio)
//            .setTargetRotation(rotation)
//            .build()
//
//        imageCapture = ImageCapture.Builder()
//            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
//            .setTargetAspectRatio(screenAspectRatio)
//            .setTargetRotation(rotation)
//            .setJpegQuality(80)
//            .build()
//
//        imageAnalyzer = ImageAnalysis.Builder()
//            .setTargetAspectRatio(screenAspectRatio)
//            .setTargetRotation(rotation)
//            .build()
//
//        cameraProvider.unbindAll()
//
//        if (camera != null) {
//            // Must remove observers from the previous camera instance
//            removeCameraStateObservers(camera!!.cameraInfo)
//        }
//
//        try {
//            camera = cameraProvider.bindToLifecycle(
//                this, cameraSelector, preview, imageCapture, imageAnalyzer
//            )
//            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
//            observeCameraState(camera?.cameraInfo!!)
//        } catch (exc: Exception) {
//            activity?.showToast("Use case binding failed")
//           Log.d("Vuihoc_Log","Use case binding failed", exc)
//        }
//    }
//
//    private fun removeCameraStateObservers(cameraInfo: CameraInfo) {
//        cameraInfo.cameraState.removeObservers(viewLifecycleOwner)
//    }
//
//    private fun observeCameraState(cameraInfo: CameraInfo) {
//        cameraInfo.cameraState.observe(viewLifecycleOwner) { cameraState ->
//            Log.d("Vuihoc_Log","camera state updated: $cameraState")
//            cameraState.error?.let { error ->
//                activity?.showToast("camera encounters an error")
//               Log.d("Vuihoc_Log","camera encounters an error: $error")
//            }
//        }
//    }
//
//    /**
//     *  [androidx.camera.core.ImageAnalysis.Builder] requires enum value of
//     *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
//     *
//     *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
//     *  of preview ratio to one of the provided values.
//     *
//     *  @param width - preview width
//     *  @param height - preview height
//     *  @return suitable aspect ratio
//     */
//    private fun aspectRatio(width: Int, height: Int): Int {
//        val previewRatio = max(width, height).toDouble() / min(width, height)
//        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
//            return AspectRatio.RATIO_4_3
//        }
//        return AspectRatio.RATIO_16_9
//    }
//
//    /** Method used to re-draw the camera UI controls, called every time configuration changes. */
//    private fun updateCameraUi() {
//        cameraUiContainerBinding?.root?.let {
//            fragmentCameraBinding.root.removeView(it)
//        }
//
//        cameraUiContainerBinding = CameraUiContainerBinding.inflate(
//            LayoutInflater.from(requireContext()),
//            fragmentCameraBinding.root,
//            true
//        )
//
//        cameraUiContainerBinding?.cameraCaptureButton?.setOnClickListener {
//            // Disable camera controls, as a simple implementation of loading state.
//            cameraUiContainerBinding?.cameraCaptureButton?.isEnabled = false
//
//            imageCapture?.let { imageCapture ->
//                val name = SimpleDateFormat(FILENAME, Locale.US).format(System.currentTimeMillis())
//                val contentValues = ContentValues().apply {
//                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
//                    put(MediaStore.MediaColumns.MIME_TYPE, PHOTO_TYPE)
//                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//                        val appName = requireContext().resources.getString(R.string.app_name)
//                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/${appName}")
//                    }
//                }
//
//                val outputOptions = ImageCapture.OutputFileOptions
//                    .Builder(
//                        requireContext().contentResolver,
//                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                        contentValues
//                    )
//                    .build()
//
//                imageCapture.takePicture(outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
//                    override fun onError(exc: ImageCaptureException) {
//                        cameraUiContainerBinding?.cameraCaptureButton?.run {
//                            post { isEnabled = true }
//                        }
//
//                       Log.d("Vuihoc_Log","[$TAG] Photo capture failed: ${exc.message}", exc)
//                    }
//
//                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//                        cameraUiContainerBinding?.cameraCaptureButton?.run {
//                            post { isEnabled = true }
//                        }
//
//                        val savedUri = output.savedUri ?: return
//                        Log.d("Vuihoc_Log","[$TAG] Photo capture succeeded: $savedUri")
//
//                        // Implicit broadcasts will be ignored for devices running API level >= 24
//                        // so if you only target API level 24+ you can remove this statement
//                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//                            // Suppress deprecated Camera usage needed for API level 23 and below
//                            @Suppress("DEPRECATION")
//                            requireActivity().sendBroadcast(
//                                Intent(android.hardware.Camera.ACTION_NEW_PICTURE, savedUri)
//                            )
//                        }
//
//                        navigateToGallery(savedUri)
//                    }
//                })
//
//                // We can only change the foreground Drawable using API level 23+ API
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//                    // Display flash animation to indicate that photo was captured
//                    fragmentCameraBinding.root.postDelayed({
//                        fragmentCameraBinding.root.foreground = ColorDrawable(Color.WHITE)
//                        fragmentCameraBinding.root.postDelayed(
//                            { fragmentCameraBinding.root.foreground = null }, ANIMATION_FAST_MILLIS
//                        )
//                    }, ANIMATION_SLOW_MILLIS)
//                }
//            }
//        }
//
//        cameraUiContainerBinding?.cameraSwitchButton?.let {
//            it.isEnabled = false
//            it.setOnClickListener {
//                lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
//                    CameraSelector.LENS_FACING_BACK
//                } else {
//                    CameraSelector.LENS_FACING_FRONT
//                }
//                bindCameraUseCases()
//            }
//        }
//    }
//
//    private fun navigateToGallery(uri: Uri) {
//        lifecycleScope.launchWhenStarted {
//            Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
//                CameraFragmentDirections.actionCameraToGallery(uri.toString())
//            )
//        }
//    }
//
//    private fun updateCameraSwitchButton() {
//        try {
//            cameraUiContainerBinding?.cameraSwitchButton?.isEnabled = hasBackCamera() && hasFrontCamera()
//        } catch (exception: CameraInfoUnavailableException) {
//            cameraUiContainerBinding?.cameraSwitchButton?.isEnabled = false
//        }
//    }
//
//    private fun hasBackCamera(): Boolean {
//        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
//    }
//
//    private fun hasFrontCamera(): Boolean {
//        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
//    }
//
//    companion object {
//        private const val TAG = "CameraFragment"
//        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
//        private const val PHOTO_TYPE = "image/jpeg"
//        private const val RATIO_4_3_VALUE = 4.0 / 3.0
//        private const val RATIO_16_9_VALUE = 16.0 / 9.0
//    }
//}
