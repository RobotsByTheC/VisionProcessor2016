/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

/**
 * @author Ben Wolsieffer
 */
public class CameraCalibration extends VisionProcessor {

    private Size boardSize = new Size(9, 6);
    private double squareSize = 1;
    private double aspectRatio = 1;
    private List<Mat> calibrationCorners = new LinkedList<>();
    private Mat cameraMatrix = Mat.eye(3, 3, CvType.CV_64F);
    private MatOfDouble distCoeffs = new MatOfDouble(Mat.zeros(8, 1, CvType.CV_64F));

    private Mat boardImage = new Mat();
    private Mat undistortImage = new Mat();

    private double error = 0;

    public CameraCalibration(Size boardSize, double squareSize, double aspectRatio) {
        this.boardSize = boardSize;
        this.squareSize = squareSize;
    }

    public void setBoardSize(Size size) {
        this.boardSize = size;
    }

    public void setSquareSize(double squareSize) {
        this.squareSize = squareSize;
    }

    public void setAspectRatio(double aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public Mat getCameraMatrix() {
        return cameraMatrix;
    }

    public MatOfDouble getDistCoeffs() {
        return distCoeffs;
    }

    public void clearCalibrationImages() {
        calibrationCorners.clear();
    }

    /**
     * @param image
     */
    public void process(Mat image, boolean addToCalibration) {
        boolean patternFound = Calib3d.findChessboardCorners(image, boardSize, boardCorners,
                Calib3d.CALIB_CB_ADAPTIVE_THRESH | Calib3d.CALIB_CB_NORMALIZE_IMAGE | Calib3d.CALIB_CB_FAST_CHECK);

        if (patternFound) {
            // Refine corner positions to be more accurate
            Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
            Imgproc.cornerSubPix(grayImage, boardCorners, new Size(6, 6), new Size(-1, -1),
                    new TermCriteria(TermCriteria.EPS + TermCriteria.COUNT, 30, 0.1));

            if (addToCalibration) {
                calibrationCorners.add(boardCorners);
            }

        }

        image.copyTo(boardImage);
        Calib3d.drawChessboardCorners(boardImage, boardSize, boardCorners, patternFound);

        if (!addToCalibration) {
            debugImage("Board", boardImage);
        }

        Imgproc.undistort(image, undistortImage, cameraMatrix, distCoeffs);

        undistortImage.copyTo(image);

        Imgproc.putText(image, "Error: " + error, new Point(20, 20), Core.FONT_HERSHEY_PLAIN, 1.5,
                new Scalar(0, 255, 0));
    }

    public double calibrate() {
        cameraMatrix = Mat.eye(3, 3, CvType.CV_64F);
        distCoeffs = new MatOfDouble(Mat.zeros(8, 1, CvType.CV_64F));
        List<Mat> rvecs = new LinkedList<>();
        List<Mat> tvecs = new LinkedList<>();

        // Set the fixed aspect ratio
        cameraMatrix.put(0, 0, aspectRatio);

        List<Mat> objectPoints = Collections.nCopies(calibrationCorners.size(), calcBoardCornerPositions());

        System.out.println(cameraMatrix);
        return error = Calib3d.calibrateCamera(objectPoints, calibrationCorners, HighGoalProcessor.IMAGE_SIZE,
                cameraMatrix, distCoeffs, rvecs, tvecs, Calib3d.CALIB_FIX_PRINCIPAL_POINT);
    }

    private MatOfPoint3f calcBoardCornerPositions() {
        Point3[] cornersArr = new Point3[(int) boardSize.area()];

        for (int i = 0; i < boardSize.height; ++i) {
            for (int j = 0; j < boardSize.width; ++j) {
                cornersArr[(int) ((i * boardSize.width) + j)] = new Point3(j * squareSize, i * squareSize, 0);
            }
        }
        MatOfPoint3f corners = new MatOfPoint3f(cornersArr);
        System.out.println(corners);
        return corners;
    }

    @Override
    public void process(Mat image) {
        process(image, false);
    }

    private Mat grayImage = new Mat();
    private MatOfPoint2f boardCorners = new MatOfPoint2f();
}
