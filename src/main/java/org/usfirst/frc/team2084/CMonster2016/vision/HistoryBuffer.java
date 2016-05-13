/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision;

import com.kauailabs.navx.desktop.Timer;

/**
 * @author Ben Wolsieffer
 */
public class HistoryBuffer {

    private final double[][] buffer;
    private final double estimatedFrequency;

    private int start = 0;
    private int filledLength = 0;

    public HistoryBuffer(int length, double estimatedFrequency) {
        buffer = new double[length][2];
        this.estimatedFrequency = estimatedFrequency;
    }

    public void newValue(double time, double value) {
        if (filledLength < buffer.length) {
            buffer[filledLength][0] = time;
            buffer[filledLength][1] = value;
            ++filledLength;

        } else {
            if (start >= filledLength) {
                start = 0;
            }
            buffer[start][0] = time;
            buffer[start][1] = value;
            ++start;
        }
    }

    private int getIndex(int position) {
        if (position >= filledLength) {
            throw new IndexOutOfBoundsException("Buffer position: " + position + " out of bounds.");
        } else {
            int index = start + position;
            if (index >= buffer.length) {
                index -= buffer.length;
            }
            return index;
        }
    }

    private double getElement(int position, int column) {
        return getElement(position)[column];
    }

    private double[] getElement(int position) {
        return buffer[getIndex(position)];
    }

    public double getValue(double time) {
        if (filledLength == 0) {
            return 0;
        } else if (time <= getElement(0, 0)) {
            return getElement(0, 1);
        } else if (time >= getElement(filledLength - 1, 0)) {
            return getElement(filledLength - 1, 1);
        } else {
            double currTime = Timer.getFPGATimestamp();
            double deltaTime = currTime - time;
            int r = filledLength - (int) (deltaTime * estimatedFrequency);
            if (r < 0) {
                r = 0;
            } else if (r >= filledLength) {
                r = filledLength - 1;
            }
            while (r < filledLength - 1 && getElement(r, 0) < time) {
                r++;
            }
            while (r > 0 && getElement(r, 0) >= time) {
                r--;
            }

            double[] oldCal = getElement(r + 1);
            double[] newCal = getElement(r);

            double slope = (newCal[1] - oldCal[1]) / (newCal[0] - oldCal[0]);
            return slope * (time - oldCal[0]) + oldCal[1];
        }
    }
}
