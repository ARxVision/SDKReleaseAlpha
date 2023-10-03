# Arx Headset SDK Documentation

The Arx Headset SDK allows developers to integrate Arx Headset functionality into their Android applications. This document provides an overview of the Arx Headset SDK and outlines the required permissions for accessing different functionalities of the headset.

## USB Devices Overview

The Arx Headset consists of three USB devices, each serving a specific purpose. Here's an overview of the USB devices and their functionalities:

| USB Device        | Product Name     | Functionality                          | Required Permissions       |
|-------------------|------------------|----------------------------------------|----------------------------|
| USB Device 1      | USB audio CODEC  | Audio Recording                        | RECORD_AUDIO               |
| USB Device 2      | ARxCamera        | Video Recording                        | CAMERA                     |
| USB Device 3      | STM32 Composite  | Button Press Detection                 | No specific permissions    |

## Requesting Permissions

When utilizing the Arx Headset SDK, it is essential to request the appropriate usb permissions to access the desired USB devices. Here's a summary of the required permissions for each USB device:

1. USB Device 1 (USB audio CODEC - Audio Recording): `RECORD_AUDIO` permission is required.
2. USB Device 2 (ARxCamera - Video Recording): `CAMERA` permission is required.
3. USB Device 3 (STM32 Composite - Button Press Detection): No specific Android permissions are required.


# Arx SDK Documentation - Readme

To use the Arx SDK library in your Android project, follow the steps below:

## Arx SDK Components

The Arx Headset SDK is divided into two AAR libraries:

1. `arxcameraapi-release.aar` - Arx Camera API Library
    - This core library enables your Android application to connect and interact with the Arx Headset.
    - It exposes an API to manage camera resolutions, receive device photos, and handle button presses.

2. `arxui-release.aar` - Arx UI Library
    - This library handles USB permission and app permission requests through a user interface (UI).
    - It provides a user-friendly way to request the necessary permissions to access the Arx Headset functionalities.
    - The Arx UI Library simplifies the process of handling permissions, device connection, and disconnection events.

## Importing the Arx SDK AAR

1. Download the `arxui-release.aar` and `arxcameraapi-release.aar` files from the attached releases.

2. Create a `libs` directory in your project's module if it doesn't exist.

3. Copy the downloaded `arxui-release.aar` and `arxcameraapi-release.aar` files into the `libs` directory.

4. Open your project's `build.gradle` file and add the following code inside the `dependencies` block:

```groovy
implementation files('libs/arxui-release.aar')
implementation files('libs/arxcameraapi-release.aar')
```

5. Sync your project to ensure the Arx SDK library is properly added.

## Dependencies

The Arx SDK library also has additional dependencies that need to be added to your project's `build.gradle` file. Ensure that you have the following dependencies included:

```groovy
implementation 'com.google.guava:guava:29.0-android'
implementation "com.jakewharton.timber:timber:5.0.1"

def coroutines_version = '1.6.4'
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version"
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version"
```

Make sure to sync your project after adding these dependencies.

## Usage

# Arx SDK Documentation - Readme

### Request Permissions Using Arx UI

1. In your application's activity or fragment, create an instance of `ArxPermissionActivityResultContract`. This step is essential to handle permissions requested through Arx UI.

```kotlin
private val myActivityResultContract = ArxPermissionActivityResultContract()
```

2. Register for activity result using the `ArxPermissionActivityResultContract` and handle the results accordingly.

```kotlin
private val myActivityResultLauncher = registerForActivityResult(myActivityResultContract) { result ->
    when (result) {
        ArxPermissionActivityResult.AllPermissionsGranted -> {
            // All required permissions are granted. Start Arx Headset service or perform actions.
        }
        ArxPermissionActivityResult.BackPressed -> {
            // Handle the back button press from Arx UI. You may choose to do nothing or handle it accordingly.
        }
        ArxPermissionActivityResult.CloseAppRequested -> {
            // The user requested to close the app when permission is denied. You can handle it based on your app's requirements.
            finish()
        }
    }
}
```

3. In your app's activity, when you need to request permissions to access the Arx Headset functionalities, use the `myActivityResultLauncher.launch(true)` method to launch Arx UI.

```kotlin
// Example: Launch Arx UI to request permissions
myActivityResultLauncher.launch(true)
```
###  Use Arx Camera API

1. Once you have the necessary permissions granted through Arx UI, you can use the Arx Camera API to interact with the Arx Headset.

