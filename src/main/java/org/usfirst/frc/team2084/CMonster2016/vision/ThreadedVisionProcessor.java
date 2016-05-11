/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision;

import org.opencv.core.Mat;

/**
 * @author Ben Wolsieffer
 */
public abstract class ThreadedVisionProcessor extends VisionProcessor {

    private final boolean runInBackground;

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

    public abstract void backgroundProcess(Mat image);

    public abstract void preProcess(Mat image);

    public abstract void postProcess(Mat image);

}
