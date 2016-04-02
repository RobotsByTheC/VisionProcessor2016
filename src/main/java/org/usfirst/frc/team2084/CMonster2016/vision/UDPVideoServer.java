/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * Serves a Motion JPEG image over HTTP. It supports multiple simultaneous
 * clients and can be started and stopped repeatedly.
 * 
 * @author Ben Wolsieffer
 */
public class UDPVideoServer {

    public static final int PORT = 5802;
    
    /**
     * Matrix that hold the JPEG quality parameters.
     */
    private final MatOfInt qualityParams;

    /**
     * Buffer that holds the JPEG data.
     */
    private final MatOfByte compressionBuffer = new MatOfByte();

    /**
     * Buffer that holds the same data as {@link #compressionBuffer}, but as a
     * Java data type.
     */
    private byte[] socketBuffer = new byte[0];

    /**
     * Flag indicating that the server is running.
     */
    private boolean running;

    private final DatagramSocket socket;
    private DatagramPacket packet;

    /**
     * Creates a new {@link UDPVideoServer} that listens on the specified port
     * and uses the specified quality level for the video stream.
     * 
     * @param port the port the listen on
     * @param quality the quality of the JPEG stream
     * 
     * @throws IOException if the socket could not be opened
     * 
     */
    public UDPVideoServer(int quality) throws IOException {

        qualityParams = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, quality);

        socket = new DatagramSocket();
        packet = new DatagramPacket(socketBuffer, socketBuffer.length);
    }

    /**
     * Creates a new {@link UDPVideoServer} that listens on the specified port
     * and the default quality level.
     * 
     * @param port the port the listen on
     * 
     * @throws IOException if the socket could not be opened
     */
    public UDPVideoServer() throws IOException {
        this(95);
    }

    /**
     * Starts listening for connections to the server
     * 
     * @throws IOException if the server socket cannot be allocated
     */
    public void start() throws IOException {
        // Do nothing if already running.
        if (!running) {
            running = true;
        }
    }

    /**
     * Stops the server.
     * 
     * @throws IOException if an error occurs while closing the socket
     */
    public void stop() throws IOException {
        // Do nothing if the server is not running.
        if (running) {
            running = false;
        }
    }

    private String oldIP = "";
    
    /**
     * Sends an image to all connected clients. This blocks until the image is
     * sent.
     * 
     * @param image the image to send
     * @throws IOException if the image could not be sent
     */
    public void sendImage(Mat image) throws IOException {
        // Only send if server is running.

        if (running) {
            // Encode the image as a JPEG.
            Imgcodecs.imencode(".jpg", image, compressionBuffer, qualityParams);
            int size = (int) compressionBuffer.total() * compressionBuffer.channels();
            
            String ip = VisionParameters.getStreamIP();
            
            // Resize the Java buffer to fit the data if necessary
            if (size > socketBuffer.length) {
                socketBuffer = new byte[size];
                packet = new DatagramPacket(socketBuffer, size, InetAddress.getByName(ip), PORT);
            }
            // Copy the OpenCV data to a Java byte[].
            compressionBuffer.get(0, 0, socketBuffer);

            if(!ip.equals(oldIP)) {
                oldIP = ip;
                packet.setAddress(InetAddress.getByName(ip));
            }
            packet.setLength(size);
            socket.send(packet);
        } else {
            throw new IllegalStateException("Server must be running to send an image.");
        }
    }

    public int getQuality() {
        return (int) qualityParams.get(1, 0)[0];
    }

    public void setQuality(int quality) {
        qualityParams.put(1, 0, quality);
    }

    /**
     * Gets whether the server is running.
     * 
     * @return true if the server is running
     */
    public boolean isRunning() {
        return running;
    }
}
