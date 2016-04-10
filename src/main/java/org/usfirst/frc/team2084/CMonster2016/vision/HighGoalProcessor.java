/* 
 * Copyright (c) 2014 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.usfirst.frc.team2084.CMonster2016.vision.capture.CameraCapture;

/**
 * Algorithm that finds the high goal on the FIRST Stronghold tower and
 * calculates the robot's distance from it and the heading of the target.
 *
 * @author Ben Wolsieffer
 */
public class HighGoalProcessor extends VisionProcessor {

    private class ProcessingThread implements Runnable {

        private final Mat localImage = new Mat();

        @Override
        public void run() {
            while (true) {
                boolean localNewImage = false;
                double localImageHeading;
                synchronized (image) {
                    try {
                        image.wait();
                    } catch (InterruptedException e) {
                    }
                    if (newImage) {
                        newImage = false;
                        localNewImage = true;
                        image.copyTo(localImage);
                    }
                    localImageHeading = imageHeading;
                }
                if (localNewImage) {
                    doProcessing(localImage, localImageHeading);
                }
            }
        }
    }

    private volatile boolean newImage;
    private final Mat image = new Mat();
    private volatile double imageHeading;
    private volatile Target target;
    private volatile double processingFps;

    private final CameraCapture camera;

    private final FramerateCounter processingFpsCounter = new FramerateCounter();
    private final FramerateCounter streamingFpsCounter = new FramerateCounter();

    public static final Size IMAGE_SIZE = new Size(640, 480);

    /**
     * @param capture
     */
    public HighGoalProcessor(CameraCapture capture) {
        if (capture != null) {
            capture.setResolution(IMAGE_SIZE);
        }
        this.camera = capture;
        new Thread(new ProcessingThread()).start();
    }

    /**
     * Native function that does the color conversion, blurring and thresholding
     * of the image. This runs on the an Nvidia GPU if available.
     * 
     * @param inputImageAddr the address of the input image
     * @param outputImageAddr the address of the image to write to
     * @param blurSize the size of the gaussian blur kernel
     * @param hMin the minimum hue
     * @param sMin the minimum saturation
     * @param vMin the minimum value
     * @param hMax the maximum hue
     * @param sMax the maximum saturation
     * @param vMax the maximum value
     */
    private static native void processNative(long inputImageAddr, long outputImageAddr, int blurSize, double hMin,
            double sMin, double vMin, double hMax, double sMax, double vMax);

    /**
     * Do the actual processing of the image. This runs in a background thread
     * so that the stream can run as fast as possible. The algorithm has now
     * been optimized to the point where it can match the capture frame rate ,so
     * this is not as necessary anymore.
     * 
     * @param image the image to process
     * @param imageHeading the robot's heading when the image was taken
     */
    private void doProcessing(Mat image, double imageHeading) {

        // Pass the raw threshold values and addresses to the native code
        double[] minThreshold = VisionParameters.getMinThreshold().val;
        double[] maxThreshold = VisionParameters.getMaxThreshold().val;
        processNative(image.nativeObj, thresholdImage.nativeObj, VisionParameters.getBlurSize(), minThreshold[0],
                minThreshold[1], minThreshold[2], maxThreshold[0], maxThreshold[1], maxThreshold[2]);

        // Convert the image to HSV, threshold it and find contours
        List<MatOfPoint> contours = findContours(
                thresholdImage/* threshold(blur(convertToHsv(image))) */);

        // Array to hold blobs that possibly could be targets
        ArrayList<Target> possibleTargets = new ArrayList<>();

        // Convert the contours to Targets
        contours.stream().forEach((contour) -> {
            contour = convexHull(contour);
            Target target = new Target(contour);
            if (target.isValid()) {
                possibleTargets.add(target);
            }
        });

        // Sort the targets to find the highest scoring one
        Collections.sort(possibleTargets);

        if (!possibleTargets.isEmpty()) {
            target = possibleTargets.get(possibleTargets.size() - 1);
            VisionResults.setGoalHeading(imageHeading + target.getGoalXAngle());
            VisionResults.setGoalAngle(VisionResults.getShooterAngle() + target.getGoalYAngle());
            VisionResults.setGoalDistance(target.getDistance());
            VisionResults.update();
        } else {
            target = null;
        }

        // debugImage("HSV", hsvImage);
        debugImage("Threshold", thresholdImage);
        // debugImage("Blur", blurImage);

        processingFps = processingFpsCounter.update();
    }

    @Override
    public void process(Mat image) {
        // Update camera exposure
        camera.setExposure(VisionParameters.getExposure());
        camera.setAutoExposure(VisionParameters.getAutoExposure());

        // Save a snapshot if requested
        if (VisionParameters.shouldTakeSnapshot()) {
            String path = System.getProperty("user.home") + "/vision_snapshots/" + System.currentTimeMillis() + ".png";
            if (!Imgcodecs.imwrite(path, image)) {
                System.err.println("Could not save snapshot to: " + path);
            }
        }

        // Copy the image for the processing thread
        synchronized (this.image) {
            image.copyTo(this.image);
            imageHeading = VisionResults.getCurrentHeading();
            newImage = true;
            this.image.notify();
        }

        Target localTarget = target;
        if (localTarget != null) {
            localTarget.draw(image, true);
        }

        // Draw the frame rates
        Imgproc.putText(image, "Vision FPS: " + Target.NUMBER_FORMAT.format(processingFps), new Point(20, 70),
                Core.FONT_HERSHEY_PLAIN, Target.TEXT_SIZE, Target.TEXT_COLOR);
        Imgproc.putText(image, "Stream FPS: " + Target.NUMBER_FORMAT.format(streamingFpsCounter.update()),
                new Point(20, 90), Core.FONT_HERSHEY_PLAIN, Target.TEXT_SIZE, Target.TEXT_COLOR);
    }

    private final Mat hsvImage = new Mat();

    private Mat convertToHsv(Mat image) {
        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);
        return hsvImage;
    }

    private final Mat blurImage = new Mat();

    private Mat blur(Mat image) {
        Imgproc.medianBlur(image, blurImage, 2 * VisionParameters.getBlurSize() + 1);

        return blurImage;
    }

    private final Mat thresholdImage = new Mat();

    private Mat threshold(Mat image) {
        Core.inRange(image, VisionParameters.getMinThreshold(), VisionParameters.getMaxThreshold(), thresholdImage);
        return thresholdImage;
    }

    private final Mat contoursImage = new Mat();

    private List<MatOfPoint> findContours(Mat image) {
        Mat hierarchy = new Mat();
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        image.copyTo(contoursImage);
        Imgproc.findContours(contoursImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }

    private MatOfPoint convexHull(MatOfPoint contour) {
        MatOfInt hullMatrix = new MatOfInt();
        Imgproc.convexHull(contour, hullMatrix); // perform convex hull, gap
                                                 // filler
        MatOfPoint hull = new MatOfPoint();
        hull.create(hullMatrix.rows(), 1, CvType.CV_32SC2);

        for (int r = 0; r < hullMatrix.rows(); r++) {
            hull.put(r, 0, contour.get((int) hullMatrix.get(r, 0)[0], 0));
        }

        return hull;
    }
}