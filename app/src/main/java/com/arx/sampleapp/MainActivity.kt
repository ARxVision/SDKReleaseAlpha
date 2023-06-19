package com.arx.sampleapp

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import com.arx.camera.ArxHeadsetApi
import com.arx.camera.ArxPermissionActivity
import com.arx.camera.HeadsetHandler
import com.arx.camera.headsetbutton.ArxHeadsetButton
import com.arx.camera.jni.FrameDesc

class MainActivity : AppCompatActivity() {
    private var handlerARx: HeadsetHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        handlerARx = HeadsetHandler(this, object : ArxHeadsetApi {
            override fun onDeviceConnectionError(p0: Throwable) {
                Toast.makeText(this@MainActivity, p0.toString(), Toast.LENGTH_SHORT).show()

            }

            override fun onDevicePhotoReceived(p0: Bitmap, p1: FrameDesc) {
                findViewById<ImageView>(R.id.imageButton).setImageBitmap(p0)
            }

            override fun onButtonClicked(p0: ArxHeadsetButton, p1: Boolean) {
                when (p0) {
                    ArxHeadsetButton.Square -> findViewById<Button>(R.id.buttonHeadsetSquare)
                    ArxHeadsetButton.Circle -> findViewById<Button>(R.id.buttonHeadsetCircle)
                    ArxHeadsetButton.Triangle -> findViewById<Button>(R.id.buttonHeadsetTriangle)
                    ArxHeadsetButton.VolumeUp -> findViewById<Button>(R.id.buttonHeadsetUp)
                    ArxHeadsetButton.VolumeDown -> findViewById<Button>(R.id.buttonHeadsetDown)
                }.apply {
                    clearAnimation()
                    if (p1) { //if is long press
                        startAnimation(
                            AnimationUtils.loadAnimation(
                                this@MainActivity,
                                R.anim.button_animation
                            )
                        )
                        this.performLongClick()
                    } else {
                        this.performClick()
                        startAnimation(
                            AnimationUtils.loadAnimation(
                                this@MainActivity,
                                R.anim.button_animation_long
                            )
                        )
                    }
                }
            }

            override fun onDisconnect() {
                startActivity(Intent(this@MainActivity, ArxPermissionActivity::class.java))
            }

            override fun onCameraResolutionUpdate(p0: MutableList<FrameDesc>, p1: FrameDesc) {
                showSpinner(p0, p1)
            }

            override fun onPermissionDenied() {
                startActivity(Intent(this@MainActivity, ArxPermissionActivity::class.java))

            }
        })
    }

    private fun showSpinner(frameDesc: List<FrameDesc>, selectedFrameDesc: FrameDesc) {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            frameDesc.map { "W:${it.width} x H:${it.height} FPS:${it.frameRate()}" }
        )
        // Specify the layout to use for the dropdown items
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        findViewById<Spinner>(R.id.resolutionSpinner).apply {
            onItemSelectedListener = null
            this.adapter = adapter
            setSelection(frameDesc.indexOf(selectedFrameDesc))
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedResolution = frameDesc[position]
                    handlerARx?.changeResolution(selectedResolution)
                    // Handle the selected resolution
                    Toast.makeText(
                        this@MainActivity,
                        "Selected Resolution: ${selectedResolution.width} x ${selectedResolution.height}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onNothingSelected(parent: AdapterView<*>) = Unit
            }
        }
    }


}