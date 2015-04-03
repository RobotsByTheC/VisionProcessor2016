/* 
 * Copyright (c) 2015 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2015.vision;

/**
 * @author Ben Wolsieffer
 */
public class YellowToteTarget implements Target {

    private final double x;
    private final double y;

    private final double aspectRatio;

    public YellowToteTarget(double x, double y, double aspectRatio) {
        this.x = x;
        this.y = y;
        this.aspectRatio = aspectRatio;
    }

    /**
     * @return the x coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * @return the y coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * @return the aspect ratio
     */
    public double getAspectRatio() {
        return aspectRatio;
    }
}
