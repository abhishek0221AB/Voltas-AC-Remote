# Voltas AC Remote

A custom infrared AC remote for selected Voltas air conditioners, built as an Android app with a responsive web-based user interface.

The Android app uses the phone's built-in IR blaster through `ConsumerIrManager`, while the interface is written in HTML, CSS, and JavaScript and displayed inside an Android `WebView`.

The project was developed and tested using an iQOO/vivo Android phone with a built-in IR emitter.

---

## Features

Current validated controls:

- Power ON / OFF
- Temperature control from 16°C to 30°C
- Modes:
  - Cool
  - Dry
  - Fan
- Fan speeds:
  - Auto
  - Low
  - Medium
  - High
- Lamp / AC display ON / OFF
- Vertical swing ON / OFF
- Turbo ON / OFF
- Timer OFF
- Timer ON
- Separate Timer ON / Timer OFF cancellation
- Responsive no-scroll mobile UI
- Light / dark theme
- State-aware button disabling

### State-aware UI

When the AC is **ON**:

- Power is enabled
- Temperature controls are enabled
- Mode / Fan / Swing / Lamp / Turbo are enabled
- Timer OFF is enabled
- Timer ON is disabled

When the AC is **OFF**:

- Power is enabled
- Timer ON is enabled
- Temperature / Mode / Fan / Swing / Lamp / Turbo are disabled
- Timer OFF is disabled

The power button is:

- Green when the AC is ON
- Red when the AC is OFF

---

## Tested Voltas Models

The project was developed around the following Voltas AC models:

- Voltas 183V CZQ
- Voltas 245V ADZ

IR behavior was validated physically on the AC using the phone's IR emitter.

---

## Project Architecture

```text
Web UI
HTML + CSS + JavaScript
        ↓
Android WebView
        ↓
JavaScript Interface
window.AndroidIR.sendState(...)
        ↓
IRBridge.kt
        ↓
MainActivity.kt
        ↓
VoltasEncoder.kt
        ↓
Android ConsumerIrManager
        ↓
Phone IR Blaster
        ↓
Voltas AC
```

---

## Project Structure

```text
voltas-ac-remote/
│
├── app/
│   ├── build.gradle.kts
│   │
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           │
│           ├── java/
│           │   └── com/example/voltasirtest/
│           │       ├── MainActivity.kt
│           │       ├── IRBridge.kt
│           │       └── VoltasEncoder.kt
│           │
│           ├── res/
│           │   ├── layout/
│           │   │   └── activity_main.xml
│           │   └── values/
│           │       └── styles.xml
│           │
│           └── assets/
│               └── web/
│                   ├── index.html
│                   ├── styles.css
│                   └── app.js
│
├── docs/
│   ├── index.html
│   ├── styles.css
│   └── app.js
│
├── gradle/
│   └── wrapper/
│
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── .gitignore
└── README.md
```

---

## Technologies Used

### Android

- Kotlin
- Android Studio
- Android WebView
- `ConsumerIrManager`
- JavaScript interface via `addJavascriptInterface`

### Web UI

- HTML
- CSS
- JavaScript
- Responsive mobile layout

### Deployment

- Git
- GitHub
- GitHub Pages

---

## Requirements

To transmit IR commands, the Android phone must have:

- Android
- A built-in IR blaster / consumer IR emitter
- Support for approximately 38 kHz IR transmission

The project was tested on an iQOO/vivo Android device with a working built-in IR emitter.

A normal browser on a laptop or phone can display the web interface, but it **cannot transmit IR by itself**.

IR transmission requires the Android app because the Android app provides the native IR bridge.

---

## Running the Android App

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/voltas-ac-remote.git
cd voltas-ac-remote
```

### 2. Open in Android Studio

Open the project root folder:

```text
voltas-ac-remote
```

Do not open only the `app` folder.

### 3. Sync Gradle

In Android Studio:

```text
File
→ Sync Project with Gradle Files
```

### 4. Build

```text
Build
→ Make Project
```

Wait for:

```text
BUILD SUCCESSFUL
```

### 5. Connect the Android phone

Enable Developer Options and USB Debugging on the phone.

Connect the phone through USB and select it in Android Studio.

### 6. Run

Press:

```text
▶ Run
```

The app should open the Voltas remote interface.

---

## Local Web UI

During development, the Android app can load the bundled interface from:

```text
app/src/main/assets/web/
```

Typical local WebView URL:

```text
file:///android_asset/web/index.html
```

This is useful for testing without internet access.

---

## GitHub Pages

The final web UI can be placed inside:

```text
docs/
```

and published through GitHub Pages.

Recommended GitHub Pages configuration:

```text
Repository
→ Settings
→ Pages
→ Deploy from a branch
→ Branch: main
→ Folder: /docs
```

The deployed site will look similar to:

```text
https://YOUR_USERNAME.github.io/voltas-ac-remote/
```

The GitHub Pages version provides the interface only.

To control the AC through IR, open that site inside the Android app WebView where the native `AndroidIR` bridge is available.

---

## IR Protocol Notes

The current implementation uses:

- Carrier frequency: 38 kHz
- 10-byte / 80-bit state
- MSB-first transmission
- State checksum
- Full-state AC commands rather than simple one-button codes

Validated state examples include:

```text
Power ON
33 E8 80 18 3B 3B 3B 11 00 8A

Power OFF
33 E8 00 18 3B 3B 3B 11 00 0A

Lamp OFF
33 E8 80 18 3B 3B 3B 11 20 6A

Lamp ON
33 E8 80 18 3B 3B 3B 11 00 8A

V-Swing ON
33 E8 87 18 3B 3B 3B 11 00 83

V-Swing OFF
33 E8 80 18 3B 3B 3B 11 00 8A

Turbo ON
33 E8 A0 18 3B 3B 3B 11 00 6A

Turbo OFF
33 E8 80 18 3B 3B 3B 11 00 8A
```

Timer commands preserve encoded timer-hour values while separate enable bits control whether Timer ON or Timer OFF is active.

---

## Current Limitations

- Horizontal swing is not supported because the tested AC uses manual horizontal adjustment.
- Heat mode is not supported on the tested cool-only AC.
- Sleep and Saver modes were intentionally removed from the final UI.
- Browser-only access does not provide IR control.
- IR mappings are validated for the tested Voltas models and may not work with every Voltas AC.

---

## Safety / Compatibility

This project only sends infrared remote-control commands.

It does not modify the AC's electrical wiring, firmware, or internal hardware.

Different AC models may use different IR state formats, so protocol values should be tested before using this encoder with another model.

---

## Development Status

Current status:

- IR protocol decoding: complete
- Android IR transmission: working
- WebView JavaScript bridge: working
- Responsive mobile UI: working
- No-scroll mobile layout: working
- Timer ON / OFF: working
- GitHub Pages deployment: in progress

---

## Author

M. Abhishek

Computer Science and Engineering

---

## License

This project is intended for educational and personal-use development.

You may add a formal open-source license such as MIT later if you want to distribute or accept contributions.
