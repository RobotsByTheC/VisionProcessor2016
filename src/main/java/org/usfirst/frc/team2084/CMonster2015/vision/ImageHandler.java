/* 
 * Copyright (c) 2014 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2015.vision;

import org.opencv.core.Mat;

/**
 * @author Ben Wolsieffer
 */
@FunctionalInterface
public interface ImageHandler {

    public void imageProcessed(Mat image);

    public default void debugImage(String name, Mat image) {
    }
}
