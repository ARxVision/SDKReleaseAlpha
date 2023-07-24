package com.arx.sampleapp

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arx.camera.ArxHeadsetApi
import com.arx.camera.foreground.ArxHeadsetHandler
import com.arx.camera.headsetbutton.ArxHeadsetButton
import com.arx.camera.jni.FrameDesc
import com.arx.camera.ui.ArxPermissionActivityResult
import com.arx.sampleapp.databinding.ActivityMainBinding
import timber.log.Timber

class ArxHeadsetSampleActivity : AppCompatActivity() {

    private var arxHeadsetHandler: ArxHeadsetHandler? = null

    private lateinit var binding: ActivityMainBinding

    // Create an instance of MyActivityResultContract
    private val myActivityResultContract = com.arx.camera.ui.ArxPermissionActivityResultContract()

    private val myActivityResultLauncher = registerForActivityResult(myActivityResultContract) {
        when (it) {
            ArxPermissionActivityResult.AllPermissionsGranted -> arxHeadsetHandler?.startHeadSetService()

            ArxPermissionActivityResult.BackPressed -> Unit

            ArxPermissionActivityResult.CloseAppRequested -> finish()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Timber.w("onNewIntent")
    }

    private fun handleUiState(uiState: UiState) {
        Timber.w("handleUiState $uiState")
        when (uiState) {
            UiState.ArxHeadsetConnected -> {
                binding.viewDeviceConnected.root.visibility = View.VISIBLE
                binding.viewDisconnected.root.visibility = View.GONE
                with(binding.viewDeviceConnected) {
                    startService.text = "Stop Arx Headset"
                    startService.visibility = View.VISIBLE
                    startService.setOnClickListener {
                        arxHeadsetHandler?.stopHeadsetService()
                    }
                }
            }

            UiState.DeviceDisconnected -> {
                binding.viewDeviceConnected.root.visibility = View.GONE
                binding.viewDisconnected.root.visibility = View.VISIBLE
                binding.viewDisconnected.textErrorTitle.text = "Device Disconnected"
                binding.viewDisconnected.textErrorSubtitle.text =
                    "Plugin the device to start the Arx Headset"
                binding.viewDisconnected.startService.apply {
                    text = "Start Arx Headset"
                    visibility = View.VISIBLE
                    setOnClickListener {
                        arxHeadsetHandler?.startHeadSetService()
                    }
                }
            }

            is UiState.DeviceError -> {
                Timber.e(uiState.throwable)
                binding.viewDeviceConnected.root.visibility = View.GONE
                binding.viewDisconnected.root.visibility = View.VISIBLE
                binding.viewDisconnected.textErrorTitle.text = "Device Streaming error"
                binding.viewDisconnected.textErrorSubtitle.text = "${uiState.throwable}"
                binding.viewDisconnected.startService.apply {
                    text = "Restart Service"
                    visibility = View.VISIBLE
                    setOnClickListener {
                        arxHeadsetHandler?.startHeadSetService()
                    }
                }
            }

            UiState.PermissionNotGiven -> {
                binding.viewDeviceConnected.root.visibility = View.GONE
                binding.viewDisconnected.root.visibility = View.VISIBLE
                binding.viewDisconnected.textErrorTitle.text = "ArxHeadset Permission not given"
                binding.viewDisconnected.textErrorSubtitle.text =
                    " Press permission button UI to give required permission "
                binding.viewDisconnected.startService.apply {
                    text = "Launch Permission UI"
                    visibility = View.VISIBLE
                    setOnClickListener {
                        myActivityResultLauncher.launch(true)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        Timber.w("onDestroy")
        super.onDestroy()
        arxHeadsetHandler?.stopHeadsetService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.w("onCreate")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root)
        title = "Arx SDK sample"
        arxHeadsetHandler = ArxHeadsetHandler(this, object : ArxHeadsetApi {
            override fun onDeviceConnectionError(throwable: Throwable) {
                handleUiState(UiState.DeviceError("", throwable))
            }

            override fun onDevicePhotoReceived(bitmap: Bitmap, frameDescriptor: FrameDesc) {
                binding.viewDeviceConnected.imageButton.setImageBitmap(bitmap)
            }

            override fun onButtonClicked(arxButton: ArxHeadsetButton, isLongPress: Boolean) {
                handleArxHeadsetButtonPress(arxButton, isLongPress)
            }

            override fun onPermissionDenied() {
                handleUiState(UiState.PermissionNotGiven)
            }

            override fun onDisconnect() {
                handleUiState(UiState.DeviceDisconnected)
            }

            override fun onCameraResolutionUpdate(
                frameDesc: List<FrameDesc>, selectedFrameDesc: FrameDesc
            ) {
                Timber.e("$frameDesc")
                showSpinner(frameDesc, selectedFrameDesc)
                handleUiState(UiState.ArxHeadsetConnected)
            }
        })
        arxHeadsetHandler?.startHeadSetService()
    }

    private fun handleArxHeadsetButtonPress(
        arxButton: ArxHeadsetButton, isLongPress: Boolean
    ) = with(binding.viewDeviceConnected) {
        when (arxButton) {
            ArxHeadsetButton.Square -> buttonHeadsetSquare
            ArxHeadsetButton.Circle -> buttonHeadsetCircle
            ArxHeadsetButton.Triangle -> buttonHeadsetTriangle
            ArxHeadsetButton.VolumeUp -> buttonHeadsetUp
            ArxHeadsetButton.VolumeDown -> buttonHeadsetDown
        }.apply {
            clearAnimation()
            if (isLongPress) {
                startAnimation(
                    AnimationUtils.loadAnimation(
                        this@ArxHeadsetSampleActivity, R.anim.button_animation
                    )
                )
                this.performLongClick()
            } else {
                this.performClick()
                startAnimation(
                    AnimationUtils.loadAnimation(
                        this@ArxHeadsetSampleActivity, R.anim.button_animation
                    )
                )
            }
        }
    }

    private fun showSpinner(frameDesc: List<FrameDesc>, selectedFrameDesc: FrameDesc) {
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item,
            frameDesc.map { "W:${it.width} x H:${it.height} FPS:${it.frameRate()}" })
        // Specify the layout to use for the dropdown items
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.viewDeviceConnected.resolutionSpinner.apply {
            onItemSelectedListener = null
            this.adapter = adapter
            setSelection(frameDesc.indexOf(selectedFrameDesc))
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    val selectedResolution = frameDesc[position]
                    arxHeadsetHandler?.changeResolution(selectedResolution)
                    // Handle the selected resolution
                    Toast.makeText(
                        this@ArxHeadsetSampleActivity,
                        "Selected Resolution: ${selectedResolution.width} x ${selectedResolution.height}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onNothingSelected(parent: AdapterView<*>) = Unit
            }
        }
    }
}

sealed class UiState {
    object ArxHeadsetConnected : UiState()
    object PermissionNotGiven : UiState()
    object DeviceDisconnected : UiState()
    data class DeviceError(val message: String, val throwable: Throwable) : UiState()
}