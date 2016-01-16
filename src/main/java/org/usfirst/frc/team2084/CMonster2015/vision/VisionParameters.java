/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2015.vision;

import org.opencv.core.Scalar;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;

/**
 * @author Ben Wolsieffer
 */
public class VisionParameters {

    public static final String H_MIN_KEY = "hMin";
    public static final String H_MAX_KEY = "hMax";

    public static final String S_MIN_KEY = "sMin";
    public static final String S_MAX_KEY = "sMax";

    public static final String V_MIN_KEY = "vMin";
    public static final String V_MAX_KEY = "vMax";

    public static final String MIN_BLOB_SIZE_KEY = "min_blob_area";
    
    public static final String APPROX_POLY_EPSILON_KEY = "approx_poly_epsilon";

    public static final ITable VISION_PARAMETERS = NetworkTable.getTable("Vision").getSubTable(
            "Parameters");

    public static void setHThreshold(Range r) {
        VISION_PARAMETERS.putNumber(H_MIN_KEY, r.getMin());
        VISION_PARAMETERS.putNumber(H_MAX_KEY, r.getMax());
    }

    public static void setSThreshold(Range r) {
        VISION_PARAMETERS.putNumber(S_MIN_KEY, r.getMin());
        VISION_PARAMETERS.putNumber(S_MAX_KEY, r.getMax());
    }

    public static void setVThreshold(Range r) {
        VISION_PARAMETERS.putNumber(V_MIN_KEY, r.getMin());
        VISION_PARAMETERS.putNumber(V_MAX_KEY, r.getMax());
    }

    public static Range getHThreshold() {
        return new Range((int) VISION_PARAMETERS.getNumber(H_MIN_KEY,
                0), (int) VISION_PARAMETERS.getNumber(H_MAX_KEY, 255));
    }

    public static Range getSThreshold() {
        return new Range((int) VISION_PARAMETERS.getNumber(S_MIN_KEY,
                0), (int) VISION_PARAMETERS.getNumber(S_MAX_KEY, 255));
    }

    public static Range getVThreshold() {
        return new Range((int) VISION_PARAMETERS.getNumber(V_MIN_KEY,
                0), (int) VISION_PARAMETERS.getNumber(V_MAX_KEY, 255));
    }

    public static Scalar getMinThreshold() {
        return new Scalar(VISION_PARAMETERS.getNumber(H_MIN_KEY, 0), VISION_PARAMETERS.getNumber(
                S_MIN_KEY, 0), (int) VISION_PARAMETERS.getNumber(V_MIN_KEY, 0));
    }

    public static Scalar getMaxThreshold() {
        return new Scalar(VISION_PARAMETERS.getNumber(H_MAX_KEY, 0), VISION_PARAMETERS.getNumber(
                S_MAX_KEY, 0), (int) VISION_PARAMETERS.getNumber(V_MAX_KEY, 0));

    }

    public static double getMinBlobArea() {
        return VISION_PARAMETERS.getNumber(MIN_BLOB_SIZE_KEY, 0);
    }

    public static void setMinBlobArea(double area) {
        VISION_PARAMETERS.putNumber(MIN_BLOB_SIZE_KEY, area);
    }
    
    public static double getApproxPolyEpsilon() {
        return VISION_PARAMETERS.getNumber(APPROX_POLY_EPSILON_KEY, 0);
    }

    public static void setApproxPolyEpsilon(double epsilon) {
        VISION_PARAMETERS.putNumber(APPROX_POLY_EPSILON_KEY, epsilon);
    }

    private VisionParameters() {
    }
}
