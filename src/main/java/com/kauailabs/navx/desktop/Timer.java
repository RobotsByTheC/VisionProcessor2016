/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package com.kauailabs.navx.desktop;

import java.util.concurrent.locks.LockSupport;

/**
 * @author ben
 */
public class Timer {

    public static final double getFPGATimestamp() {
        return System.currentTimeMillis() / 1000.0;
    }

    public static final void delay(double time) {
        LockSupport.parkNanos((long) (time * 1.0e9));
    }
}
