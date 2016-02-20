/* 
 * Copyright (c) 2014 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.usfirst.frc.team2084.CMonster2016.vision.capture.CameraCapture;

/**
 *
 * @author Ben Wolsieffer
 */
public class HighGoalProcessor extends VisionProcessor {

    private final FramerateCounter fpsCounter = new FramerateCounter();

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

    @Override
    public void process(Mat image, Mat outImage) {
        // Convert the image to HSV, threshold it and find contours
        List<MatOfPoint> contours = findContours(threshold(blur(convertToHsv(image))));

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

        Collections.sort(possibleTargets);

        for (int i = 0; i < possibleTargets.size(); i++) {
            Target t = possibleTargets.get(i);
            t.draw(outImage, i == possibleTargets.size() - 1);
            if (i == possibleTargets.size() - 1) {
                VisionResults.setGoalHeading(VisionResults.getCurrentHeading() - t.getGoalXAngle());
                VisionResults.setGoalAngle(VisionResults.getShooterAngle() + t.getGoalYAngle());
                VisionResults.setGoalDistance(t.getDistance());
                VisionResults.update();
            }
        }

        debugImage("HSV", hsvImage);
        debugImage("Threshold", thresholdImage);
        debugImage("Blur", blurImage);

        Imgproc.putText(outImage, "FPS: " + Target.NUMBER_FORMAT.format(fpsCounter.update()), new Point(20, 70),
                Core.FONT_HERSHEY_PLAIN, Target.TEXT_SIZE, Target.TEXT_COLOR);
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