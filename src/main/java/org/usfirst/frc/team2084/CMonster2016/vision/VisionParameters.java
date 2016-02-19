/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision;

import org.opencv.core.Scalar;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;

/**
 * @author Ben Wolsieffer
 */
public class VisionParameters {

    public static final String DEFAULT_CAMERA_SOURCE = "0";

    public static final Range DEFAULT_H_THRESHOLD = new Range(0, 176);
    public static final Range DEFAULT_S_THRESHOLD = new Range(0, 255);
    public static final Range DEFAULT_V_THRESHOLD = new Range(99, 255);
    public static final double DEFAULT_MIN_BLOB_SIZE = 2000;
    public static final double DEFAULT_MIN_ASPECT_RATIO_SCORE = 10;
    public static final double DEFAULT_MIN_RECTANGULARITY_WIDTH_SCORE = 10;
    public static final double DEFAULT_MIN_RECTANGULARITY_HEIGHT_SCORE = 10;
    public static final double DEFAULT_APPROX_POLY_EPSILON = 10;
    public static final int DEFAULT_BLUR_SIZE = 6;
    public static final double DEFAULT_FOV_ANGLE = Math.toRadians(64.94);
    public static final double DEFAULT_EXPOSURE = 0;

    private static final String CAMERA_SOURCE_KEY = "camera_source";

    private static final String H_MIN_KEY = "hMin";
    private static final String H_MAX_KEY = "hMax";

    private static final String S_MIN_KEY = "sMin";
    private static final String S_MAX_KEY = "sMax";

    private static final String V_MIN_KEY = "vMin";
    private static final String V_MAX_KEY = "vMax";

    private static final String MIN_BLOB_SIZE_KEY = "min_blob_area";
    private static final String MIN_ASPECT_RATIO_SCORE_KEY = "min_aspect_score";
    private static final String MIN_RECTANGULARITY_WIDTH_SCORE_KEY = "min_rect_w_score";
    private static final String MIN_RECTANGULARITY_HEIGHT_SCORE_KEY = "min_rect_h_score";

    private static final String APPROX_POLY_EPSILON_KEY = "approx_poly_epsilon";

    private static final String BLUR_SIZE_KEY = "blur_size";

    private static final String FOV_ANGLE_KEY = "fov_angle";

    private static final String EXPOSURE_KEY = "exposure";

    public static final ITable VISION_PARAMETERS = NetworkTable.getTable("Vision").getSubTable("Parameters");

    public static void setCameraSource(String source) {
        VISION_PARAMETERS.putString(CAMERA_SOURCE_KEY, source);
    }

    private static String getCameraSource() {
        return VISION_PARAMETERS.getString(CAMERA_SOURCE_KEY, DEFAULT_CAMERA_SOURCE);
    }

    public static String getCameraSourceRemote() {
        String source = getCameraSource();
        try {
            Integer.parseInt(source);
            return null;
        } catch (NumberFormatException ex) {
            return source;
        }
    }

