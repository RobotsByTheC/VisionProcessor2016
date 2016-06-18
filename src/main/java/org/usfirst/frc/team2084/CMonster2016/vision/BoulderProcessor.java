/* 
 * Copyright (c) 2015 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.usfirst.frc.team2084.CMonster2016.vision.capture.CameraCapture;

/**
 * A processor designed to track a boulder based on its color. It didn't work
 * and we didn't need it.
 * 
 * @author Ben Wolsieffer
 */
public class BoulderProcessor extends ThreadedVisionProcessor {

    private static final Scalar CONTOUR_COLOR = new Scalar(255, 0, 0);

    private int KERNEL_SIZE = 5;
    private Mat KERNEL = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
            new Size(2 * KERNEL_SIZE + 1, 2 * KERNEL_SIZE + 1), new Point(-1, -1));

    private final Mat hsvImage = new Mat();
    private final Mat thresholdImage = new Mat();
    private final Mat contourImage = new Mat();
    private final List<MatOfPoint> contours = new ArrayList<>();
    private final List<MatOfPoint> hulls = new ArrayList<>();
    private final Mat hierarchy = new Mat();
    private final CameraCapture camera;

    /**
     * @param capture
     */
    public BoulderProcessor(CameraCapture camera) {
        super(camera != null);
        this.camera = camera;
    }

    /**
     * Native function that does the color conversion and thresholding of the
     * image. This runs on the an NVIDIA GPU if available.
     * 
     * @param inputImageAddr the address of the input image
     * @param outputImageAddr the address of the image to write to
     * @param hMin the minimum hue
     * @param sMin the minimum saturation
     * @param vMin the minimum value
     * @param hMax the maximum hue
     * @param sMax the maximum saturation
     * @param vMax the maximum value
     */
    private static native void processNative(long inputImageAddr, long outputImageAddr, double hMin, double sMin,
            double vMin, double hMax, double sMax, double vMax);

    @Override
    public void backgroundProcess(Mat cameraImage) {
        Imgproc.cvtColor(cameraImage, hsvImage, Imgproc.COLOR_BGR2HSV);

        // Threshold image to find blue/green
        Core.inRange(hsvImage, VisionParameters.getBoulderMinThreshold(), VisionParameters.getBoulderMaxThreshold(),
                thresholdImage);

        Imgproc.erode(thresholdImage, thresholdImage, KERNEL);
        Imgproc.dilate(thresholdImage, thresholdImage, KERNEL);

        thresholdImage.copyTo(contourImage);

        contours.clear();
        hulls.clear();

        Imgproc.findContours(contourImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.drawContours(cameraImage, contours, -1, CONTOUR_COLOR);

        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);
            hulls.add(convexHull(contour));
        }

        Imgproc.drawContours(cameraImage, hulls, -1, CONTOUR_COLOR);

        debugImage("Boulder Threshold", thresholdImage);
    }

    /**
     * @param image
     */
    @Override
    public void preProcess(Mat image) {

    }

    /**
     * @param image
     */
    @Override
    public void postProcess(Mat image) {

    }

    private static MatOfPoint convexHull(MatOfPoint contour) {
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
