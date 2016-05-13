/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package com.kauailabs.navx.desktop;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;

/**
 * @author Ben Wolsieffer
 */
public class SmartDashboard {

    private static final ITable TABLE = NetworkTable.getTable("SmartDashboard");

    public static void putNumber(String key, double value) {
        TABLE.putNumber(key, value);
    }

    public static void putString(String key, String value) {
        TABLE.putString(key, value);
    }
}
