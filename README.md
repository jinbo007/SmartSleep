# SmartSleep — Snore Detection System Requirements & Implementation (MVP/V2.2)

## Overview
SmartSleep is an Android app that monitors snoring using the device microphone. It runs a multi-feature detection pipeline entirely on-device, triggers vibration when a snore is confirmed, and shows a real-time amplitude graph and today’s cumulative snore count. No audio leaves the device; only statistics are stored.

## Goals & Use Cases
- Nap and nighttime snore monitoring and alerts
- Validate detection under different environments, distances, and intensities
- Provide intuitive feedback (graph + counter) to iterate detection parameters

## Key Features
- One-tap start/stop monitoring (main screen)
- Foreground service with persistent notification
- Snore detection + vibration alert (short intermittent pattern: 1s vibrate / 0.5s pause × 3)
- Today’s cumulative snore count with real-time updates
- Real-time amplitude graph with threshold line and time labels
- “Test Snore Detection” button for quick sensitivity validation
- Adaptive app icon suitable for stores and different device shapes

## Architecture
- Modules:
  - Audio capture: [AudioRecorder.kt](file:///Users/jinbo/Documents/code/SmartSleep/app/src/main/java/com/jinbo/smartsleep/audio/AudioRecorder.kt)
  - Audio utilities (RMS/ZCR/band energy): [AudioUtils.kt](file:///Users/jinbo/Documents/code/SmartSleep/app/src/main/java/com/jinbo/smartsleep/audio/AudioUtils.kt)
  - Detection service (foreground service, detection logic, vibration): [SnoreDetectionService.kt](file:///Users/jinbo/Documents/code/SmartSleep/app/src/main/java/com/jinbo/smartsleep/service/SnoreDetectionService.kt)
  - Data management (session stats): [SessionManager.kt](file:///Users/jinbo/Documents/code/SmartSleep/app/src/main/java/com/jinbo/smartsleep/data/SessionManager.kt)
  - UI (Compose, main/report/graph): [MainActivity.kt](file:///Users/jinbo/Documents/code/SmartSleep/app/src/main/java/com/jinbo/smartsleep/MainActivity.kt), [AmplitudeGraph.kt](file:///Users/jinbo/Documents/code/SmartSleep/app/src/main/java/com/jinbo/smartsleep/ui/AmplitudeGraph.kt)

## Detection Pipeline & Parameters (V2.2)
1. Capture
   - Sample rate: 16000 Hz (16 kHz)
   - Channels: Mono
   - Bit depth: PCM 16-bit
   - Buffer: max of `AudioRecord.getMinBufferSize()` and configured size (default 1024)
2. Features
   - RMS (intensity): `AudioUtils.calculateRMS(data)`
   - ZCR (Zero Crossing Rate): `AudioUtils.calculateZCR(data)` (distinguish low-frequency snore vs high-frequency noise)
   - Band energy (optional): `AudioUtils.calculateBandEnergy(data, sampleRate, 50, 800)` (pipeline primarily uses RMS+ZCR; band energy reserved for later model/policy)
3. Confirmation Conditions (must all pass)
   - Intensity: `RMS > 800.0`
   - Frequency characteristic: `ZCR < 0.15` (low-frequency)
   - Duration: continuously meets the above conditions for `> 500 ms`
4. Response & Cooldown
   - Vibration: pattern `[0, 1000, 500, 1000, 500, 1000]` (no repeat)
   - Event logging: `SessionManager.addEvent()`, UI updates via local broadcast
   - Cooldown: `VIBRATION_COOLDOWN_MS = 5000 ms` (ignore audio and broadcast zero amplitude during and shortly after vibration to avoid feedback loops)
5. UI Broadcast Contracts
   - Event: `ACTION_SNORE_DETECTED` with `EXTRA_SNORE_COUNT`
   - Amplitude: `ACTION_AMPLITUDE_UPDATE` with `EXTRA_AMPLITUDE` (forced `0` during cooldown)

## Environmental Noise Filtering
- Silence gate: drop frames with RMS below threshold
- Frequency gate: drop frames with ZCR above threshold (speech/hiss/typing)
- Temporal continuity: transient impulses cannot pass the `500 ms` continuity requirement
- Cooldown suppression: ignore audio and send zero amplitude during vibration + 1s buffer

## UI & Interaction
- Main screen:
  - Buttons: “Start Sleep”, “Stop Monitoring”, “Test Snore Detection”
  - Title and status labels, today’s cumulative snore count
  - Real-time amplitude graph:
    - Green when under threshold, red when exceeding
    - Red dashed threshold line with label “Threshold: xxx”
    - Bottom time labels (start & end of the visible window)
- Report screen: session statistics (MVP baseline)

## Permissions & Foreground Service
- Required permissions:
  - `RECORD_AUDIO`
  - `POST_NOTIFICATIONS` (Android 13+)
  - `WAKE_LOCK`
  - `FOREGROUND_SERVICE_MICROPHONE`
  - `VIBRATE`
- Foreground service type: `ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE`
- DND behavior: vibration is not blocked by Do Not Disturb (Android behavior)

## Icon & Resources
- Adaptive Icon:
  - Foreground: `app/src/main/res/drawable/ic_launcher_foreground.xml` (gold crescent + “Zzz” + waveform)
  - Background: `app/src/main/res/drawable/ic_launcher_background.xml` (deep night blue)
  - Combined: `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml`
- Resolutions: generated bitmaps satisfy store requirements (48/72/96/144/192, etc.)

## Build & Run
- Requirements: Android 8.0+ (adapted up to Android 14)
- Build toolchain:
  - AGP 8.3.0 / Kotlin 1.9.23 / Compose Compiler 1.5.11
  - Repos include Google, MavenCentral, and JitPack
- Commands:
  - Install: `gradle installDebug` (requires connected device)
  - Launch: `adb shell am start -n com.jinbo.smartsleep/.MainActivity`

## Tunables (Detection)
- In `SnoreDetectionService`:
  - `SAMPLE_RATE = 16000`
  - `RMS_THRESHOLD = 800.0`
  - `ZCR_THRESHOLD = 0.15`
  - `MIN_DURATION_MS = 500L`
  - `VIBRATION_COOLDOWN_MS = 5000L`
- Graph window:
  - Keep last 100 points (drop oldest beyond 100)
  - Display range up to `threshold * 1.2`
- Adjust above parameters to increase sensitivity or robustness as needed

## Testing & Acceptance
- Scenarios:
  - Quiet environment (< 30 dB), snore-like sound within 30 cm → should trigger
  - Speech/hiss/typing → should not trigger (high ZCR)
  - During vibration & cooldown → should not re-trigger; graph should flatten to zero
- Steps:
  1. Tap “Test Snore Detection” to start
  2. Observe graph vs threshold line and peaks
  3. Verify counter increments, vibration behavior, and cooldown suppression

## Known Issues & Roadmap
- Known:
  - UI updates use `LocalBroadcastManager` (deprecated). Will migrate to `Flow`/`LiveData` or service binding.
  - Stats use `SharedPreferences` (limited historical/multi-session support).
- Roadmap:
  - Integrate a TFLite acoustic model to replace/enhance heuristic detection (RMS+ZCR)
  - Migrate to Room for historical trends and weekly/monthly reports
  - Extend graph: longer window, zoom, event annotations
  - Add sensitivity/threshold settings in UI

## Directory Index (Key Files)
- Service: [SnoreDetectionService.kt](file:///Users/jinbo/Documents/code/SmartSleep/app/src/main/java/com/jinbo/smartsleep/service/SnoreDetectionService.kt)
- Capture: [AudioRecorder.kt](file:///Users/jinbo/Documents/code/SmartSleep/app/src/main/java/com/jinbo/smartsleep/audio/AudioRecorder.kt)
- Utils: [AudioUtils.kt](file:///Users/jinbo/Documents/code/SmartSleep/app/src/main/java/com/jinbo/smartsleep/audio/AudioUtils.kt)
- Data: [SessionManager.kt](file:///Users/jinbo/Documents/code/SmartSleep/app/src/main/java/com/jinbo/smartsleep/data/SessionManager.kt)
- UI: [MainActivity.kt](file:///Users/jinbo/Documents/code/SmartSleep/app/src/main/java/com/jinbo/smartsleep/MainActivity.kt) / [AmplitudeGraph.kt](file:///Users/jinbo/Documents/code/SmartSleep/app/src/main/java/com/jinbo/smartsleep/ui/AmplitudeGraph.kt)
- Manifest: [AndroidManifest.xml](file:///Users/jinbo/Documents/code/SmartSleep/app/src/main/AndroidManifest.xml)

## Version & Changes
- V2.2
  - Added ZCR filtering and 500ms continuity requirement
  - Introduced 5s cooldown (zero amplitude broadcast & audio ignore during vibration)
  - Graph supports threshold dashed line + start/end time labels
  - Switched to Adaptive Icon and fixed manifest references
  - Updated Compose Compiler and repos (including JitPack)

## Contribution
- Track requirements and issues; include reproduction steps and validation in PRs
- Follow Kotlin/Compose defaults; avoid logging sensitive info
