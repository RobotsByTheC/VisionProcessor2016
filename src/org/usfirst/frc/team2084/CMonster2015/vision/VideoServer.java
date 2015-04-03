/* 
 * Copyright (c) 2014 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2015.vision;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.highgui.Highgui;

/**
 * Serves a Motion JPEG image over HTTP. It supports multiple simultaneous
 * clients and can be started and stopped repeatedly.
 * 
 * @author Ben Wolsieffer
 */
public class VideoServer {

	/**
	 * Listens for connections on the server port. This thread sends an HTTP
	 * header to each client and them adds them to the streaming list.
	 */
	private class ServerThread implements Runnable {

		@Override
		public void run() {
			while (true) {
				if (running) {
					try {
						Socket clientSocket = serverSocket.accept();
						// Give the client into its own thread and send the
						// header.
						new Thread(() -> {
							try {
								OutputStream clientWriteStream = clientSocket.getOutputStream();
								clientWriteStream.write(HEADER);
								synchronized (clientStreams) {
									clientStreams.add(clientWriteStream);
								}
							} catch (SocketException se) {
							} catch (IOException e) {
								System.out.println("Unable to communicate with client: " + e);
							}
						}).start();
					} catch (IOException e) {
						System.out.println("Unable to accept connection to video server: " + e);
					}
				} else {
					synchronized (serverThread) {
						try {
							serverThread.wait();
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}
	}

	/**
	 * The header that is initially sent when a client connects.
	 */
	private static final byte[] HEADER = ("HTTP/1.0 200 OK\r\n" +
			"Server: VideoStreamer\r\n" +
			"Content-Type: multipart/x-mixed-replace;boundary=jpgbound\r\n").getBytes();

	/**
	 * The content type that is sent to the client before each image.
	 */
	private static final byte[] CONTENT_TYPE = "Content-Type: image/jpeg\r\n".getBytes();
	/**
	 * The boundary that is sent between each image.
	 */
	private static final byte[] BOUNDARY = "\r\n--jpgbound\r\n".getBytes();

	/**
	 * TCP port the server is listens on.
	 */
	private final int port;
	/**
	 * Matrix that hold the JPEG quality parameters.
	 */
	private final MatOfInt qualityParams;
	/**
	 * Socket that listens for connections.
	 */
	private ServerSocket serverSocket;
	/**
	 * Thread that handles incoming connections.
	 */
	private final Thread serverThread = new Thread(new ServerThread(), "Video Server Thread");
	/**
	 * List of streams for currently connected clients.
	 */
	private final ArrayList<OutputStream> clientStreams = new ArrayList<>(1);

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
		qualityParams = new MatOfInt(Highgui.IMWRITE_JPEG_QUALITY, quality);
		serverSocket = new ServerSocket();
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
			if (serverSocket.isClosed()) {
				serverSocket = new ServerSocket();
			}
			serverSocket.bind(new InetSocketAddress(port));

			running = true;
			// If the thread has already been started, tell it to resume.
			if (serverThread.isAlive()) {
				synchronized (serverThread) {
					serverThread.notify();
				}
			} else {
				// Otherwise, start it.
				serverThread.start();
			}
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
			serverSocket.close();
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
			// Only send if at least one client is connected.
			if (!clientStreams.isEmpty()) {
				// Encode the image as a JPEG.
				Highgui.imencode(".jpg", image, compressionBuffer, qualityParams);
				int size = (int) compressionBuffer.total() * compressionBuffer.channels();
				// Resize the Java buffer to fit the data if necessary
				if (size > socketBuffer.length) {
					socketBuffer = new byte[size];
				}
				// Copy the OpenCV data to a Java byte[].
				compressionBuffer.get(0, 0, socketBuffer);
				synchronized (clientStreams) {
					// Send to all clients.
					for (int i = 0; i < clientStreams.size(); i++) {
						OutputStream cs = clientStreams.get(i);
						try {
							// Write image boundary.
							cs.write(BOUNDARY);
							// Write content type.
							cs.write(CONTENT_TYPE);
							// Write content length.
							cs.write(("Content-Length: " + size + "\r\n\r\n").getBytes());
							// Write the actual image.
							cs.write(socketBuffer, 0, size);
						} catch (IOException ex) {
							clientStreams.remove(i);
						}
					}
				}
			}
		} else {
			throw new IllegalStateException("Server must be running to send an image.");
		}
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
