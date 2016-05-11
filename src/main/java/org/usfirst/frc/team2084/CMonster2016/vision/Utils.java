/* 
 * Copyright (c) 2014 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Utility methods.
 *
 * @author Ben Wolsieffer
 */
public final class Utils {

    public static final class Color {

        private Color() {
        }

        public static final Scalar WHITE = Scalar.all(255);
        public static final Scalar BLACK = Scalar.all(0);
        public static final Scalar RED = new Scalar(0, 0, 255);
        public static final Scalar GREEN = new Scalar(0, 255, 0);
        public static final Scalar BLUE = new Scalar(255, 0, 0);

    }

    public static final Scalar TEXT_COLOR = Color.WHITE;
    public static final double TEXT_SIZE = 1.5;

    private Utils() {
    }

    /**
     * Converts a ratio with an ideal value of 1 to a 0-100 score value using a
     * piecewise linear function that goes from (0,0) to (1,100) to (2,0).
     *
     * @param ratio a ratio with a value between 0-2 with and ideal value of 1
     * @return the score between 0-100
     */
    public static double ratioToScore(double ratio) {
        return (Math.max(0, Math.min(100 * (1 - Math.abs(1 - ratio)), 100)));
    }

    public static void drawText(Mat image, String text, double x, double y) {
        Imgproc.putText(image, text, new Point(x, y), Core.FONT_HERSHEY_PLAIN, TEXT_SIZE, TEXT_COLOR);
    }

    public static void drawText(Mat image, String text, double x, double y, double textSize) {
        Imgproc.putText(image, text, new Point(x, y), Core.FONT_HERSHEY_PLAIN, textSize, TEXT_COLOR);
    }

    public static void drawText(Mat image, String text, double x, double y, Scalar textColor) {
        Imgproc.putText(image, text, new Point(x, y), Core.FONT_HERSHEY_PLAIN, TEXT_SIZE, textColor);
    }

    public static void drawText(Mat image, String text, double x, double y, double textSize, Scalar textColor) {
        Imgproc.putText(image, text, new Point(x, y), Core.FONT_HERSHEY_PLAIN, textSize, textColor);
    }

    public static byte[] longToBytes(long i) {
        byte[] result = new byte[8];
        longToBytes(i, result);
        return result;
    }

    public static byte[] longToBytes(long l, byte[] result) {
        for (int i = Long.BYTES - 1; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= Byte.SIZE;
        }
        return result;
    }

    public static long bytesToLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < Long.BYTES; i++) {
            result <<= Byte.SIZE;
            result |= (b[i] & 0xFF);
        }
        return result;
    }
}
