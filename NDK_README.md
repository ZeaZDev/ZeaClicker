NDK & OpenCV notes:
- This project includes a simple native HSV detector that works on raw RGBA buffers.
- For better performance and robustness, you may integrate OpenCV Android SDK:
  1. Download OpenCV Android SDK from https://opencv.org/releases/
  2. Unpack and add OpenCV as a module or point CMake to the OpenCV SDK native libs.
  3. Update CMakeLists.txt to include OpenCV headers and link to opencv_java4.
- The included native code is self-contained and does not require OpenCV; it's a minimal example.
