/*
 * Copyright (c) 2016 Avanza Bank
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.avanza.graphite.duplicator;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Connection to an endpoint. Is responsible for reconnection when a
 * connection is lost.
 * 
 * Currently data is discarded if an error occurs while sending.
 * 
 * @author Kristoffer Erlandsson
 */
public class EndpointConnection {

	private static final int RECONNECTION_DELAY = 3000;

	private final Logger log = new Logger(EndpointConnection.class);

	private Socket socket;

	private int reconnectionDelay;

	private Endpoint endpoint;

	public EndpointConnection(Endpoint endpoint) {
		this(endpoint, RECONNECTION_DELAY);
	}
	
	public EndpointConnection(Endpoint endpoint, int reconnectionDelay) {
		this.endpoint = endpoint;
		if (reconnectionDelay < 0) {
			throw new IllegalArgumentException("reconnectionDelay must be >= 0");
		}
		this.reconnectionDelay = reconnectionDelay;
	}

	public void send(String msg) {
		send(new byte[0], msg);
	}

	public void send(byte[] header, String msg) {
		for (boolean connected = ensureConnected(); !connected; connected = ensureConnected()) {
			sleep();
		}
		doSend(header, msg);
	}

	private void sleep() {
		try {
			Thread.sleep(reconnectionDelay);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private void doSend(byte[] header, String msg) {
		try {
			OutputStream outputStream = socket.getOutputStream();
			outputStream.write(header);
			outputStream.write(msg.getBytes());
		} catch (IOException e) {
			log.error("Exception when sending, this message: '" + msg + "', discarding:", e);
			close();
		}
	}

	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
		}
		socket = null;
	}

	private boolean ensureConnected() {
		if (socket == null) {
			try {
				socket = new Socket(endpoint.getAddress(), endpoint.getPort());
				log.info("Connected to " + endpoint);
			} catch (Exception e) {
				log.error("Failed to connect", e);
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "Endpoint connection to " + endpoint;
	}

}
