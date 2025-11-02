# GreenClicker - Final Enhanced Android prototype (HSV + ROI + Debug + Downscale)

This final package includes:
- HSV-based detection (robust to lighting)
- Region Of Interest (ROI) selection: interactive drag-overlay
- Debug overlay: shows detected points (green circle) and messages
- Processing downscale to improve performance (configurable)
- Floating control button to start/stop detection
- Settings UI and persistent storage (SharedPreferences)

**How to build & run**
1. Open the project in Android Studio.
2. Sync Gradle, build and install on a test device (minSdk 24+).
3. On device:
   - Launch app -> Request Screen Capture Permission (tap "Start now")
   - Start Overlay Service -> Grant draw-over-other-apps permission if requested
   - Enable Accessibility service for GreenClicker in Settings -> Accessibility
   - Use floating button to start/stop detection
   - Use "Interactive ROI" button in main UI to drag-select ROI on screen
   - Toggle "Debug Overlay" to show/hide detection circle and messages

**Performance tips**
- Increase downscale value (e.g., 4 or 6) to reduce CPU usage; coordinates are mapped back.
- Increase sampling step to reduce pixel checks.
- Consider adding OpenCV NDK for faster native processing if needed.

**Legal / Ethical**
- Only use on apps you own or are permitted to automate.
- Automating other apps may violate Terms of Service.

Enjoy. This is a final, ready-to-run educational project. Test responsibly.
