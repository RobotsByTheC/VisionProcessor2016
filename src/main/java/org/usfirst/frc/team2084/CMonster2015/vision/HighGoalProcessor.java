/* 
 * Copyright (c) 2014 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2015.vision;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.usfirst.frc.team2084.CMonster2015.vision.capture.CameraCapture;

/**
 *
 * @author Ben Wolsieffer
 */
public class HighGoalProcessor extends VisionProcessor {

    public static final Size IMAGE_SIZE = new Size(640, 480);

    /**
     * @param capture
     */
    public HighGoalProcessor(CameraCapture capture) {
        super(capture);

        if (capture != null) {
            capture.setResolution(IMAGE_SIZE);
        }
    }

    private static final Scalar TARGET_COLOR = new Scalar(255, 255, 255);

    private final FramerateCounter fps = new FramerateCounter();

    public void init() {
    }

    public void process(Mat image, Mat outImage) {

        boolean autonomousRunning = VisionResults.isAutonomousVisionRunning();

        // Convert the image to HSV, threshold it and find contours
        List<MatOfPoint> contours = findContours(threshold(convertToHsv(image)));

        // Array to hold blobs that possibly could be targets
        ArrayList<Target> possibleTargets = new ArrayList<>();

        ArrayList<MatOfPoint> quads = new ArrayList<>();
        
        // Convert the contours to Targets
        contours.stream().forEach(
                (contour) -> {
                    contour = convexHull(contour);
                    if (Imgproc.contourArea(contour) > VisionParameters.getMinBlobArea()) {
                        Target target = new Target(contour);
                        possibleTargets.add(target);
                        
                        quads.add(target.getQuad());
                    }
                });

        Imgproc.polylines(outImage, quads, true, TARGET_COLOR);
        
        debugImage("Threshold Image", thresholdImage);

        // Core.putText(outImage, Double.toString(fps.update()), new
        // Point(0,0),
        // Core.FONT_HERSHEY_SIMPLEX, 1.0, OTHER_TARGET_COLOR);

    }

    public void setTargetState(VisionResults.State state) {
        VisionResults.setState(state);
        VisionResults.setAutonomousVisionRunning(false);
        VisionResults.setCameraEnabled(false);
        System.out.println("Told robot: " + state);
        init();
    }

    public BufferedImage matToBufferedImage(Mat m) {
        MatOfByte matOfByte = new MatOfByte();

        Imgcodecs.imencode(".jpg", m, matOfByte);

        byte[] byteArray = matOfByte.toArray();
        BufferedImage bufImage = null;

        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
        } catch (IOException e) {
        }
        return bufImage;
    }

    private final Mat hsvImage = new Mat(IMAGE_SIZE, CvType.CV_8UC3);

    private Mat convertToHsv(Mat image) {
        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);
        return hsvImage;
    }

    private final Mat thresholdImage = new Mat(IMAGE_SIZE, CvType.CV_8UC1);

    private Mat threshold(Mat image) {
        Core.inRange(image, VisionParameters.getMinThreshold(), VisionParameters.getMaxThreshold(),
                thresholdImage);
        Imgproc.medianBlur(thresholdImage, thresholdImage, 13);
        return thresholdImage;
    }

    private final Mat contoursImage = new Mat(IMAGE_SIZE, CvType.CV_8UC1);

    private List<MatOfPoint> findContours(Mat image) {
        Mat hierarchy = new Mat();
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        image.copyTo(contoursImage);
        Imgproc.findContours(contoursImage, contours, hierarchy, Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }

    private RotatedRect getRectangle(MatOfPoint contour) {
        MatOfPoint2f poly = new MatOfPoint2f();
        MatOfPoint2f fContour = new MatOfPoint2f();
        contour.convertTo(fContour, CvType.CV_32F);

        return Imgproc.minAreaRect(fContour);
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