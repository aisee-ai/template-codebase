# Aisee Template Codebase

Welcome to the **Aisee Template**, a scaffolding project designed to help you quickly build applications for the **AISEE Android Headset**.

**Important:** The AISEE headset runs a customized version of Android. It is designed to be a "headless" experience for the end-user. The Visual UIs (Activities, Overlays, Previews) discussed in this guide are intended **strictly for development and debugging purposes**.

This guide will walk you through the architecture, explain why we use specific components, and show you exactly where to add your own code.

---

## 1. Project Overview
This project is split into two main modules:
*   **`:app`**: The main application code (Accessibility Service, Camera, Logic).
*   **`:vad_module`**: A specialized module for Voice Activity Detection (VAD). *We will cover this briefly later as a trigger mechanism.*

---

## 2. Why Accessibility Service?
Unlike traditional Android apps that rely on **Activities** (UI screens), the AISEE headset is used without looking at a screen. Therefore, there is no point in developing complex UI flows in Activities for the final product.

Instead, we use an **Android Accessibility Service** (`MyAccessibilityService.kt`) as the core of the application. This allows the app to:
1.  Run in the background constantly.
2.  Intercept global hardware button presses (Volume, F2, etc.) regardless of the system state.

---

## 3. Getting Started: The Main Activity
You will notice there is a `MainActivity.kt`. Since this is a headset, why is it there?

On a standard Android device, enabling an Accessibility Service requires navigating through deep Settings menus. On AISEE that means using a tool like Vysor or android studio to use the UI and toggle the setting on.

In `MainActivity.kt`, we use a utility called `ShellOperator` to run root commands (`settings put secure ...`) that **automatically enable the service** when you run the app from Android Studio. This ensures your code is live immediately after a build without manual intervention.

---

## 4. How to Trigger Actions
The device interacts with the world through **Physical Buttons**, **Voice**, and **LEDs**.

### Physical Buttons
*   **File:** `MyAccessibilityService.kt`
*   **Function:** `onKeyEvent`

This function listens for hardware key presses. Currently, it is set up to detect **F2** (`KeyEvent.KEYCODE_F2`).
```kotlin
if (event.keyCode == KeyEvent.KEYCODE_F2) {
    Log.d(TAG, "F2 Pressed: Taking Photo...")
    // Add your custom logic here!
    photoHandler?.takePhoto() 
}
```

### Voice Commands
*   **File:** `MyAccessibilityService.kt`
*   **Function:** `setupVoiceActivation`

We use the `:vad_module` to listen for wake words (like "Hey I See") via the device's internal UART connection. When the wake word command (`CMD_HEY_I_SEE`) is detected, you can trigger actions just like a button press.

### LED Control
*   **File:** `utils/LEDUtils.java`
*   **Purpose:** Provides simple static methods to control the device's LEDs.

To use it, call `LEDUtils.setled()` with one of the provided static color variables:
*   `LEDUtils.FRONT`
*   `LEDUtils.BACK_RED`
*   `LEDUtils.BACK_BLUE`

**Example (Turn on the FRONT LED):
```java
// To turn the FRONT LED on
LEDUtils.setled(LEDUtils.FRONT, true);

// To turn it off
LEDUtils.setled(LEDUtils.FRONT, false);
```

There is also an overloaded method to make the LEDs blink:
```java
// Blink the BACK LED every 500ms on, 500ms off
LEDUtils.setled(LEDUtils.FRONT, 500, 500, true);
```

---

## 5. The Camera System
We have modularized the CameraX code into the `camera/` package to make it easy to understand. 

### A. Image Capture (Photos)
*   **File:** `camera/PhotoHandler.kt`
*   **Purpose:** Takes high-quality, high-resolution JPEGs.
*   **Usage:** Call `photoHandler.takePhoto()`. It automatically saves the image to the public Gallery.

### B. Image Analysis (ML Stream)
*   **File:** `camera/AnalysisHandler.kt`
*   **Purpose:** Streams a continuous, lower-resolution (VGA) video feed to a Machine Learning processor.
*   **Why VGA?** We intentionally use 640x480 for this stream to keep the processing fast and lightweight, ensuring it doesn't slow down the high-quality photo capture.

### C. The Core Engine
*   **File:** `camera/CameraCore.kt`
*   **Purpose:** Manages the camera hardware lifecycle. It binds the Photo and Analysis streams together so they can run simultaneously without colliding.

---

## 6. Machine Learning & Debug UI
The project comes with Google ML Kit Object Detection pre-integrated.

*   **File:** `ml/MlKitProcessor.kt`
*   **Logic:** This class receives frames from the `AnalysisHandler`, runs an Object Detector, and draws bounding boxes on a custom **Overlay** (`OverlayView`).

**Debugging vs. Production**
*   **Development:** In `MyAccessibilityService.kt`, uncomment `setupMlKit()` to enable the **Camera Preview and Bounding Box Overlay**. This allows you to "see what the AI sees" on your monitor or debug screen.
*   **Production:** When deploying for the user, you should **remove the UI components** (the Preview and Overlay) but keep the ML processing logic. Instead of drawing boxes, you would typically use Text-to-Speech (TTS) to describe the detected objects to the user.

---

## Summary
1.  **Build & Run**: The app auto-enables itself via `ShellOperator`.
2.  **Press F2**: `PhotoHandler` takes a high-res photo.
3.  **Uncomment `setupMlKit`**: The app starts detecting objects and showing the **debug video feed**.
4.  **Edit `MyAccessibilityService`**: This is your playground to connect buttons and voice commands to new features.
