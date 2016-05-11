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

    public static final Range DEFAULT_GOAL_H_THRESHOLD = new Range(0, 176);
    public static final Range DEFAULT_GOAL_S_THRESHOLD = new Range(0, 255);
    public static final Range DEFAULT_GOAL_V_THRESHOLD = new Range(99, 255);
    public static final double DEFAULT_GOAL_MIN_BLOB_AREA = 2000;
    public static final double DEFAULT_GOAL_MIN_ASPECT_RATIO_SCORE = 10;
    public static final double DEFAULT_GOAL_MIN_RECTANGULARITY_WIDTH_SCORE = 10;
    public static final double DEFAULT_GOAL_MIN_RECTANGULARITY_HEIGHT_SCORE = 10;
    public static final double DEFAULT_GOAL_MAX_DISTANCE = 30;
    public static final double DEFAULT_GOAL_MIN_DISTANCE = 3;
    public static final double DEFAULT_GOAL_APPROX_POLY_EPSILON = 10;
    public static final int DEFAULT_GOAL_BLUR_SIZE = 6;
    public static final double DEFAULT_AIMING_EXPOSURE = 0;
    public static final Range DEFAULT_BOULDER_H_THRESHOLD = new Range(0, 255);
    public static final Range DEFAULT_BOULDER_S_THRESHOLD = new Range(0, 255);
    public static final Range DEFAULT_BOULDER_V_THRESHOLD = new Range(0, 255);
    public static final double DEFAULT_BOULDER_MIN_BLOB_AREA = 10000;

    public static final double DEFAULT_FOV_ANGLE = Math.toRadians(64.94);
    public static final int DEFAULT_STREAM_QUALITY = 20;
    public static final String DEFAULT_STREAM_IP = "10.20.84.6";

    public static final boolean DEFAULT_AUTO_EXPOSURE = true;

    private static final String CAMERA_SOURCE_KEY = "camera_source";

    private static final String GOAL_H_MIN_KEY = "goal_hMin";
    private static final String GOAL_H_MAX_KEY = "goal_hMax";

    private static final String GOAL_S_MIN_KEY = "goal_sMin";
    private static final String GOAL_S_MAX_KEY = "goal_sMax";

    private static final String GOAL_V_MIN_KEY = "goal_vMin";
    private static final String GOAL_V_MAX_KEY = "goal_vMax";

    private static final String GOAL_MIN_BLOB_AREA_KEY = "goal_min_blob_area";
    private static final String GOAL_MIN_ASPECT_RATIO_SCORE_KEY = "min_aspect_score";
    private static final String GOAL_MIN_RECTANGULARITY_WIDTH_SCORE_KEY = "min_rect_w_score";
    private static final String GOAL_MIN_RECTANGULARITY_HEIGHT_SCORE_KEY = "min_rect_h_score";
    private static final String GOAL_MAX_DISTANCE_KEY = "goal_max_dist";
    private static final String GOAL_MIN_DISTANCE_KEY = "goal_min_dist";
    private static final String GOAL_APPROX_POLY_EPSILON_KEY = "approx_poly_epsilon";

    private static final String GOAL_BLUR_SIZE_KEY = "blur_size";

    private static final String FOV_ANGLE_KEY = "fov_angle";

    private static final String AIMING_AUTO_EXPOSURE_KEY = "auto_exposure";
    private static final String AIMING_EXPOSURE_KEY = "exposure";

    private static final String BOULDER_H_MIN_KEY = "boulder_hMin";
    private static final String BOULDER_H_MAX_KEY = "boulder_hMax";

    private static final String BOULDER_S_MIN_KEY = "boulder_sMin";
    private static final String BOULDER_S_MAX_KEY = "boulder_sMax";

    private static final String BOULDER_V_MIN_KEY = "boulder_vMin";
    private static final String BOULDER_V_MAX_KEY = "boulder_vMax";

    private static final String BOULDER_MIN_BLOB_AREA_KEY = "boulder_min_blob_area";

    private static final String STREAM_QUALITY_KEY = "str_qual";

    private static final String SHUTDOWN_KEY = "shutdown";
    private static final String SNAPSHOT_KEY = "snapshot";

    private static final String INTAKE_CAMERA_KEY = "intake_cam";

    private static final String STREAM_IP_KEY = "stream_ip";

    public static final ITable VISION_PARAMETERS = NetworkTable.getTable("Vision").getSubTable("Parameters");

    static {
        setAimingAutoExposure(true);
        VISION_PARAMETERS.putBoolean(SNAPSHOT_KEY, false);
        VISION_PARAMETERS.putBoolean(SHUTDOWN_KEY, false);
    }

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

    public static void setGoalHThreshold(Range r) {
        VISION_PARAMETERS.putNumber(GOAL_H_MIN_KEY, r.getMin());
        VISION_PARAMETERS.putNumber(GOAL_H_MAX_KEY, r.getMax());
    }

    public static void setGoalSThreshold(Range r) {
        VISION_PARAMETERS.putNumber(GOAL_S_MIN_KEY, r.getMin());
        VISION_PARAMETERS.putNumber(GOAL_S_MAX_KEY, r.getMax());
    }

    public static void setGoalVThreshold(Range r) {
        VISION_PARAMETERS.putNumber(GOAL_V_MIN_KEY, r.getMin());
        VISION_PARAMETERS.putNumber(GOAL_V_MAX_KEY, r.getMax());
    }

    public static Range getGoalHThreshold() {
        return new Range((int) VISION_PARAMETERS.getNumber(GOAL_H_MIN_KEY, DEFAULT_GOAL_H_THRESHOLD.getMin()),
                (int) VISION_PARAMETERS.getNumber(GOAL_H_MAX_KEY, DEFAULT_GOAL_H_THRESHOLD.getMax()));
    }

    public static Range getGoalSThreshold() {
        return new Range((int) VISION_PARAMETERS.getNumber(GOAL_S_MIN_KEY, DEFAULT_GOAL_S_THRESHOLD.getMin()),
                (int) VISION_PARAMETERS.getNumber(GOAL_S_MAX_KEY, DEFAULT_GOAL_S_THRESHOLD.getMax()));
    }

    public static Range getGoalVThreshold() {
        return new Range((int) VISION_PARAMETERS.getNumber(GOAL_V_MIN_KEY, DEFAULT_GOAL_V_THRESHOLD.getMin()),
                (int) VISION_PARAMETERS.getNumber(GOAL_V_MAX_KEY, DEFAULT_GOAL_V_THRESHOLD.getMax()));
    }

    public static Scalar getGoalMinThreshold() {
        return new Scalar(VISION_PARAMETERS.getNumber(GOAL_H_MIN_KEY, DEFAULT_GOAL_H_THRESHOLD.getMin()),
                VISION_PARAMETERS.getNumber(GOAL_S_MIN_KEY, DEFAULT_GOAL_S_THRESHOLD.getMin()),
                (int) VISION_PARAMETERS.getNumber(GOAL_V_MIN_KEY, DEFAULT_GOAL_V_THRESHOLD.getMin()));
    }

    public static Scalar getGoalMaxThreshold() {
        return new Scalar(VISION_PARAMETERS.getNumber(GOAL_H_MAX_KEY, DEFAULT_GOAL_H_THRESHOLD.getMax()),
                VISION_PARAMETERS.getNumber(GOAL_S_MAX_KEY, DEFAULT_GOAL_S_THRESHOLD.getMax()),
                (int) VISION_PARAMETERS.getNumber(GOAL_V_MAX_KEY, DEFAULT_GOAL_V_THRESHOLD.getMax()));
    }

    public static double getGoalMinBlobArea() {
        return VISION_PARAMETERS.getNumber(GOAL_MIN_BLOB_AREA_KEY, DEFAULT_GOAL_MIN_BLOB_AREA);
    }

    public static void setGoalMinBlobArea(double area) {
        VISION_PARAMETERS.putNumber(GOAL_MIN_BLOB_AREA_KEY, area);
    }

    public static double getGoalMinAspectRatioScore() {
        return VISION_PARAMETERS.getNumber(GOAL_MIN_ASPECT_RATIO_SCORE_KEY, DEFAULT_GOAL_MIN_ASPECT_RATIO_SCORE);
    }

    public static void setGoalMinAspectRatioScore(double score) {
        VISION_PARAMETERS.putNumber(GOAL_MIN_ASPECT_RATIO_SCORE_KEY, score);
    }

    public static double getGoalMinRectangularityHeightScore() {
        return VISION_PARAMETERS.getNumber(GOAL_MIN_RECTANGULARITY_HEIGHT_SCORE_KEY,
                DEFAULT_GOAL_MIN_RECTANGULARITY_HEIGHT_SCORE);
    }

    public static void setGoalMinRectangularityHeightScore(double score) {
        VISION_PARAMETERS.putNumber(GOAL_MIN_RECTANGULARITY_HEIGHT_SCORE_KEY, score);
    }

    public static double getGoalMinRectangularityWidthScore() {
        return VISION_PARAMETERS.getNumber(GOAL_MIN_RECTANGULARITY_WIDTH_SCORE_KEY,
                DEFAULT_GOAL_MIN_RECTANGULARITY_WIDTH_SCORE);
    }

    public static void setGoalMinRectangularityWidthScore(double score) {
        VISION_PARAMETERS.putNumber(GOAL_MIN_RECTANGULARITY_WIDTH_SCORE_KEY, score);
    }

    public static double getGoalMaxDistance() {
        return VISION_PARAMETERS.getNumber(GOAL_MAX_DISTANCE_KEY, DEFAULT_GOAL_MAX_DISTANCE);
    }

    public static void setGoalMaxDistance(double distance) {
        VISION_PARAMETERS.putNumber(GOAL_MAX_DISTANCE_KEY, distance);
    }

    public static double getGoalMinDistance() {
        return VISION_PARAMETERS.getNumber(GOAL_MIN_DISTANCE_KEY, DEFAULT_GOAL_MIN_DISTANCE);
    }

    public static void setGoalMinDistance(double distance) {
        VISION_PARAMETERS.putNumber(GOAL_MIN_DISTANCE_KEY, distance);
    }

    public static double getGoalApproxPolyEpsilon() {
        return VISION_PARAMETERS.getNumber(GOAL_APPROX_POLY_EPSILON_KEY, DEFAULT_GOAL_APPROX_POLY_EPSILON);
    }

    public static void setApproxPolyEpsilon(double epsilon) {
        VISION_PARAMETERS.putNumber(GOAL_APPROX_POLY_EPSILON_KEY, epsilon);
    }

    public static int getGoalBlurSize() {
        return (int) VISION_PARAMETERS.getNumber(GOAL_BLUR_SIZE_KEY, DEFAULT_GOAL_BLUR_SIZE);
    }

    public static void setGoalBlurSize(int size) {
        VISION_PARAMETERS.putNumber(GOAL_BLUR_SIZE_KEY, size);
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

    public static void setAimingAutoExposure(boolean enabled) {
        VISION_PARAMETERS.putBoolean(AIMING_AUTO_EXPOSURE_KEY, enabled);
    }

    public static boolean getAimingAutoExposure() {
        return VISION_PARAMETERS.getBoolean(AIMING_AUTO_EXPOSURE_KEY, DEFAULT_AUTO_EXPOSURE);
    }

    public static void setAimingExposure(double exposure) {
        VISION_PARAMETERS.putNumber(AIMING_EXPOSURE_KEY, exposure);
    }

    public static double getAimingExposure() {
        return VISION_PARAMETERS.getNumber(AIMING_EXPOSURE_KEY, DEFAULT_AIMING_EXPOSURE);
    }

    public static void setStreamQuality(int quality) {
        VISION_PARAMETERS.putNumber(STREAM_QUALITY_KEY, quality);
    }

    public static int getStreamQuality() {
        return (int) VISION_PARAMETERS.getNumber(STREAM_QUALITY_KEY, DEFAULT_STREAM_QUALITY);
    }

    public static void setBoulderHThreshold(Range r) {
        VISION_PARAMETERS.putNumber(BOULDER_H_MIN_KEY, r.getMin());
        VISION_PARAMETERS.putNumber(BOULDER_H_MAX_KEY, r.getMax());
    }

    public static void setBoulderSThreshold(Range r) {
        VISION_PARAMETERS.putNumber(BOULDER_S_MIN_KEY, r.getMin());
        VISION_PARAMETERS.putNumber(BOULDER_S_MAX_KEY, r.getMax());
    }

    public static void setBoulderVThreshold(Range r) {
        VISION_PARAMETERS.putNumber(BOULDER_V_MIN_KEY, r.getMin());
        VISION_PARAMETERS.putNumber(BOULDER_V_MAX_KEY, r.getMax());
    }

    public static Range getBoulderHThreshold() {
        return new Range((int) VISION_PARAMETERS.getNumber(BOULDER_H_MIN_KEY, DEFAULT_BOULDER_H_THRESHOLD.getMin()),
                (int) VISION_PARAMETERS.getNumber(BOULDER_H_MAX_KEY, DEFAULT_BOULDER_H_THRESHOLD.getMax()));
    }

    public static Range getBoulderSThreshold() {
        return new Range((int) VISION_PARAMETERS.getNumber(BOULDER_S_MIN_KEY, DEFAULT_BOULDER_S_THRESHOLD.getMin()),
                (int) VISION_PARAMETERS.getNumber(BOULDER_S_MAX_KEY, DEFAULT_BOULDER_S_THRESHOLD.getMax()));
    }

    public static Range getBoulderVThreshold() {
        return new Range((int) VISION_PARAMETERS.getNumber(BOULDER_V_MIN_KEY, DEFAULT_BOULDER_V_THRESHOLD.getMin()),
                (int) VISION_PARAMETERS.getNumber(BOULDER_V_MAX_KEY, DEFAULT_BOULDER_V_THRESHOLD.getMax()));
    }

    public static Scalar getBoulderMinThreshold() {
        return new Scalar(VISION_PARAMETERS.getNumber(BOULDER_H_MIN_KEY, DEFAULT_BOULDER_H_THRESHOLD.getMin()),
                VISION_PARAMETERS.getNumber(BOULDER_S_MIN_KEY, DEFAULT_BOULDER_S_THRESHOLD.getMin()),
                (int) VISION_PARAMETERS.getNumber(BOULDER_V_MIN_KEY, DEFAULT_BOULDER_V_THRESHOLD.getMin()));
    }

    public static Scalar getBoulderMaxThreshold() {
        return new Scalar(VISION_PARAMETERS.getNumber(BOULDER_H_MAX_KEY, DEFAULT_BOULDER_H_THRESHOLD.getMax()),
                VISION_PARAMETERS.getNumber(BOULDER_S_MAX_KEY, DEFAULT_BOULDER_S_THRESHOLD.getMax()),
                (int) VISION_PARAMETERS.getNumber(BOULDER_V_MAX_KEY, DEFAULT_BOULDER_V_THRESHOLD.getMax()));
    }

    public static double getBoulderMinBlobArea() {
        return VISION_PARAMETERS.getNumber(BOULDER_MIN_BLOB_AREA_KEY, DEFAULT_BOULDER_MIN_BLOB_AREA);
    }

    public static void setBoulderMinBlobArea(double area) {
        VISION_PARAMETERS.putNumber(BOULDER_MIN_BLOB_AREA_KEY, area);
    }

    public static boolean isIntakeCamera() {
        return VISION_PARAMETERS.getBoolean(INTAKE_CAMERA_KEY, false);
    }

    public static void setIntakeCamera(boolean intakeCam) {
        VISION_PARAMETERS.putBoolean(INTAKE_CAMERA_KEY, intakeCam);
    }

    public static void setStreamIP(String ip) {
        VISION_PARAMETERS.putString(STREAM_IP_KEY, ip);
    }

    public static String getStreamIP() {
        return VISION_PARAMETERS.getString(STREAM_IP_KEY, DEFAULT_STREAM_IP);
    }

    public static void shutdown() {
        VISION_PARAMETERS.putBoolean(SHUTDOWN_KEY, true);
    }

    public static boolean shouldShutdown() {
        boolean shutdown = VISION_PARAMETERS.getBoolean(SHUTDOWN_KEY, false);
        if (shutdown) {
            VISION_PARAMETERS.putBoolean(SHUTDOWN_KEY, false);
        }
        return shutdown;
    }

    public static void takeSnapshot() {
        VISION_PARAMETERS.putBoolean(SNAPSHOT_KEY, true);
    }

    public static boolean shouldTakeSnapshot() {
        boolean snapshot = VISION_PARAMETERS.getBoolean(SNAPSHOT_KEY, false);
        if (snapshot) {
            VISION_PARAMETERS.putBoolean(SNAPSHOT_KEY, false);
        }
        return snapshot;
    }

    private VisionParameters() {
    }
}
