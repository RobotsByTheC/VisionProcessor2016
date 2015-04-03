/* 
 * Copyright (c) 2014 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2015.vision.capture;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

/**
 * Captures images from a camera asynchronously. This allows applications to
 * work at any frame rate without worrying about what the camera supports. The
 * image returned from {@link #capture()} is always the latest one and ones in
 * between are skipped.
 * 
 * @author Ben Wolsieffer
 */
public class CameraCapture {

	/**
	 * Implementation of the thread that does the actually capturing of the
	 * image from the camera.
	 */
	private class CaptureThread implements Runnable {

		@Override
		public void run() {
			while (true) {
				// If the camera is running, capture an image.
				if (running) {
					synchronized (capture) {
						// Grab the image outside of the image synchronization.
						capture.grab();
						synchronized (image) {
							// Copy grabbed image
							if (!capture.retrieve(image)) {
								// If it failed, store an exception to be
								// thrown.
								if (filename == null) {
									cameraReadException = new CameraReadException(device);
								} else {
									cameraReadException = new CameraReadException(filename);
								}
							} else {
								// If it succeeded, set the newImage flag.
								newImage = true;
							}
						}
					}
					// Notify the capture() method in case it was waiting.
					synchronized (imageNotifier) {
						imageNotifier.notify();
					}
				} else {
					// Otherwise, wait to be notified.
					synchronized (captureThread) {
						try {
							captureThread.wait();
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}
	}

	private static final int CV_CAP_PROP_FPS = 5;

	/**
	 * Device number, if this is a local camera, otherwise it should be -1} .
	 */
	private int device = -1;
	/**
	 * Fileanem of video file or stream, null} if using a local camera.
	 */
	private String filename;
	/**
	 * Running flag.
	 */
	private volatile boolean running = false;
	/**
	 * The latest captured image.
	 */
	private final Mat image = new Mat();
	/**
	 * Flag that signals that a new image has been captured and not yet
	 * retrieved.
	 */
	private volatile boolean newImage = false;
	/**
	 * {@link VideoCapture} object that does the actual capturing from the
	 * camera.
	 */
	private final VideoCapture capture = new VideoCapture();
	/**
	 * The thread that runs the video capturing.
	 */
	private final Thread captureThread = new Thread(new CaptureThread(), "Camera Capture Thread");
	/**
	 * Object that tells the waiting {@link #capture(Mat)} that a new image
	 * arrived.
	 */
	private final Object imageNotifier = new Object();
	/**
	 * Stores a {@link CameraReadException} that occurred in the capture thread.
	 * This is then thrown by the {@link #capture(Mat)}.
	 */
	private volatile CameraReadException cameraReadException;

	// Properties
	private Size resolution = null;
	private double fps = -1;

	/**
	 * Captures from the device specified by the parameter.
	 * 
	 * @param device the camera device number
	 */
	public CameraCapture(int device) {
		this.device = device;
	}

	/**
	 * Captures from the specified file. This could be a video file or a stream
	 * from a camera. This accepts all the formats {@link VideoCapture} does.
	 * 
	 * @see VideoCapture
	 * 
	 * @param filename
	 */
	public CameraCapture(String filename) {
		this.filename = filename;
	}

	/**
	 * Starts capturing from the camera in the background.
	 * 
	 * @throws CameraOpenException if the camera fails to open.
	 */
	public void start() throws CameraOpenException {
		// Do nothing if already running.
		if (!running) {
			// Open either a device number or filename.
			if (filename == null) {
				if (!capture.open(device)) {
					// If camera cannot be opened, throw an exception.
					throw new CameraOpenException(device);
				}
			} else {
				if (!capture.open(filename)) {
					// If camera cannot be opened, throw an exception.
					throw new CameraOpenException(filename);
				}
			}
			running = true;
			// Set the properties of the camera.
			setResolution(resolution);
			setFPS(fps);
			// If the thread has already been started, tell it to resume.
			if (captureThread.isAlive()) {
				synchronized (captureThread) {
					captureThread.notify();
				}
			} else {
				// Otherwise, start it.
				captureThread.start();
			}
		}
	}

	public void stop() {
		// Do nothing if the capture is not running.
		if (running) {
			running = false;
			// Release the camera.
			synchronized (image) {
				capture.release();
			}
		}
	}

	/**
	 * Gets whether the camera capture is running.
	 * 
	 * @return true if the capture is running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Gets the latest image captured from the camera.
	 * 
	 * @param image a {@link Mat} to copy the captured image into
	 * @throws CameraReadException if the camera could be read from the last
	 *             time it was attempted.
	 */
	public void capture(Mat image) throws CameraReadException {
		if (running) {
			// If the last capture attempt failed, throw its exception.
			if (cameraReadException != null) {
				CameraReadException cameraReadExceptionTemp = cameraReadException;
				cameraReadException = null;
				throw cameraReadExceptionTemp;
			}

			// Wait for an image from the camera
			while (!newImage) {
				// The capture thread calls imageNotifier.notify() when it grabs
				// and image.
				synchronized (imageNotifier) {
					try {
						imageNotifier.wait();
					} catch (InterruptedException e) {
					}
				}
			}

			// Copy the new image to the parameter. This is synchronized to
			// avoid possibly returning a half grabbed image, even though this
			// is very unlikely.
			synchronized (this.image) {
				newImage = false;
				this.image.copyTo(image);
			}
		} else {
			// Throw an exception if the capture is not running.
			throw new IllegalStateException("Camera must be running to capture an image.");
		}
	}

	/**
	 * Get the resolution of the camera or video stream.
	 * 
	 * @return the resolution of the camera or video stream.
	 */
	public Size getResolution() {
		if (running) {
			// Update the resolution from the camera. This won't work if the
			// camera is not opened, so its in an if statement.
			synchronized (capture) {
				double width = capture.get(Highgui.CV_CAP_PROP_FRAME_WIDTH);
				double height = capture.get(Highgui.CV_CAP_PROP_FRAME_HEIGHT);
				resolution = new Size(width, height);
			}
		}
		return resolution;
	}

	/**
	 * Set the resolution of the camera. This only works with resolutions the
	 * camera supports. This probably won't work on files. A message is printed
	 * if it fails.
	 * 
	 * @param resolution the resolution of the camera
	 */
	public void setResolution(Size resolution) {
		this.resolution = resolution;

		// If the camera is running and the resolution is valid, try to set the
		// camera settings.
		if (running && resolution != null) {
			synchronized (capture) {
				capture.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, resolution.width);
				capture.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, resolution.height);
				// If the values did not change, it failed, so print a message.
				// The set() method is supposed to return false when it fails,
				// but it doesn't work reliably.
				if (capture.get(Highgui.CV_CAP_PROP_FRAME_WIDTH) != resolution.width) {
					System.out.println("Warning: Failed to set frame width.");
				}
				if (capture.get(Highgui.CV_CAP_PROP_FRAME_HEIGHT) != resolution.height) {
					System.out.println("Warning: Failed to set frame height.");
				}
			}
		}
	}

	/**
	 * Gets the frame rate of the video stream. This method usually doesn't work
	 * for cameras. If the device doesn't support it, this method should return
	 * -1.
	 * 
	 * @return the frame rate
	 */
	public double getFPS() {
		if (running) {
			synchronized (capture) {
				fps = capture.get(CV_CAP_PROP_FPS);
				if (fps <= 0) {
					fps = -1;
				}
			}
		}
		return fps;
	}

	/**
	 * Tries to set the maximum frames per second to capture from the camera.
	 * 
	 * @param fps the number of frames per second
	 */
	public void setFPS(double fps) {
		this.fps = fps;
		// If the fps is invalid, make it -1.
		if (fps <= 0) {
			this.fps = -1;
		}
		// If the fps is valid and the camera is running, change the camera
		// settings.
		if (running && fps > 0) {
			synchronized (capture) {
				capture.set(CV_CAP_PROP_FPS, fps);
			}
		}
	}
}
