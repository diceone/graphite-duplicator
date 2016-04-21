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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.function.Consumer;

/**
 * @author Kristoffer Erlandsson
 */
public class UdpReceiver {

	private static final int RECEIVE_BUFFER_SIZE = 16777216;
	Logger log = new Logger(UdpReceiver.class);
	
	private final int port;
	private DatagramSocket serverSocket;
	private byte[] receiveData = new byte[512];
	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	private Consumer<String> packetConsumer;
	
	private Thread t = new Thread(() -> {
		while (!Thread.interrupted()) {
			try {
				serverSocket.receive(receivePacket);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			String s = new String(receiveData, receivePacket.getOffset(), receivePacket.getLength());
			s = s.trim();
			packetConsumer.accept(s);
		}
	});

	public UdpReceiver(int port, Consumer<String> packetConsumer) {
		t.setName("udp-receiver");
		this.packetConsumer = packetConsumer;
		try {
			serverSocket = new DatagramSocket(port);
			serverSocket.setReceiveBufferSize(RECEIVE_BUFFER_SIZE);
			this.port = serverSocket.getLocalPort();
			log.info("Listening on port " + port + ", receive buffer size " + serverSocket.getReceiveBufferSize());
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
	}

	public void start() {
		t.start();
	}
	
	public void stop() {
		t.interrupt();
		try {
			t.join();
		} catch (InterruptedException e) {
		}
	}

	public int getPort() {
		return port;
	}

}
