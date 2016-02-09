/* 
 * Copyright (c) 2014 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision.capture;

/**
 * Represents an exception that occurred while reading from the camera.
 * 
 * @author Ben Wolsieffer
 */
@SuppressWarnings("serial")
public class CameraReadException extends CameraException {

    /**
     * Create a {@link CameraReadException} for the specified device.
     * 
     * @param device the camera that cannot be read
     */
    public CameraReadException(int device) {
        super("Unable to read from camera " + device);
    }

    /**
     * Create a {@link CameraReadException} for the specified camera or file.
     * 
     * @param filename the camera or file that cannot be read
     */
    public CameraReadException(String filename) {
        super("Unable to read from camera or file " + filename);
    }
}
