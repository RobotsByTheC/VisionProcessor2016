/* 
 * Copyright (c) 2014 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision;

import java.util.ArrayList;

import org.opencv.core.Mat;

/**
 * Provides easy access to the vision processing algorithm. All the public
 * methods are thread safe.
 * 
 * @author Ben Wolsieffer
 */
public abstract class VisionProcessor {

    @FunctionalInterface
    public interface DebugHandler {

        public void debugImage(String name, Mat image);
    }

    private final ArrayList<DebugHandler> debugHandlers = new ArrayList<>(1);

    /**
     * 
     */
    public VisionProcessor() {
    }

    public void addDebugHandler(DebugHandler handler) {
        debugHandlers.add(handler);
    }

    public void removeDebugHandler(DebugHandler handler) {
        debugHandlers.remove(handler);
    }

    protected void debugImage(String name, Mat image) {
        debugHandlers.forEach((handler) -> handler.debugImage(name, image));
    }

    /**
     * Called from the processing thread, this method should be overridden by
     * subclasses to implement their algorithm.
     * 
     * @param image the image retrieved from the camera
     * @param outputImage the image which to draw output onto, it starts out the
     *        same as the camera image
     */
    public abstract void process(Mat image);

}
