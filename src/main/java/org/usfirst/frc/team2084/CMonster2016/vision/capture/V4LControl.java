/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision.capture;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Allows control of V4L parameters that are not exposed through OpenCV.
 * 
 * @author Ben Wolsieffer
 */
public class V4LControl {

    private final BlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();

    private int index;

    private boolean autoExposure = false;
    private int exposure = 50;

    private class ProcessThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Runtime.getRuntime().exec(commandQueue.take()).waitFor();
                } catch (IOException | InterruptedException e) {
                }
            }
        }
    }

    public V4LControl(int index) {
        this.index = index;

        new Thread(new ProcessThread()).start();
    }

    public void enableAutoExposure(boolean enabled) {
        if (enabled != autoExposure) {
            autoExposure = enabled;
            setParameter("exposure_auto", enabled ? "3" : "1");
            if (!enabled) {
                setExposure(exposure++);
            }
        }
    }

    public void setExposure(int exposure) {
        if (exposure != this.exposure) {
            this.exposure = exposure;
            setParameter("exposure_absolute", Integer.toString(exposure));
        }
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    private void setParameter(String parameter, String value) {
        String command = "v4l2-ctl -d " + Integer.toString(index) + " -c " + parameter + "=" + value;
        commandQueue.offer(command);
    }

}
