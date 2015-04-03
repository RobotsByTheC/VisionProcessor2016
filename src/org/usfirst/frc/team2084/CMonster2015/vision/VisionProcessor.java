/* 
 * Copyright (c) 2014 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2015.vision;

import java.util.ArrayList;

import org.opencv.core.Mat;
import org.usfirst.frc.team2084.CMonster2015.vision.capture.CameraCapture;
import org.usfirst.frc.team2084.CMonster2015.vision.capture.CameraOpenException;
import org.usfirst.frc.team2084.CMonster2015.vision.capture.CameraReadException;

/**
 * Provides easy access to the vision processing algorithm. All the public
 * methods are thread safe.
 * 
 * @author Ben Wolsieffer
 */
public abstract class VisionProcessor {

	/**
	 * Thread that runs the OpenCV processing of the camera.
	 */
	private class ProcessorThread implements Runnable {

		@Override
		public void run() {
			while (true) {
				if (running) {
					try {
						camera.capture(cameraImage);
						cameraImage.copyTo(processedImage);
						process(cameraImage, processedImage);
						imageHandlers.forEach((handler) -> handler.imageProcessed(processedImage));
					} catch (CameraReadException e) {
						System.err.println(e);
					}
				} else {
					synchronized (processorThread) {
						try {
							processorThread.wait();
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}
	}

	private final ArrayList<ImageHandler> imageHandlers = new ArrayList<>(1);
	private final CameraCapture camera;
	private final Thread processorThread = new Thread(new ProcessorThread());
	private final Mat cameraImage = new Mat();
	private final Mat processedImage = new Mat();
	private boolean running = false;

	/**
	 * 
	 */
	public VisionProcessor(CameraCapture capture) {
		this.camera = capture;
	}

	public void start() throws CameraOpenException {
		camera.start();
		running = true;
		if (processorThread.isAlive()) {
			synchronized (processorThread) {
				processorThread.notifyAll();
			}
		} else {
			processorThread.start();
		}
	}

	public void stop() {
		running = false;
		camera.stop();
	}

	public void addImageHandler(ImageHandler handler) {
		imageHandlers.add(handler);
	}

	public void removeImageHandler(ImageHandler handler) {
		imageHandlers.remove(handler);
	}

	protected void debugImage(String name, Mat image) {
		imageHandlers.forEach((handler) -> handler.debugImage(name, image));
	}

	/**
	 * Called from the processing thread, this method should be overridden by
	 * subclasses to implement their algorithm.
	 * 
	 * @param cameraImage the image retrieved from the camera
	 * @param outputImage the image which to draw output onto, it starts out the
	 *            same as the camera image
	 */
	protected abstract void process(Mat cameraImage, Mat outputImage);
}
