/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision;

import java.util.ArrayList;

import org.opencv.core.Mat;
import org.usfirst.frc.team2084.CMonster2016.vision.capture.CameraCapture;

/**
 * @author Ben Wolsieffer
 */
public class VisionRunner {

    private final ArrayList<ImageHandler> imageHandlers = new ArrayList<>(1);

    protected final VisionProcessor processor;
    protected final CameraCapture camera;
    private final Thread processorThread = new Thread(new ProcessorThread());
    private final Mat cameraImage = new Mat();
    private boolean running = false;

    /**
     * Thread that runs the OpenCV processing of the camera.
     */
    private class ProcessorThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                if (running) {
                    camera.capture(cameraImage);
                    processor.process(cameraImage);
                    imageHandlers.forEach((handler) -> handler.imageProcessed(cameraImage));
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

    /**
     * 
     */
    public VisionRunner(VisionProcessor processor, CameraCapture camera) {
        this.processor = processor;
        this.camera = camera;
    }

    public void start() {
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
}
