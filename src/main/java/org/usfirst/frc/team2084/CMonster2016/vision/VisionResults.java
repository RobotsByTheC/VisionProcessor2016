/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;

/**
 * @author Ben Wolsieffer
 */
public class VisionResults {

    public static final ITable VISION_RESULTS = NetworkTable.getTable("Vision").getSubTable(
            "Results");

    public static final String HEADING_KEY = "heading";
    public static final String GOAL_HEADING_KEY = "goal_heading";
    public static final String SHOOTER_ANGLE_KEY = "shooter_angle";
    public static final String GOAL_ANGLE_KEY = "goal_angle";
    public static final String TARGET_TABLE_ENABLE_CAMERA_KEY = "enable_camera";

    public static double getCurrentHeading() {
        return VISION_RESULTS.getNumber(HEADING_KEY, 0);
    }

    public static void setCurrentHeading(double heading) {
        VISION_RESULTS.putNumber(HEADING_KEY, heading);
    }

    public static double getGoalHeading() {
        return VISION_RESULTS.getNumber(GOAL_HEADING_KEY, 0);
    }

    public static void setGoalHeading(double heading) {
        VISION_RESULTS.putNumber(GOAL_HEADING_KEY, heading);
    }

    public static double getShooterAngle() {
        return VISION_RESULTS.getNumber(SHOOTER_ANGLE_KEY, 0);
    }

    public static void setShooterAngle(double angle) {
        VISION_RESULTS.putNumber(SHOOTER_ANGLE_KEY, angle);
    }
    
    public static double getGoalAngle() {
        return VISION_RESULTS.getNumber(GOAL_ANGLE_KEY, 0);
    }

    public static void setGoalAngle(double angle) {
        VISION_RESULTS.putNumber(GOAL_ANGLE_KEY, angle);
    }
    
    public static void setCameraEnabled(boolean enabled) {
        VISION_RESULTS.putBoolean(TARGET_TABLE_ENABLE_CAMERA_KEY, enabled);
    }

    public static boolean isCameraEnabled() {
        return VISION_RESULTS.getBoolean(TARGET_TABLE_ENABLE_CAMERA_KEY, true);
    }

    private VisionResults() {
    }
}
