/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision;

import org.opencv.core.Mat;

/**
 * Vision processor that runs the algorithm in a separate thread. This alows the
 * camera to stream as fast as possible.
 * 
 * @author Ben Wolsieffer
 */
public abstract class ThreadedVisionProcessor extends VisionProcessor {

    private final boolean runInBackground;

    /**
     * @param runInBackground if false, do not enable background thread for
     *        debugging
     */
    public ThreadedVisionProcessor(final boolean runInBackground) {
        this.runInBackground = runInBackground;
        if (runInBackground) {
            new Thread(new ProcessingThread()).start();
        }
    }

    private class ProcessingThread implements Runnable {

        private final Mat localImage = new Mat();

        @Override
        public void run() {
            while (true) {
                boolean localNewImage = false;
                synchronized (image) {
                    try {
                        image.wait();
                    } catch (InterruptedException e) {
                    }
                    if (newImage) {
                        newImage = false;
                        localNewImage = true;
                        image.copyTo(localImage);
                    }
                }
                if (localNewImage) {
                    backgroundProcess(localImage);
                }
            }
        }
    }

    private volatile boolean newImage;
    private final Mat image = new Mat();

    /**
     * @param image
     */
    @Override
    public final void process(Mat image) {
        preProcess(image);

        if (runInBackground) {
            // Copy the image for the processing thread
            synchronized (this.image) {
                image.copyTo(this.image);
                newImage = true;
                this.image.notify();
            }
        } else {
            backgroundProcess(image);
        }

        postProcess(image);
    }

    /**
     * Do the processing of the image in a separate thread.
     * 
     * @param image the image to process
     */
    public abstract void backgroundProcess(Mat image);

    /**
     * Processing that happens in the streaming thread before the image is
     * handed to the background thread.
     * 
     * @param image the image to process
     */
    public abstract void preProcess(Mat image);

    /**
     * Processing that happens in the streaming thread after the image is handed
     * to the background thread.
     * 
     * @param image the image to process
     */
    public abstract void postProcess(Mat image);

}
