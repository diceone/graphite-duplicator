/*
 * Copyright 2015 Avanza Bank AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.avanza.graphite.duplicator;

import java.io.IOException;
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
			socket.getOutputStream().write(header);
			socket.getOutputStream().write(msg.getBytes());
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
