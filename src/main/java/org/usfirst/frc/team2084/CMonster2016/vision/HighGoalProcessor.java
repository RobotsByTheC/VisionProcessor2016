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

    static {
        System.load("/home/ben/Dropbox/Robotics/Code/2016/FRC/VisionProcessor2016/lib/gpuvision/libgpuvision.so");
    }

    private class ProcessingThread implements Runnable {

        private final Mat localImage = new Mat();

        @Override
        public void run() {
            while (true) {
                boolean localNewImage = false;
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
                }
                if (localNewImage) {
                    doProcessing(localImage);
                }
            }
        }
    }

    private volatile boolean newImage;
    private final Mat image = new Mat();
    private volatile Target target;
    private volatile double processingFps;

    private final FramerateCounter processingFpsCounter = new FramerateCounter();
    private final FramerateCounter streamingFpsCounter = new FramerateCounter();

    public static final Size IMAGE_SIZE = new Size(640, 480);

    /**
     * @param capture
     */
    public HighGoalProcessor(CameraCapture capture) {
        super(capture);

        if (capture != null) {
            capture.setResolution(IMAGE_SIZE);
        }
        new Thread(new ProcessingThread()).start();
    }

    private static native void processNative(long inputImageAddr, long outputImageAddr, int blurSize);

    private void doProcessing(Mat image) {

        processNative(image.nativeObj, thresholdImage.nativeObj, VisionParameters.getBlurSize());

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

        Collections.sort(possibleTargets);

        if (!possibleTargets.isEmpty()) {
            for (int i = 0; i < possibleTargets.size(); i++) {
                Target t = possibleTargets.get(i);
                target = t;
                if (i == possibleTargets.size() - 1) {
                    VisionResults.setGoalHeading(VisionResults.getCurrentHeading() + t.getGoalXAngle());
                    VisionResults.setGoalAngle(VisionResults.getShooterAngle() + t.getGoalYAngle());
                    VisionResults.setGoalDistance(t.getDistance());
                    VisionResults.update();
                }
            }
        } else {
            target = null;
        }

        // debugImage("HSV", hsvImage);
        debugImage("Threshold", thresholdImage);
        // debugImage("Blur", blurImage);

        processingFps = processingFpsCounter.update();
    }

    @Override
    public void process(Mat image, Mat outImage) {
        // Update camera exposure
        camera.setExposure(VisionParameters.getExposure());
        camera.setAutoExposure(VisionParameters.getAutoExposure());

        if (VisionParameters.shouldTakeSnapshot()) {
            String path = System.getProperty("user.home") + "/vision_snapshots/" + System.currentTimeMillis() + ".png";
            if (!Imgcodecs.imwrite(path, image)) {
                System.err.println("Could not save snapshot to: " + path);
            }
        }

        synchronized (this.image) {
            image.copyTo(this.image);
            newImage = true;
            this.image.notify();
        }

        Target localTarget = target;
        if (localTarget != null) {
            localTarget.draw(outImage, true);
        }

        Imgproc.putText(outImage, "Vision FPS: " + Target.NUMBER_FORMAT.format(processingFps), new Point(20, 70),
                Core.FONT_HERSHEY_PLAIN, Target.TEXT_SIZE, Target.TEXT_COLOR);
        Imgproc.putText(outImage, "Stream FPS: " + Target.NUMBER_FORMAT.format(streamingFpsCounter.update()),
                new Point(20, 90), Core.FONT_HERSHEY_PLAIN, Target.TEXT_SIZE, Target.TEXT_COLOR);
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