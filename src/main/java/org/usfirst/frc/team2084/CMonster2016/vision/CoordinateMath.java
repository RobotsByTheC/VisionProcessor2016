/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Coordinate transform math library.
 * 
 * @author Ben Wolsieffer
 */
public class CoordinateMath {

    /**
     * Rotate a point around the x axis and store the result in another point.
     * 
     * @param point the point to rotate
     * @param destPoint the output point
     * @param angle the angle to rotate (in radians)
     */
    public static void rotateX(Mat point, Mat destPoint, double angle) {
        Mat rotateMat = Mat.eye(3, 3, point.type());

        double c = Math.cos(angle);
        double s = Math.sin(angle);

        rotateMat.put(1, 1, c);
        rotateMat.put(1, 2, -s);
        rotateMat.put(2, 1, s);
        rotateMat.put(2, 2, c);

        matMult(rotateMat, point, destPoint);
    }

    /**
     * Rotate a point around the x axis.
     * 
     * @param point the point to rotate
     * @param angle the angle to rotate (in radians)
     */
    public static void rotateX(Mat point, double angle) {
        rotateX(point, point, angle);
    }

    /**
     * Rotate a point around the y axis and store the result in another point.
     * 
     * @param point the point to rotate
     * @param destPoint the output point
     * @param angle the angle to rotate (in radians)
     */
    public static void rotateY(Mat point, Mat destPoint, double angle) {
        Mat rotateMat = Mat.eye(3, 3, point.type());

        double c = Math.cos(angle);
        double s = Math.sin(angle);

        rotateMat.put(0, 0, c);
        rotateMat.put(0, 2, s);
        rotateMat.put(2, 0, -s);
        rotateMat.put(2, 2, c);

        matMult(rotateMat, point, destPoint);
    }

    /**
     * Rotate a point around the y axis.
     * 
     * @param point the point to rotate
     * @param angle the angle to rotate (in radians)
     */
    public static void rotateY(Mat point, double angle) {
        rotateY(point, point, angle);
    }

    /**
     * Rotate a point around the z axis and store the result in another point.
     * 
     * @param point the point to rotate
     * @param destPoint the output point
     * @param angle the angle to rotate (in radians)
     */
    public static void rotateZ(Mat point, Mat destPoint, double angle) {
        Mat rotateMat = Mat.eye(3, 3, point.type());

        double c = Math.cos(angle);
        double s = Math.sin(angle);

        rotateMat.put(0, 0, c);
        rotateMat.put(0, 1, -s);
        rotateMat.put(1, 0, s);
        rotateMat.put(1, 1, c);

        matMult(rotateMat, point, destPoint);
    }

    /**
     * Rotate a point around the z axis.
     * 
     * @param point the point to rotate
     * @param angle the angle to rotate (in radians)
     */
    public static void rotateZ(Mat point, double angle) {
        rotateZ(point, point, angle);
    }

    /**
     * Translate a point in space.
     * 
     * @param vec the input point
     * @param destVec the output point
     * @param x the x translation
     * @param y the y translation
     * @param z the z translation
     */
    public static void translate(Mat vec, Mat destVec, double x, double y, double z) {
        Mat translateVec = new Mat(3, 1, CvType.CV_64FC1);
        translateVec.put(0, 0, x);
        translateVec.put(1, 0, y);
        translateVec.put(2, 0, z);

        Core.add(vec, translateVec, destVec);
    }

    /**
     * Translate a point in space.
     * 
     * @param vec the point
     * @param x the x translation
     * @param y the y translation
     * @param z the z translation
     */
    public static void translate(Mat vec, double x, double y, double z) {
        translate(vec, vec, x, y, z);
    }

    /**
     * Multiply two matrices.
     * 
     * @param src1 the first matrix
     * @param src2 the second matrix
     * @param dest the output matrix
     */
    public static void matMult(Mat src1, Mat src2, Mat dest) {
        Core.gemm(src1, src2, 1, Mat.zeros(dest.size(), dest.type()), 1, dest);
    }
}
