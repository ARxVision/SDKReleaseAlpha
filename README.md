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
      //  Handle permission denied for the device here
   }

   // Handle device disconnection
   override fun onDisconnect(){
      // when headset is disconnected.
   }

   // Handle received still photo from the device
   override fun onStillPhotoReceived(bitmap: Bitmap, currentFrameDesc: Resolution) = Unit

   // Handle camera resolution update
   override fun onCameraResolutionUpdate(frameDesc: List<Resolution>, selectedFrameDesc: Resolution) =Unit

   // provides IMU data coming from headser.
   override void onImuDataUpdate(@NotNull ImuData imuData) {
   })

```

3. To change the camera resolution of the Arx Headset, you can use the `changeResolution()` method of the `ArxHeadsetHandler`.

```kotlin
// Change the camera resolution to the selected resolution
arxHeadsetHandler.changeResolution(selectedResolution)
```

4. Ensure to handle the `onPermissionDenied()` and `onDisconnect()` callback appropriately, and launch Arx UI again if needed.

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

7. Disconnect all other app using the ARX SDK. This will restore the connection if the other apps using the SDK will release the USB connection. If this doesn't resolve unfortunatily you have to manually exit the app using USB resource.

```kotlin
fun notifyToDisconnectApp()
```

This method allows you to capture a photo using the Arx Headset with the specified camera resolution. It throws an `IllegalStateException` if the USB foreground service is not connected.

By following these steps, you can integrate the Arx SDK library into your Android application and utilize its functionalities.

```kotlin
// Example usage
val selectedResolution = Resolution._1920x1080
arxHeadsetHandler.clickPhoto(selectedResolution)
```
7. Make sure you handle all the use case define in `Use Cases for Providing the Relevant App and USB Device Permissions (Using Arx UI Library)` section

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
To tailor the visual appearance of the Arx Headset UI elements within your application, you can customize the provided XML layout files. This lets you achieve a look and feel that aligns with your app's overall design.

**Available XML Files**

-   **`activity_arx.xml`:** The primary layout file for the Arx SDK's UI components. Modify this for broader changes to the UI structure.

-   **Permission-Related Files:**
-   `item_permission.xml` and `item_no_permission.xml`: Control the appearance of individual permission items (like camera or audio) displayed as granted or not granted.
-   `fragment_permission.xml`: Represents the section of the UI where app permissions are managed.
-   **USB Device Permission -Related Files:**
-   `fragment_usb_device.xml`: Represents the section of the UI where USB device permissions are managed.
-   `item_usb_devices_without_permission.xml` and `item_usb_devices_with_permission.xml`: Control the appearance of USB device items in the UI based on their permission status.

**Important Notes**

-   **Maintain Structure:** When making changes, ensure you preserve the general layout hierarchy and naming used in the default files. This helps the Arx SDK correctly interpret your customizations.

**Example**

Suppose you want to change the background color and text style of the permission request screen. You could:

1.  Create a copy of `fragment_permission.xml` in your own project's resource directory.
2.  Modify the background color attribute of the root layout element.
3.  Adjust text colors and styles as desired.
    Absolutely! Here's how to incorporate that information about strings into your Readme for clarity and to highlight the flexibility of the Arx SDK:

### Customizing Text and Supporting Internationalization
The Arx Headset UI offers additional customization and localization options by allowing you to override or translate its text strings. This is essential for providing a tailored and accessible user experience in different languages.

**Customizable Strings**

The following strings, defined in the Arx SDK's resources, can be modified:

-   **`connect_headset` and `connect_headset_help`:** Instructions related to connecting the ArxVision GO headset.
-   **`usb_helptext`:** Detailed guidance for the user during the USB permission process.
-   **`usb_product_details`:** Technical information about the connected USB device (use this cautiously, as it might not be user-friendly).
-   **`connectivity`:** Likely a section heading relating to connection status.
-   **`permission_granted`, `permission_not_granted`, `button_request_permission`:** Feedback messages about permissions.
-   **`quit_app`:** Button label to exit the application flow.
-   **`unsupported_device`:** Message displayed when the headset is incompatible.
-   **`prev_setting`:** Explanation when a permission has been permanently denied.
-   **`title_usb_permission`:** Title for the USB Permission section.

**How to Override**

1.  Create a `strings.xml` file in your own project's `res/values` directory.
2.  Define string resources with the same names as those listed above.
3.  Provide your desired text content or translations within these string resources.

**Example (French Translation)**
XML
```
<resources>
   <string name="connect_headset">Connectez le ARxVision GO</string>
   <string name="permission_granted">Permission accordée</string>
 </resources>
```
**Important:** Ensure you create language-specific resource directories (e.g., `res/values-fr` for French) to provide translations for different locales.

### Use Cases for Providing the Relevant App and USB Device Permissions (Using Arx UI Library)

**Note:** These use cases apply specifically when you utilize the Arx UI Library (`arxui-release.aar`) to simplify permission handling.

### 1. App Open & User Initiates Connection
-   **Scenario:** The user is actively using your app and triggers an action that requires the Arx Headset (for example, pressing a "Connect Headset" button).

-   **Process:**
1.  Your app initializes the Arx SDK.
2.  Your app launches the Arx SDK's permission UI activity.
3.  The Arx UI Library clearly presents the necessary permission requests (camera, audio, etc.) and USB permission requests.
4.  The user grants or denies permissions.
5.  Your app receives a callback indicating the result. If permissions are granted, you can proceed to utilize the headset's features.

### 2. App in Background & Headset Connects
-   **Scenario:** Your app is running in the background, and the user connects the Arx Headset.
-   **Process:**
1.  You've defined a specific intent filter in your app to recognize the Arx Headset connection event.
2.  The headset connection triggers this intent filter.
3.  The Arx UI Library automatically launches and displays permission requests if needed.
4.  Your app receives relevant updates (usually through the `onNewIntent` method), allowing you to react and start using headset features.

### 3. App Closed & Headset Connects

-   **Scenario:** Your app is not running, and the user connects the Arx Headset.
-   **Process:**
1.  You've defined an intent filter to recognize the Arx Headset connection event.
2.  The headset connection triggers this intent filter.
3.  The Arx UI Library intelligently determines the activity within your app that should handle the headset connection.
4.  The determined activity is launched, and the Arx UI Library facilitates the permission process if needed.

### Handling "Device in Use" Connection Errors**
If another application is already using the Arx Headset's USB devices, the Arx SDK will notify you through the `ArxHeadsetHandler#onDeviceConnectionError(throwable: Throwable)` callback. The `throwable` in this case will be a `UVCException`.

**What it Means:** A `UVCException` during connection usually indicates that the headset is unavailable due to another application having control of its USB resources.

**Recommended Action: `notifyToDisconnectApp()`**

1.  **Implement the Error Callback:**
    ```Kotlin
    override fun onDeviceConnectionError(throwable: Throwable) {
        if (throwable is UVCException) {
            arxHeadsetHandler.notifyToDisconnectApp() // Attempt to resolve
        } else {
            // Handle other connection errors differently 
        }
    }
    
    ```

2.  **Signal Other Apps:** If you receive the `UVCException`, call `arxHeadsetHandler.notifyToDisconnectApp()`. This signals other apps using Arx SDK (v0.5 or above) to release their connection to the headset.

**Important:**
-   Older Arx SDK versions or non-SDK applications may not respond to this signal.
-   In some cases, the user might need to manually close the conflicting application.

**For any questions or issues, please reach out to the Arx Headset support team.**