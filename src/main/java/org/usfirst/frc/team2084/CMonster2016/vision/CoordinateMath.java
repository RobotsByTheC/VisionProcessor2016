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
 * @author Ben Wolsieffer
 */
public class CoordinateMath {

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

    public static void rotateX(Mat point, double angle) {
        rotateX(point, point, angle);
    }

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

    public static void rotateY(Mat point, double angle) {
        rotateY(point, point, angle);
    }

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

    public static void rotateZ(Mat point, double angle) {
        rotateZ(point, point, angle);
    }

    public static void translate(Mat vec, Mat destVec, double x, double y, double z) {
        Mat translateVec = new Mat(3, 1, CvType.CV_64FC1);
        translateVec.put(0, 0, x);
        translateVec.put(1, 0, y);
        translateVec.put(2, 0, z);

        Core.add(vec, translateVec, destVec);
    }

    public static void translate(Mat vec, double x, double y, double z) {
        translate(vec, vec, x, y, z);
    }

    public static void matMult(Mat src1, Mat src2, Mat dest) {
        Core.gemm(src1, src2, 1, Mat.zeros(dest.size(), dest.type()), 1, dest);
    }
}
