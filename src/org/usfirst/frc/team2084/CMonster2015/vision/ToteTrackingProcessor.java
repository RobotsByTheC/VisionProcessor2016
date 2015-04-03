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
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.usfirst.frc.team2084.CMonster2015.vision.capture.CameraCapture;

/**
 * @author Ben Wolsieffer
 */
public class ToteTrackingProcessor extends VisionProcessor {

    private static final Scalar THRESHOLD_MIN = new Scalar(20, 0, 0);
    private static final Scalar THRESHOLD_MAX = new Scalar(30, 255, 255);

    private Mat hsvImage = new Mat();
    private Mat thresholdImage = new Mat();
    private List<MatOfPoint> contours = new ArrayList<>();

    /**
     * @param capture
     */
    public ToteTrackingProcessor(CameraCapture capture) {
        super(capture);
    }

    @Override
    protected void process(Mat cameraImage, Mat outputImage) {
        // Convert camera image to HSV.
        Imgproc.cvtColor(cameraImage, hsvImage, Imgproc.COLOR_BGR2HSV);

        Imgproc.blur(hsvImage, hsvImage, new Size(5, 5));

        // Threshold image to find yellow
        Core.inRange(hsvImage, THRESHOLD_MIN, THRESHOLD_MAX, thresholdImage);

        // Imgproc.findContours(thresholdImage, contours, null, Imgproc.);

        debugImage("HSV", hsvImage);
        debugImage("Threshold image", thresholdImage);
    }
}