2. Create an instance of `ArxHeadsetHandler` by passing the activity and an implementation of `ArxHeadsetApi` interface.

```kotlin
val arxHeadsetHandler = ArxHeadsetHandler(this, object : ArxHeadsetApi {
      // Handle device connection error
      // UVCException (Related to connection of usb device)
      // StillImageException (Error occurred during the requesting still frames)
      override fun onDeviceConnectionError(throwable: Throwable) = Unit
   
      // Handle received device photo
      override fun onDevicePhotoReceived(bitmap: Bitmap, frameDescriptor: Resolution) = Unit
   
      // Handle button click on Arx headset
      override fun onButtonClicked(arxButton: ArxHeadsetButton, isLongPress: Boolean) = Unit
   
      // Handle permission denied for the device
      override fun onPermissionDenied() {
         // TODO: Handle permission denied for the device here
      }
   
      // Handle device disconnection
      override fun onDisconnect() = Unit
   
      // Handle received still photo from the device
      override fun onStillPhotoReceived(bitmap: Bitmap, currentFrameDesc: Resolution) = Unit
   
      // Handle camera resolution update
      override fun onCameraResolutionUpdate(frameDesc: List<Resolution>, selectedFrameDesc: Resolution) =Unit
})

```

3. To change the camera resolution of the Arx Headset, you can use the `changeResolution()` method of the `ArxHeadsetHandler`.

```kotlin
// Change the camera resolution to the selected resolution
arxHeadsetHandler.changeResolution(selectedResolution)
```

4. Ensure to handle the `onPermissionDenied()` callback appropriately, and launch Arx UI again if needed.

```kotlin
override fun onPermissionDenied() {
    // Launch Arx UI again to request permissions, if necessary.
    myActivityResultLauncher.launch(true)
}
```

5. Start the Arx Headset Service with Resolution

```kotlin
fun startHeadSetService(resolution: Resolution)
```

This method allows you to start the Arx Headset service with a specific camera resolution. You can choose from various resolutions available in the `Resolution` enum.

```kotlin
// Example usage
val selectedResolution = Resolution._1920x1080
arxHeadsetHandler.startHeadSetService(selectedResolution)
```
6. Capture a Photo with Arx Headset

```kotlin
@Throws(IllegalStateException::class)
fun clickPhoto(frameDesc: Resolution)
```

This method allows you to capture a photo using the Arx Headset with the specified camera resolution. It throws an `IllegalStateException` if the USB foreground service is not connected.

By following these steps, you can integrate the Arx SDK library into your Android application and utilize its functionalities.

```kotlin
// Example usage
val selectedResolution = Resolution._1920x1080
arxHeadsetHandler.clickPhoto(selectedResolution)
```

### Resolution Enum

The `Resolution` enum provides a list of supported camera resolutions with their width, height, and frame rate values. You can use these resolutions when starting the Arx Headset service.

```kotlin
enum class Resolution(val width: Int, val height: Int, val frameRate: Int = 30) {
    _640x480(640, 480),
    _800x600(800, 600),
    _1280x720(1280, 720),
    _1280x1024(1280, 1024),
    _1920x1080(1920, 1080),
    _2048x1536(2048, 1536, 20),
    _2592x1944(2592, 1944, 20),
    _3264x2448(3264, 2448, 15)
}
```
### Customizing UI Look and Feel

To customize the user interface (UI) look and feel of your Arx Headset integrated application, you can use the provided XML layout files. These files allow you to override the default UI styles and create a more personalized user experience.

The following XML layout files are available for customization:

- `item_usb_devices.xml`: This layout defines the appearance of individual USB devices in the Arx Headset UI, such as their icons and labels.

- `item_permission.xml`: Use this layout to customize the UI elements related to permission requests. You can adjust the layout, colors, and text to match your app's design.

- `layout_device_connected.xml`: Customize the layout for the UI when the Arx Headset is connected. You can modify the arrangement and appearance of elements like buttons and status indicators.

- `layout_device_disconnected.xml`: This layout file allows you to define the UI when the Arx Headset is disconnected. Customize the appearance of disconnected device UI components as needed.

Please ensure that when customizing these XML layout files, you maintain the layout hierarchy and naming conventions similar to the original files. While the current customization options are limited, future updates may provide more extensive customization capabilities.

By using these XML layout files, you can tailor the Arx Headset UI to align with your app's branding and user interface preferences.

For any questions or issues, please reach out to the Arx Headset support team.
