# VisionProcessor2016

#### Computer vision algorithms for the 2016 FRC season.

This project does nothing by itself and simply serves as a library which
[VisionServer2016](https://github.com/RobotsByTheC/VisionServer2016) and
[SmartDashboardExtensions2016](https://github.com/RobotsByTheC/SmartDashboardExtensions2015)
use.

### Requirements
* Java
* OpenCV 2.4.12 (this is the version the build script looks for, but it can be
  changed)

If the OpenCV jar cannot be found, you probably need to add its location to the
`opencvLocations` array in `build.gradle`.

If you get an `UnsatisfiedLinkError` when trying to run anything that uses this
library, you probably need to add the native library directory to
`OPENCV_SEARCH_PATHS` in `OpenCVLoader.java`.
