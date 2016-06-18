/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision;

/**
 * Class that keeps track of framerate using a weighted average.
 * 
 * @author Ben Wolsieffer
 */
public class FramerateCounter {

    private static final double ALPHA = 0.9;

    private long lastTime = System.nanoTime();
    private double averageFps = 1;

    public FramerateCounter() {
    }

    public double update() {
        long currTime = System.nanoTime();
        long elapsedTime = currTime - lastTime;
        lastTime = currTime;

        return averageFps = ALPHA * averageFps + (1.0 - ALPHA) * (1_000_000_000.0 / elapsedTime);
    }

    public double getAverageFps() {
        return averageFps;
    }
}
