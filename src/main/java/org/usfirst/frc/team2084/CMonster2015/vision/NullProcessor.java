/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2015.vision;

import org.opencv.core.Mat;
import org.usfirst.frc.team2084.CMonster2015.vision.capture.CameraCapture;

/**
 * A vision processor that does nothing. Originally created to debug performance
 * problems in camera capture code.
 * 
 * @author Ben Wolsieffer
 */
public class NullProcessor extends VisionProcessor {

    /**
     * @param capture
     */
    public NullProcessor(CameraCapture capture) {
        super(capture);
    }

    @Override
    protected void process(Mat cameraImage, Mat outputImage) {
        // No-op
    }

}
