/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2015.vision;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;

/**
 * @author Ben Wolsieffer
 */
public class VisionResults {

    public static final ITable VISION_RESULTS = NetworkTable.getTable("Vision").getSubTable(
            "Results");
    
    public static final String TARGET_TABLE_STATE_KEY = "goal_hot";
    public static final String TARGET_TABLE_AUTONOMOUS_VISION_RUNNING_KEY = "auto_vision";
    public static final String TARGET_TABLE_ENABLE_CAMERA_KEY = "enable_camera";

    public enum State {
        HOT,
        NOT_HOT,
        UNKNOWN;
    }

    public static void setState(State state) {
        VISION_RESULTS.putNumber(TARGET_TABLE_STATE_KEY, state.ordinal());
    }

    public static State getState() {
        return State.values()[(int) VISION_RESULTS.getNumber(TARGET_TABLE_STATE_KEY, State.UNKNOWN.ordinal())];
    }

    public static boolean isAutonomousVisionRunning() {
        return VISION_RESULTS.getBoolean(TARGET_TABLE_AUTONOMOUS_VISION_RUNNING_KEY, false);
    }

    public static void setAutonomousVisionRunning(boolean started) {
        VISION_RESULTS.putBoolean(TARGET_TABLE_AUTONOMOUS_VISION_RUNNING_KEY, started);
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
