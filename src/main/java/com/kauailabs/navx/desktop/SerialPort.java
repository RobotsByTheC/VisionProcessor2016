/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package com.kauailabs.navx.desktop;

/**
 * @author ben
 */
public class SerialPort {

    private final com.fazecast.jSerialComm.SerialPort port;

    private byte[] readBuffer = new byte[1];
    private int filledLength = 0;
    private boolean terminatorEnabled = false;
    private char terminator;

    public SerialPort(int baudRate, String portId) {
        port = com.fazecast.jSerialComm.SerialPort.getCommPort(portId);
        port.setBaudRate(baudRate);

    }

    public void setReadBufferSize(int size) {
        readBuffer = new byte[size];
    }

    public void setTimeout(double timeout) {
        int intTimeout = (int) (timeout * 1000);
        port.setComPortTimeouts(com.fazecast.jSerialComm.SerialPort.TIMEOUT_READ_BLOCKING
                | com.fazecast.jSerialComm.SerialPort.TIMEOUT_WRITE_BLOCKING, intTimeout, intTimeout);
    }

    private long lastReadTime = -1;

    public byte[] read(int count) {
        byte[] returnBuffer = null;
        byte[] newBuffer = new byte[count - filledLength];
        int read = port.readBytes(newBuffer, newBuffer.length);
        long currTime = System.currentTimeMillis();
        if (read > 0) {
            lastReadTime = currTime;

            System.arraycopy(newBuffer, 0, readBuffer, filledLength, read);
            read += filledLength;

            if (terminatorEnabled) {
                for (int i = 0; i < read; i++) {
                    if (readBuffer[i] == terminator) {
                        read = i + 1;
                        returnBuffer = new byte[read];
                        System.arraycopy(readBuffer, 0, returnBuffer, 0, read);
                        filledLength = readBuffer.length - read;
                        System.arraycopy(readBuffer, read, readBuffer, 0, filledLength);
                        break;
                    }
                }
            }

            if (returnBuffer == null) {
                if (read != readBuffer.length) {
                    returnBuffer = new byte[read];
                    System.arraycopy(readBuffer, 0, returnBuffer, 0, read);
                } else {
                    returnBuffer = readBuffer;
                }
            }
        } else {
            if (lastReadTime != -1 && (currTime - lastReadTime) > 1000) {
                port.closePort();
            }
            returnBuffer = new byte[0];
        }

        return returnBuffer;
    }

    public int write(byte[] buffer, int count) {
        return port.writeBytes(buffer, count);
    }

    public int getBytesReceived() {
        return port.bytesAvailable();
    }

    public void free() {
        port.closePort();
    }

    public void enableTermination(char terminator) {
        terminatorEnabled = true;
        this.terminator = terminator;
    }

    public void flush() {
        // NOOP
    }

    public void reset() {
        port.closePort();
        port.openPort();
    }
}
