/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision;

import com.kauailabs.navx.frc.Timer;

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
            buffer[filledLength][0] = value;
            buffer[filledLength][1] = value;
            ++filledLength;

        } else {
            ++start;
            if (start >= filledLength) {
                start = 0;
            }
            int index = getIndex(0);
            buffer[index][0] = time;
            buffer[index][1] = value;
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
        return buffer[getIndex(position)][column];
    }

    public double getValue(double time) {
        if (filledLength == 0) {
            return 0;
        } else if (time >= getElement(0, 0)) {
            return getElement(0, 1);
        } else if (time <= getElement(filledLength - 1, 0)) {
            return getElement(filledLength - 1, 1);
        } else {
            double currTime = Timer.getFPGATimestamp();
            double deltaTime = currTime - time;
            int r = (int) (deltaTime * estimatedFrequency);
            while (getElement(r, 0) > time) {
                r++;
            }
            while (getElement(r, 0) < time) {
                r--;
            }

            double[] oldCal = buffer[r + 1];
            double[] newCal = buffer[r];

            double slope = (newCal[1] - oldCal[1]) / (newCal[0] - oldCal[0]);
            return slope * (time - oldCal[0]) + oldCal[1];
        }
    }
}
