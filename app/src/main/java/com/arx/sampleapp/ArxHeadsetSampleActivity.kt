package com.arx.sampleapp

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.arx.camera.ArxHeadsetApi
import com.arx.camera.BuildConfig
import com.arx.camera.foreground.ArxHeadsetHandler
import com.arx.camera.headsetbutton.ArxHeadsetButton
import com.arx.camera.headsetbutton.ImuData
import com.arx.camera.jni.UVCException
import com.arx.camera.state.UsbCameraPhotoCaptureException
import com.arx.camera.ui.ArxPermissionActivityResult
import com.arx.camera.util.Resolution
import com.arx.sampleapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import timber.log.Timber


class ArxHeadsetSampleActivity : AppCompatActivity() {

    private var arxHeadsetHandler: ArxHeadsetHandler? = null

    private lateinit var binding: ActivityMainBinding
    private val startResolution = Resolution._640x480

    // Create an instance of MyActivityResultContract
    private val myActivityResultContract = com.arx.camera.ui.ArxPermissionActivityResultContract()

    private val myActivityResultLauncher = registerForActivityResult(myActivityResultContract) {
        when (it) {
            ArxPermissionActivityResult.AllPermissionsGranted -> arxHeadsetHandler?.startHeadSetService(
                startResolution
            )

            ArxPermissionActivityResult.BackPressed -> {
                showMessage("Back Pressed")
            }

            ArxPermissionActivityResult.CloseAppRequested -> {
                showMessage("Close app CTA clicked")
            }

            else -> {}
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Timber.w("onNewIntent")
        arxHeadsetHandler?.startHeadSetService(startResolution)
    }

    private var snackbar: Snackbar? = null
    private fun showMessage(message: String) {
        snackbar?.dismiss()
        snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
        snackbar?.show()
    }

    private fun handleUiState(uiState: UiState) {
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
                showMessage("Device Disconnected, Plugin the device to start the Arx Headset")
                binding.viewDeviceConnected.root.visibility = View.GONE
                binding.viewDisconnected.root.visibility = View.VISIBLE
                binding.viewDisconnected.textErrorTitle.text = "Device Disconnected"
                binding.viewDisconnected.textErrorSubtitle.text =
                    "Plugin the device to start the Arx Headset"
                binding.viewDisconnected.startService.apply {
                    text = "Start Arx Headset"
                    visibility = View.VISIBLE
                    setOnClickListener {
                        arxHeadsetHandler?.startHeadSetService(startResolution)
                    }
                }
            }

            is UiState.DeviceError -> {
                Timber.e(uiState.throwable)
                binding.viewDeviceConnected.root.visibility = View.GONE
                binding.viewDisconnected.root.visibility = View.VISIBLE
                binding.viewDisconnected.textErrorTitle.text = "Device Streaming error"
                binding.viewDisconnected.textErrorSubtitle.text =
                    if (uiState.throwable is UVCException &&
                        uiState.throwable.errorCode == UVCException.UVC_ERROR_DEVICE_BUSY
                    ) {
                        "Arx Headset is currently in use by another application. " +
                                "Please try unplugging and then reconnecting the device." +
                                "${uiState.throwable}"
                    } else {
                        "${uiState.throwable} "
                    }.also {
                        showMessage("Device Error:${it}")
                    }
                binding.viewDisconnected.startService.apply {
                    text = "Restart Service"
                    visibility = View.VISIBLE
                    setOnClickListener {
                        arxHeadsetHandler?.notifyToDisconnectApp()
                    }
                }
            }

            UiState.PermissionNotGiven -> {
                showMessage("Permission Not Given")
                binding.viewDeviceConnected.root.visibility = View.GONE
                binding.viewDisconnected.root.visibility = View.VISIBLE
                binding.viewDisconnected.textErrorTitle.text = "ArxHeadset Permission not given"
                binding.viewDisconnected.textErrorSubtitle.text =
                    " Press permission button UI to give required permission "
                binding.viewDisconnected.startService.apply {
                    text = "Launch Permission UI"
                    visibility = View.VISIBLE
                    setOnClickListener {
                        myActivityResultLauncher.launch(BuildConfig.DEBUG)
                    }
                }
            }

            is UiState.ImuDataUpdate -> {
                with(binding.viewDeviceConnected) {
                    imuData.text = uiState.imuData.toString()
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
        arxHeadsetHandler = ArxHeadsetHandler(this, BuildConfig.DEBUG, object : ArxHeadsetApi {
            override fun onDeviceConnectionError(throwable: Throwable) {
                if (throwable is UsbCameraPhotoCaptureException) {
                    Toast.makeText(
                        this@ArxHeadsetSampleActivity,
                        "Error clicking picture",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    handleUiState(UiState.DeviceError("", throwable))
                }
            }

            override fun onDevicePhotoReceived(bitmap: Bitmap, currentFrameDesc: Resolution) {
                binding.viewDeviceConnected.imageButton.setImageBitmap(bitmap)
            }

            override fun onStillPhotoReceived(bitmap: Bitmap, currentFrameDesc: Resolution) {
                Toast.makeText(
                    this@ArxHeadsetSampleActivity,
                    "photo clicked $currentFrameDesc",
                    Toast.LENGTH_SHORT
                ).show()
                showBitmapDialog(bitmap, currentFrameDesc)
            }

            override fun onButtonClicked(arxButton: ArxHeadsetButton, isLongPress: Boolean) {
                handleArxHeadsetButtonPress(arxButton, isLongPress)
            }

            override fun onPermissionDenied() {
                handleUiState(UiState.PermissionNotGiven)
            }

            override fun onImuDataUpdate(imuData: ImuData) {
                super.onImuDataUpdate(imuData)
                handleUiState(UiState.ImuDataUpdate(imuData))
            }

            override fun onDisconnect() {
                handleUiState(UiState.DeviceDisconnected)
            }

            override fun onCameraResolutionUpdate(
                availableResolutions: List<Resolution>, selectedResolution: Resolution
            ) {
                Timber.e("$availableResolutions")
                showSpinner(availableResolutions, selectedResolution)
                showSpinnerClickPicture(availableResolutions)
                handleUiState(UiState.ArxHeadsetConnected)
            }
        })
        arxHeadsetHandler?.startHeadSetService(startResolution)

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
                        this@ArxHeadsetSampleActivity, com.arx.camera.ui.R.anim.button_animation
                    )
                )
                this.performLongClick()
            } else {
                this.performClick()
                startAnimation(
                    AnimationUtils.loadAnimation(
                        this@ArxHeadsetSampleActivity, com.arx.camera.ui.R.anim.button_animation
                    )
                )
            }
        }
    }

    private fun showSpinnerClickPicture(frameDesc: List<Resolution>) {
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item,
            frameDesc.map { "W:${it.width} x H:${it.height} FPS:${it.frameRate}" })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.viewDeviceConnected.buttonClickPicture.setOnClickListener {
            try {
                val selectedPost =
                    findViewById<Spinner>(R.id.resolutionSpinnerClickPicture).selectedItemPosition
                arxHeadsetHandler?.clickPhoto(frameDesc[selectedPost])
            } catch (error: IllegalStateException) {
                Timber.e(error)
                Toast.makeText(this, "$error", Toast.LENGTH_SHORT).show()
            }
        }
        binding.viewDeviceConnected.resolutionSpinnerClickPicture.apply {
            this.adapter = adapter
            setSelection(1)
        }
    }

    private fun showSpinner(frameDesc: List<Resolution>, selectedFrameDesc: Resolution) {
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item,
            frameDesc.map { "W:${it.width} x H:${it.height} FPS:${it.frameRate}" })
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

    private fun showBitmapDialog(bitmap: Bitmap, resolution: Resolution) {
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_picture, null)
        with(dialogView) {
            findViewById<ImageView>(R.id.imageView).apply {
                setImageBitmap(bitmap)
            }
            findViewById<TextView>(R.id.imageDetails).apply {
                text = "Image Details w: ${resolution.width} h:${resolution.height}"
            }
            findViewById<TextView>(R.id.openInIntent).apply {
                setOnClickListener {
                    openImageWithIntent(bitmap)
                }
            }
        }
        dialogBuilder.setView(dialogView)
        val dialog: Dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun openImageWithIntent(bitmap: Bitmap) {
        try {
            // Save the Bitmap as a temporary image file
            val tempFile = createTempImageFile(bitmap)
            val imageUri = FileProvider.getUriForFile(
                this,
                "pl.nextcamera.example",
                // Replace with your package name
                tempFile
            )
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(imageUri, "*/*")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Timber.e("Not resolving any activity $tempFile")
            }
        } catch (e: IOException) {
            Timber.e(e)
        }
    }

    @Throws(IOException::class)
    private fun createTempImageFile(bitmap: Bitmap): File {
        val fileName = "temp_image.jpg" // You can change the file name and format as needed
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File(storageDir, fileName)
        val outputStream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()
        return imageFile
    }
}

sealed class UiState {
    object ArxHeadsetConnected : UiState()
    object PermissionNotGiven : UiState()
    object DeviceDisconnected : UiState()
    data class ImuDataUpdate(val imuData: ImuData) : UiState()
    data class DeviceError(val message: String, val throwable: Throwable) : UiState()
}