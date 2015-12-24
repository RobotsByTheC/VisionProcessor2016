/* 
 * Copyright (c) 2015 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2015.vision;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * @author ben
 */
public final class ImageConvertor {

    public ImageConvertor() {
    }

    private BufferedImage javaImage;

    public BufferedImage toBufferedImage(Mat image) {
        int width = image.width();
        int height = image.height();
        int type = image.type();
        // Get BufferedImage type
        int javaType = toJavaImageType(type);
        // If the Mat does not match the BufferedImage, create a new one.
        if (javaImage == null || width != javaImage.getWidth() || height != javaImage.getHeight()
                || javaType != javaImage.getType()) {
            javaImage = new BufferedImage(width, height, javaType);
        }
        // Copy Mat data to BufferedImage
        image.get(0, 0, ((DataBufferByte) javaImage.getRaster().getDataBuffer()).getData());
        return javaImage;
    }

    /**
     * Convert an {@link Mat} type code to a {@link BufferedImage} type. This
     * assumes a BGR or grayscale color model for the {@link Mat} and only works
     * on three or one channel unsigned byte {@link Mat}s.
     * 
     * @param cvImageType the {@link Mat} type
     * @return the {@link BufferedImage} type
     */
    private static int toJavaImageType(int cvImageType) {
        if (cvImageType == CvType.CV_8UC3) {
            return BufferedImage.TYPE_3BYTE_BGR;
        } else if (cvImageType == CvType.CV_8UC1) {
            return BufferedImage.TYPE_BYTE_GRAY;
        } else {
            return BufferedImage.TYPE_CUSTOM;
        }
    }
}
