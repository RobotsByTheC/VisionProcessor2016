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

    public static final ITable VISION_RESULTS = NetworkTable.getTable("Vision").getSubTable("Results");

    public static final String HEADING_KEY = "heading";
    public static final String GOAL_HEADING_KEY = "goal_heading";
    public static final String SHOOTER_ANGLE_KEY = "shooter_angle";
    public static final String GOAL_ANGLE_KEY = "goal_angle";
    public static final String GOAL_DISTANCE_KEY = "goal_distance";
    public static final String UPDATE_KEY = "update";

    public static final long STALE_TIMEOUT = 500;

    private static int updateCount = 0;
    private static boolean listenerRegistered = false;
    private static long lastUpdateTime;

    public static void update() {
        VISION_RESULTS.putNumber(UPDATE_KEY, ++updateCount);
    }

    public static boolean isStale() {
        if (!listenerRegistered) {
            VISION_RESULTS.addTableListener(UPDATE_KEY, (ITable source, String key, Object value, boolean isNew) -> {
                lastUpdateTime = System.currentTimeMillis();
            } , false);
            listenerRegistered = true;
        }

        return System.currentTimeMillis() - lastUpdateTime > STALE_TIMEOUT;
    }

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

    public static double getGoalDistance() {
        return VISION_RESULTS.getNumber(GOAL_DISTANCE_KEY, 0);
    }

    public static void setGoalDistance(double distance) {
        VISION_RESULTS.putNumber(GOAL_DISTANCE_KEY, distance);
    }

    private VisionResults() {
    }
}
