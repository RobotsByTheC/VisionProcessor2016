/* 
 * Copyright (c) 2014 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision;

/**
 * Utility methods for calculating scores. Only contains one method right now.
 *
 * @author Ben Wolsieffer
 */
public final class ScoreUtils {

    private ScoreUtils() {
    }

    /**
     * Converts a ratio with an ideal value of 1 to a 0-100 score value using a
     * piecewise linear function that goes from (0,0) to (1,100) to (2,0).
     *
     * @param ratio a ratio with a value between 0-2 with and ideal value of 1
     * @return the score between 0-100
     */
    public static double ratioToScore(double ratio) {
        return (Math.max(0, Math.min(100 * (1 - Math.abs(1 - ratio)), 100)));
    }
}
