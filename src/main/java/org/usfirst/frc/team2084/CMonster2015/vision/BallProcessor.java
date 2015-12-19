/* 
 * Copyright (c) 2015 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2015.vision;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.usfirst.frc.team2084.CMonster2015.vision.capture.CameraCapture;

/**
 * @author Ben Wolsieffer
 */
public class BallProcessor extends VisionProcessor {

    private static final Scalar CONTOUR_COLOR = new Scalar(255, 0, 0);

    private int KERNEL_SIZE = 5;
    private Mat KERNEL = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
            new Size(2 * KERNEL_SIZE + 1, 2 * KERNEL_SIZE + 1),
            new Point(-1, -1));

    private Scalar thresholdMin = new Scalar(24, 0, 30);
    private Scalar thresholdMax = new Scalar(135, 196, 225);

    private final Mat hsvImage = new Mat();
    private final Mat thresholdImage = new Mat();
    private final Mat contourImage = new Mat();
    private final List<MatOfPoint> contours = new ArrayList<>();
    private final List<MatOfPoint> hulls = new ArrayList<>();
    private final Mat hierarchy = new Mat();

    /**
     * @param capture
     */
    public BallProcessor(CameraCapture capture) {
        super(capture);
    }

    @Override
    protected void process(Mat cameraImage, Mat outputImage) {
        Imgproc.blur(cameraImage, hsvImage, new Size(20, 20));

        Imgproc.cvtColor(hsvImage, hsvImage, Imgproc.COLOR_BGR2HSV);

        // Threshold image to find blue/green
        Core.inRange(hsvImage, thresholdMin, thresholdMax, thresholdImage);

        Imgproc.erode(thresholdImage, thresholdImage, KERNEL);
        Imgproc.dilate(thresholdImage, thresholdImage, KERNEL);

        thresholdImage.copyTo(contourImage);

        contours.clear();
        hulls.clear();

        Imgproc.findContours(contourImage, contours, hierarchy, Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.drawContours(outputImage, contours, -1, CONTOUR_COLOR);

        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);
            MatOfInt hullIndices = new MatOfInt();
            MatOfPoint hull;
            hulls.add(hull = new MatOfPoint());
            Imgproc.convexHull(contour, hullIndices);

            for (int c = 0; c < hullIndices.rows(); c++) {
                int v = (int) hullIndices.get(c, 0)[0];
                hull.put(c, 0, contour.get(v, 0));
                // System.out.println(v);
            }

            // System.out.println(hull.size());

        }

        Imgproc.drawContours(outputImage, hulls, -1, CONTOUR_COLOR);

        debugImage("Threshold Image", thresholdImage);
        // debugImage("Contour Image", contourImage);
    }

    public void setHThreshold(Range r) {
        thresholdMin.val[0] = r.getMin();
        thresholdMax.val[0] = r.getMax();
    }

    public void setSThreshold(Range r) {
        thresholdMin.val[1] = r.getMin();
        thresholdMax.val[1] = r.getMax();
    }

    public void setVThreshold(Range r) {
        thresholdMin.val[2] = r.getMin();
        thresholdMax.val[2] = r.getMax();
    }

    public Scalar getThresholdMin() {
        return thresholdMin;
    }

    public Scalar getThresholdMax() {
        return thresholdMax;
    }
}
