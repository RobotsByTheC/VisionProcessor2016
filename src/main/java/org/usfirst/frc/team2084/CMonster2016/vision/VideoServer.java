/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

/**
 * Serves a Motion JPEG image over HTTP. It supports multiple simultaneous
 * clients and can be started and stopped repeatedly. We stopped using this
 * because of bandwidth problems on the field. {@link UDPVideoServer} works much
 * better.
 * 
 * @author Ben Wolsieffer
 */
public class VideoServer {

    private class HTTPServer extends NanoHTTPD {

        public HTTPServer() {
            super(port);
        }

        /**
         * @param session
         * @return
         */
        @Override
        public Response serve(IHTTPSession session) {
            Response res = NanoHTTPD.newChunkedResponse(Status.OK,
                    "multipart/x-mixed-replace; boundary=" + BOUNDARY_KEY, new VideoInputStream());

            res.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0");
            res.addHeader("Cache-Control", "private");
            res.addHeader("Pragma", "no-cache");
            res.addHeader("Expires", "-1");

            return res;
        }
    }

    private class VideoInputStream extends InputStream {

        public ByteBuffer responseBuffer;
        public long lastImageIndex = 0;

        /**
         * @return
         * @throws IOException
         */
        @Override
        public int read() throws IOException {
            updateBuffer();
            return responseBuffer.get();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            updateBuffer();

            if (len > responseBuffer.remaining()) {
                len = responseBuffer.remaining();
            }
            responseBuffer.get(b, off, len);

            return len;
        }

        private void updateBuffer() {
            if (responseBuffer == null) {
                flipBuffer();
            } else {
                if (!responseBuffer.hasRemaining()) {
                    while (lastImageIndex == imageIndex) {
                        synchronized (responseBufferOutputStream) {
                            try {
                                responseBufferOutputStream.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                    flipBuffer();
                }
            }
        }

        private void flipBuffer() {
            synchronized (responseBufferOutputStream) {
                responseBuffer = ByteBuffer.wrap(responseBufferOutputStream.toByteArray());
                lastImageIndex = imageIndex;
            }
        }

        @Override
        public int available() throws IOException {
            updateBuffer();
            return responseBuffer.remaining();
        }
    }

    /**
     * The content type that is sent to the client before each image.
     */
    private static final byte[] CONTENT_TYPE = "Content-Type: image/jpeg\r\n".getBytes();
    /**
     * The boundary that is sent between each image.
     */
    private static final String BOUNDARY_KEY = "jpgbound";

    private static final byte[] BOUNDARY = ("\r\n--" + BOUNDARY_KEY + "\r\n").getBytes();

    /**
     * TCP port the server is listens on.
     */
    private final int port;
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

    private NakedByteArrayOutputStream responseBufferOutputStream = new NakedByteArrayOutputStream();
    private long imageIndex = 0;

    private final HTTPServer server;

    /**
     * Creates a new {@link VideoServer} that listens on the specified port and
     * uses the specified quality level for the video stream.
     * 
     * @param port the port the listen on
     * @param quality the quality of the JPEG stream
     * 
     * @throws IOException if the socket could not be opened
     * 
     */
    public VideoServer(int port, int quality) throws IOException {
        this.port = port;
        responseBufferOutputStream.write(BOUNDARY);
        responseBufferOutputStream.write(CONTENT_TYPE);
        responseBufferOutputStream.mark();

        qualityParams = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, quality);

        server = new HTTPServer();
    }

    /**
     * Creates a new {@link VideoServer} that listens on the specified port and
     * the deafult quality level.
     * 
     * @param port the port the listen on
     * 
     * @throws IOException if the socket could not be opened
     */
    public VideoServer(int port) throws IOException {
        this(port, 95);
    }

    /**
     * Starts listening for connections to the server
     * 
     * @throws IOException if the server socket cannot be allocated
     */
    public void start() throws IOException {
        // Do nothing if already running.
        if (!running) {
            server.start();

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
            server.stop();

            running = false;
        }
    }

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
            // Resize the Java buffer to fit the data if necessary
            if (size > socketBuffer.length) {
                socketBuffer = new byte[size];
            }
            // Copy the OpenCV data to a Java byte[].
            compressionBuffer.get(0, 0, socketBuffer);

            synchronized (responseBufferOutputStream) {
                ++imageIndex;

                responseBufferOutputStream.reset();

                // Reset response buffer to the end of the header
                responseBufferOutputStream.markReset();
                // Write content length
                responseBufferOutputStream.write(("Content-Length: " + size + "\r\n\r\n").getBytes());
                // Write image to response buffer
                responseBufferOutputStream.write(socketBuffer, 0, size);

                responseBufferOutputStream.notifyAll();
            }

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

    /**
     * Gets the port number the server is listening on.
     * 
     * @return the port
     */
    public int getPort() {
        return port;
    }
}
