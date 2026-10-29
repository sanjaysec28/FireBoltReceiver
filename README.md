# FireBoltReceiver

An Android prototype for receiving health-related readings from a
Fire-Boltt smartwatch over Bluetooth Low Energy (BLE). The project
explores reading heart-rate and SpO₂ data on an Android phone, with the
goal of making smartwatch readings available to the mobile app.

> **Project status:** Work in progress. BLE behavior can vary by watch
> firmware and Android version. Verify each reading against the actual
> device before relying on it.

## Workflow

1.  **Enable Bluetooth** on the Android phone and keep the Fire-Boltt
    watch nearby.
2.  **Request Bluetooth permissions.** Android 12 and newer require
    runtime permissions such as `BLUETOOTH_SCAN` and
    `BLUETOOTH_CONNECT`. Older Android versions may require location
    permission for BLE scanning.
3.  **Scan for the watch.** The prototype has been developed around the
    device name `FireBoltt 128`; the name may differ depending on the
    watch or firmware.
4.  **Connect to the watch** using Bluetooth Low Energy (BLE).
5.  **Discover GATT services and characteristics** exposed by the
    device.
6.  **Subscribe to notifications** for supported characteristics so new
    readings can arrive without repeatedly polling the watch.
7.  **Decode incoming values** and display the readings in the Android
    app. The project has investigated the standard Heart Rate Service
    and a device-specific SpO₂ characteristic.
8.  **Handle disconnects and permission changes** so the app can report
    connection state and reconnect when appropriate.

### BLE characteristics investigated

  --------------------------------------------------------------------------------------
  Reading           Service UUID      Characteristic    Notes
                                      UUID              
  ----------------- ----------------- ----------------- --------------------------------
  Heart rate        `0x180D`          `0x2A37`          Standard Bluetooth SIG Heart
                                                        Rate Service and Measurement
                                                        characteristic

  SpO₂              `0xFEEA`          `0xFEE3`          Device-specific/vendor-related
                                                        UUIDs observed during
                                                        development; support and payload
                                                        format should be verified on the
                                                        target watch
  --------------------------------------------------------------------------------------

The watch may require a measurement to be started in its companion app
or on the watch itself. Available data and notification behavior can
depend on the model and firmware.

## Technology

-   **Language:** Kotlin
-   **Platform:** Android
-   **IDE:** Android Studio
-   **Connectivity:** Bluetooth Low Energy (BLE / GATT)

## Requirements

-   Android Studio and a compatible Android SDK
-   A physical Android phone with Bluetooth Low Energy support
-   A compatible Fire-Boltt smartwatch
-   Bluetooth enabled on both devices
-   Runtime Bluetooth permissions granted to the app

A physical phone and watch are recommended; an emulator generally cannot
reproduce the full BLE workflow with a real watch.

## Run the project

1.  Clone the repository:

    ``` bash
    git clone https://github.com/sanjaysec28/FireBoltReceiver.git
    cd FireBoltReceiver
    ```

2.  Open the project directory in Android Studio.

3.  Allow Android Studio to sync Gradle and install any required SDK
    components.

4.  Connect an Android phone with USB debugging enabled, or use a
    compatible device deployment method.

5.  Build and run the app.

6.  Grant the Bluetooth permissions requested by the app.

7.  Turn on the watch, keep it nearby, and follow the app's scan/connect
    flow.

> If the Gradle sync or build fails, check the Android Studio error
> output first. The exact SDK and Gradle requirements are determined by
> the versions configured in this repository.

## Permissions

The exact permissions depend on the Android versions targeted by the
app. Typical BLE permissions include:

-   Android 12 (API 31) and newer: `BLUETOOTH_SCAN` and
    `BLUETOOTH_CONNECT` at runtime.
-   Older Android versions: Bluetooth permissions and, on some versions,
    location permission for BLE scanning.

Only request the permissions the app actually needs, explain their
purpose to the user, and handle denied permissions gracefully.

## Current scope and next steps

This repository is a prototype focused on Bluetooth communication and
receiving smartwatch readings. Useful next steps include:

-   Complete and test the scan, connect, and disconnect states.
-   Validate heart-rate and SpO₂ payload parsing against real watch
    notifications.
-   Add clear connection and permission error messages.
-   Improve the UI for displaying readings and their timestamps.
-   Add reliable local persistence if it is not already implemented.
-   Test reconnection behavior and different supported Android versions.

## Important note

This project is for learning and prototyping only. Smartwatch readings
may be delayed, unavailable, or inaccurate. **Do not use this app for
diagnosis, emergency decisions, or medical treatment.**

## Repository

https://github.com/sanjaysec28/FireBoltReceiver
