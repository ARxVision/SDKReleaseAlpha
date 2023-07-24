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
    // Implement the interface methods to handle different events and interactions with the Arx Headset.
    override fun onDeviceConnectionError(throwable: Throwable) {
        // Handle device connection error
    }

    override fun onDevicePhotoReceived(bitmap: Bitmap, frameDescriptor: FrameDesc) {
        // Handle received device photo
    }

    override fun onButtonClicked(arxButton: ArxHeadsetButton, isLongPress: Boolean) {
        // Handle button click
    }

    override fun onDisconnect() {
        // Handle headset disconnection
    }

    override fun onCameraResolutionUpdate(
        frameDescList: List<FrameDesc>,
        selectedFrameDesc: FrameDesc
    ) {
        // Handle camera resolution update
    }

    override fun onPermissionDenied() {
        // Handle permission denial
    }
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
By following these steps, you can integrate the Arx SDK library into your Android application and utilize its functionalities.

For any questions or issues, please reach out to the Arx Headset support team.