    public static int getCameraSourceLocal() {
        try {
            return Integer.parseInt(getCameraSource());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

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
        return new Range((int) VISION_PARAMETERS.getNumber(H_MIN_KEY, DEFAULT_H_THRESHOLD.getMin()),
                (int) VISION_PARAMETERS.getNumber(H_MAX_KEY, DEFAULT_H_THRESHOLD.getMax()));
    }

    public static Range getSThreshold() {
        return new Range((int) VISION_PARAMETERS.getNumber(S_MIN_KEY, DEFAULT_S_THRESHOLD.getMin()),
                (int) VISION_PARAMETERS.getNumber(S_MAX_KEY, DEFAULT_S_THRESHOLD.getMax()));
    }

    public static Range getVThreshold() {
        return new Range((int) VISION_PARAMETERS.getNumber(V_MIN_KEY, DEFAULT_V_THRESHOLD.getMin()),
                (int) VISION_PARAMETERS.getNumber(V_MAX_KEY, DEFAULT_V_THRESHOLD.getMax()));
    }

    public static Scalar getMinThreshold() {
        return new Scalar(VISION_PARAMETERS.getNumber(H_MIN_KEY, DEFAULT_H_THRESHOLD.getMin()),
                VISION_PARAMETERS.getNumber(S_MIN_KEY, DEFAULT_S_THRESHOLD.getMin()),
                (int) VISION_PARAMETERS.getNumber(V_MIN_KEY, DEFAULT_V_THRESHOLD.getMin()));
    }

    public static Scalar getMaxThreshold() {
        return new Scalar(VISION_PARAMETERS.getNumber(H_MAX_KEY, DEFAULT_H_THRESHOLD.getMax()),
                VISION_PARAMETERS.getNumber(S_MAX_KEY, DEFAULT_S_THRESHOLD.getMax()),
                (int) VISION_PARAMETERS.getNumber(V_MAX_KEY, DEFAULT_V_THRESHOLD.getMax()));

    }

    public static double getMinBlobArea() {
        return VISION_PARAMETERS.getNumber(MIN_BLOB_SIZE_KEY, DEFAULT_MIN_BLOB_SIZE);
    }

    public static void setMinBlobArea(double area) {
        VISION_PARAMETERS.putNumber(MIN_BLOB_SIZE_KEY, area);
    }

    public static double getMinAspectRatioScore() {
        return VISION_PARAMETERS.getNumber(MIN_ASPECT_RATIO_SCORE_KEY, DEFAULT_MIN_ASPECT_RATIO_SCORE);
    }

    public static void setMinAspectRatioScore(double score) {
        VISION_PARAMETERS.putNumber(MIN_ASPECT_RATIO_SCORE_KEY, score);
    }

    public static double getMinRectangularityHeightScore() {
        return VISION_PARAMETERS.getNumber(MIN_RECTANGULARITY_HEIGHT_SCORE_KEY,
                DEFAULT_MIN_RECTANGULARITY_HEIGHT_SCORE);
    }

    public static void setMinRectangularityHeightScore(double score) {
        VISION_PARAMETERS.putNumber(MIN_RECTANGULARITY_HEIGHT_SCORE_KEY, score);
    }

    public static double getMinRectangularityWidthScore() {
        return VISION_PARAMETERS.getNumber(MIN_RECTANGULARITY_WIDTH_SCORE_KEY, DEFAULT_MIN_RECTANGULARITY_WIDTH_SCORE);
    }

    public static void setMinRectangularityWidthScore(double score) {
        VISION_PARAMETERS.putNumber(MIN_RECTANGULARITY_WIDTH_SCORE_KEY, score);
    }

    public static double getApproxPolyEpsilon() {
        return VISION_PARAMETERS.getNumber(APPROX_POLY_EPSILON_KEY, DEFAULT_APPROX_POLY_EPSILON);
    }

    public static void setApproxPolyEpsilon(double epsilon) {
        VISION_PARAMETERS.putNumber(APPROX_POLY_EPSILON_KEY, epsilon);
    }

    public static int getBlurSize() {
        return (int) VISION_PARAMETERS.getNumber(BLUR_SIZE_KEY, DEFAULT_BLUR_SIZE);
    }

    public static void setBlurSize(int size) {
        VISION_PARAMETERS.putNumber(BLUR_SIZE_KEY, size);
    }

    public static double getFOVAngle() {
        return VISION_PARAMETERS.getNumber(FOV_ANGLE_KEY, DEFAULT_FOV_ANGLE);
    }

    public static void setFOVAngleRadians(double angleRadians) {
        VISION_PARAMETERS.putNumber(FOV_ANGLE_KEY, angleRadians);
    }

    public static void setFOVAngleDegrees(double angleDegrees) {
        VISION_PARAMETERS.putNumber(FOV_ANGLE_KEY, Math.toRadians(angleDegrees));
    }

    public static void setExposure(double exposure) {
        VISION_PARAMETERS.putNumber(EXPOSURE_KEY, exposure);
    }

    public static double getExposure() {
        return VISION_PARAMETERS.getNumber(EXPOSURE_KEY, DEFAULT_EXPOSURE);
    }

    private VisionParameters() {
    }
}
