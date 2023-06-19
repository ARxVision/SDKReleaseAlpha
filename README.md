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

When utilizing the Arx Headset SDK, it is essential to request the appropriate permissions to access the desired USB devices. Here's a summary of the required permissions for each USB device:

1. USB Device 1 (USB audio CODEC - Audio Recording): `RECORD_AUDIO` permission is required.
2. USB Device 2 (ARxCamera - Video Recording): `CAMERA` permission is required.
3. USB Device 3 (STM32 Composite - Button Press Detection): No specific Android permissions are required.


# Arx SDK Documentation - Readme

To use the Arx SDK library in your Android project, follow the steps below:

## Importing the Arx SDK AAR

1. Download the Arx SDK AAR file from the attached release.

2. Create a `libs` directory in your project's module if it doesn't exist.

3. Copy the downloaded Arx SDK AAR file into the `libs` directory.

4. Open your project's `build.gradle` file and add the following code inside the `dependencies` block:

```groovy
implementation files('libs/arx-sdk.aar')
```

Make sure to replace `'arx-sdk.aar'` with the actual filename of the Arx SDK AAR file you copied.

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

## Usage

To use the Arx SDK library in your Android project, follow the steps below:

1. Import the Arx SDK library into your project by following the instructions mentioned in the previous section titled "Importing the Arx SDK AAR."

2. Ensure that you have added the required dependencies to your project's `build.gradle` file as shown below:

```groovy
implementation 'com.google.guava:guava:29.0-android'
implementation "com.jakewharton.timber:timber:5.0.1"

def coroutines_version = '1.6.4'
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version"
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version"
```

Make sure to sync your project after adding these dependencies.

3. Once the Arx SDK library and dependencies are successfully imported, you can start using the ArxHeadsetHandler class to manage the Arx Headset functionality in your application.

```kotlin
// Create an instance of ArxHeadsetHandler by passing the activity and ArxHeadsetApi
val arxHeadsetHandler = ArxHeadsetHandler(activity, arxHeadsetApi)

// Implement the ArxHeadsetApi interface to listen to various events and interactions
val arxHeadsetApi = object : ArxHeadsetApi {
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

    override fun onCameraResolutionUpdate(frameDescList: List<FrameDesc>, selectedFrameDesc: FrameDesc) {
        // Handle camera resolution update
    }

    override fun onPermissionDenied() {
        // Handle permission denial
    }
}
```

4. To change the resolution of the camera, you can use the `changeResolution()` method of the `ArxHeadsetHandler` class.

```kotlin
// Change the camera resolution to the selected resolution
arxHeadsetHandler.changeResolution(selectedResolution)
```

5. Make sure to handle the `onPermissionDenied()` callback appropriately. You can use the provided `ArxPermissionActivity` class to handle connectivity and listen to connections in case of permission denial.

```kotlin
override fun onPermissionDenied() {
    startActivity(Intent(this@MainActivity, ArxPermissionActivity::class.java))
}
```

By following these steps, you can integrate the Arx SDK library into your Android application and utilize its functionalities.

For any questions or issues, please reach out to the Arx Headset support team.
