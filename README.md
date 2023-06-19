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

Please ensure that the necessary permissions are included in your Android application manifest and are requested from the user at runtime, as per the Android permission model.

## Automatic USB Device Permission and App Launch

To remember the USB device permission and automatically launch your app when the headset is connected, you can define the following intent filter in your XML folder:

Create a new XML file (e.g., `device_filter.xml`) with the following content:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <usb-device
        product-id="10514"
        vendor-id="2235" />
    <usb-device
        product-id="22337"
        vendor-id="1155" />
    <usb-device
        product-id="1"
        vendor-id="1155" />
</resources>
```

Add the following intent filter to your activity in the app's manifest file:

```xml
<intent-filter>
    <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
</intent-filter>

<meta-data
    android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED"
    android:resource="@xml/device_filter" />
```

With this configuration, your app will remember the USB device permission and automatically launch when the corresponding headset is connected.

Please note that when the permission dialog is launched for the first time in the app, the user needs to check the "Always open" checkbox to ensure that the app is opened automatically when the headset is connected.

## Conclusion

The Arx Headset SDK provides seamless integration of Arx Headset functionality into Android applications. By following the guidelines outlined in this documentation, developers can effectively utilize the different USB devices of the Arx Headset while adhering to the required permissions for each functionality.

For further details and implementation specifics, refer to the Arx Headset SDK documentation and sample code provided by the Arx Headset manufacturer.
