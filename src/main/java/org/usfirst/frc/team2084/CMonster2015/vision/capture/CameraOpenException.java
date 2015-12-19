/* 
 * Copyright (c) 2014 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2015.vision.capture;

/**
 * Represents an exception that occurred while opening the camera stream.
 * 
 * @author Ben Wolsieffer
 */
@SuppressWarnings("serial")
public class CameraOpenException extends CameraException {

	/**
	 * Create a {@link CameraOpenException} for the specified device.
	 * 
	 * @param device the camera that cannot be opened
	 */
	public CameraOpenException(int device) {
		super("Unable to open stream from camera " + device);
	}

	/**
	 * Create a {@link CameraOpenException} for the specified camera or file.
	 * 
	 * @param filename the camera that cannot be opened
	 */
	public CameraOpenException(String filename) {
		super("Unable to open stream from file or camera: " + filename);
	}
}
