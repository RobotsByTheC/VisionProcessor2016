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
import java.util.stream.Collectors;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.usfirst.frc.team2084.CMonster2016.vision.capture.CameraCapture;

import com.kauailabs.navx.desktop.AHRS;
import com.kauailabs.navx.desktop.AHRS.SerialDataType;
import com.kauailabs.navx.desktop.Timer;

/**
 * Algorithm that finds the high goal on the FIRST Stronghold tower and
 * calculates the robot's distance from it and the heading of the target.
 *
 * @author Ben Wolsieffer
 */
public class HighGoalProcessor extends ThreadedVisionProcessor {

    public static final Size IMAGE_SIZE = new Size(640, 480);
    public static final double ESTIMATED_CAMERA_LATENCY = 0.1279;

    private volatile boolean newImage;
    private final Mat image = new Mat();
    private final Mat grayImage = new Mat();
    private volatile Target target;
    private volatile List<Target> allTargets;
    private volatile double processingFps;

    public static final int GYRO_UPDATE_RATE = 100;
    private final AHRS gyro = new AHRS("/dev/navx", SerialDataType.kProcessedData, (byte) GYRO_UPDATE_RATE);
    private final HistoryBuffer headingBuffer = new HistoryBuffer(GYRO_UPDATE_RATE / 2, GYRO_UPDATE_RATE);

    private final CameraCapture camera;

    private final FramerateCounter processingFpsCounter = new FramerateCounter();
    private final FramerateCounter streamingFpsCounter = new FramerateCounter();

    private long lastGyroTime = System.currentTimeMillis();

    /**
     * @param capture
     */
    public HighGoalProcessor(CameraCapture capture) {
        super(capture != null);
        if (capture != null) {
            capture.setResolution(IMAGE_SIZE);
        }
        this.camera = capture;
        gyro.setUpdateListener((timestamp) -> {
            synchronized (headingBuffer) {
                long currTime = System.currentTimeMillis();
                double yaw = Math.toRadians(gyro.getYaw());
                // System.out.println("yaw: " + yaw + ", dt: " + (currTime -
                // lastGyroTime));
                lastGyroTime = currTime;
                headingBuffer.newValue(Timer.getFPGATimestamp(), yaw);
            }
        });
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
    private static native void processNative(long inputImageAddr, long outputImageAddr, long grayImageAddr,
            int blurSize, double hMin, double sMin, double vMin, double hMax, double sMax, double vMax);

    /**
     * Do the actual processing of the image. This runs in a background thread
     * so that the stream can run as fast as possible. The algorithm has now
     * been optimized to the point where it can match the capture frame rate ,so
     * this is not as necessary anymore.
     * 
     * @param image the image to process
     * @param imageHeading the robot's heading when the image was taken
     */
    @Override
    public void backgroundProcess(Mat image) {

        // Pass the raw threshold values and addresses to the native code
        double[] minThreshold = VisionParameters.getGoalMinThreshold().val;
        double[] maxThreshold = VisionParameters.getGoalMaxThreshold().val;
        processNative(image.nativeObj, thresholdImage.nativeObj, grayImage.nativeObj,
                VisionParameters.getGoalBlurSize(), minThreshold[0], minThreshold[1], minThreshold[2], maxThreshold[0],
                maxThreshold[1], maxThreshold[2]);

        // Convert the image to HSV, threshold it and find contours
        List<MatOfPoint> contours = findContours(
                thresholdImage/* threshold(blur(convertToHsv(image))) */);

        // Convert the contours to Targets
        List<Target> possibleTargets = contours.stream().map((contour) -> {
            contour = convexHull(contour);
            return new Target(contour, grayImage);
        }).filter((t) -> t.isAreaValid()).collect(Collectors.toList());

        // Sort the targets to find the highest scoring one
        Collections.sort(possibleTargets);

        if (!possibleTargets.isEmpty()) {
            double heading = getHeading(image);

            target = possibleTargets.get(possibleTargets.size() - 1);
            if (target.isValid()) {
                VisionResults.setGoalHeading(heading + target.getGoalXAngle());
                VisionResults.setGoalAngle(target.getGoalYAngle());
                VisionResults.setGoalDistance(target.getDistance());
                VisionResults.update();
            } else {
                target = null;
            }
        } else {
            target = null;
        }

        allTargets = possibleTargets;

        // debugImage("HSV", hsvImage);
        debugImage("Threshold", thresholdImage);
        debugImage("Grayscale", grayImage);
        // debugImage("Blur", blurImage);

        processingFps = processingFpsCounter.update();
    }

    private double getHeading(Mat image) {
        double heading;
        {
            byte[] timestampBytes = new byte[9];
            image.get(0, 0, timestampBytes);
            long timestamp = Utils.bytesToLong(timestampBytes);

            synchronized (headingBuffer) {
                heading = headingBuffer.getValue(timestamp / 1000.0);
            }
        }
        return heading;
    }

    @Override
    public void preProcess(Mat image) {
        // Update camera exposure
        if (camera != null) {
            camera.setExposure(VisionParameters.getAimingExposure());
            camera.setAutoExposure(VisionParameters.getAimingAutoExposure());
        }

        // Save a snapshot if requested
        if (VisionParameters.shouldTakeSnapshot()) {
            String path = System.getProperty("user.home") + "/vision_snapshots/" + System.currentTimeMillis() + ".png";
            if (!Imgcodecs.imwrite(path, image)) {
                System.err.println("Could not save snapshot to: " + path);
            }
        }

        // Encode the estimated image timestamp into the top left corner
        byte[] timestamp = new byte[9];
        Utils.longToBytes((long) (System.currentTimeMillis() - (ESTIMATED_CAMERA_LATENCY * 1000)), timestamp);
        image.put(0, 0, timestamp);
    }

    @Override
    public void postProcess(Mat image) {
        // Draw other targets
        Target localTarget = target;
        if (localTarget != null) {
            localTarget.draw(image, true, getHeading(image));
        }

        List<Target> localAllTargets = allTargets;
        if (localAllTargets != null) {
            // Draw all other targets that were considered
            for (int i = localAllTargets.size() - (target == null ? 1 : 2); i >= 0; i--) {
                Target t = localAllTargets.get(i);
                t.draw(image, false, 0);
            }
        }

        // Draw the frame rates
        Utils.drawText(image, "Vision FPS: " + Target.NUMBER_FORMAT.format(processingFps), 20, 50);
        Utils.drawText(image, "Stream FPS: " + Target.NUMBER_FORMAT.format(streamingFpsCounter.update()), 20, 70);
    }

    private final Mat hsvImage = new Mat();

    private Mat convertToHsv(Mat image) {
        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);
        return hsvImage;
    }

    private final Mat blurImage = new Mat();

    private Mat blur(Mat image) {
        Imgproc.medianBlur(image, blurImage, 2 * VisionParameters.getGoalBlurSize() + 1);

        return blurImage;
    }

    private final Mat thresholdImage = new Mat();

    private Mat threshold(Mat image) {
        Core.inRange(image, VisionParameters.getGoalMinThreshold(), VisionParameters.getGoalMaxThreshold(),
                thresholdImage);
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