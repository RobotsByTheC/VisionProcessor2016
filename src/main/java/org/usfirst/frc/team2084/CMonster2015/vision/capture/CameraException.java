/* 
 * Copyright (c) 2014 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2015.vision.capture;

/**
 * 
 * Class representing any type of {@link Exception} in capturing from the
 * camera.
 *
 * @author Ben Wolsieffer
 */
@SuppressWarnings("serial")
public class CameraException extends Exception {

	/**
	 * Creates a {@link CameraException} with the specified message.
	 * 
	 * @param message the message
	 */
	public CameraException(String message) {
		super(message);
	}

	/**
	 * Creates an unknown camera error.
	 */
	public CameraException() {
		super("Unknown camera error");
	}
}
