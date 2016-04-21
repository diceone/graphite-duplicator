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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;


/**
 * @author Kristoffer Erlandsson
 */
public class TcpReceiver {

	private static Logger log = new Logger(TcpReceiver.class);
	private int port;
	private ServerSocket serverSocket;
	private Executor executor = Executors.newCachedThreadPool(new NamedThreadFactory("tcp-receiver", false));
	private Consumer<String> consumer;

	public TcpReceiver(int port, Consumer<String> consumer) {
		this.consumer = Objects.requireNonNull(consumer);
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.port = serverSocket.getLocalPort();
		log.info("Listening on port " + port);
	}

	private Runnable acceptor = () -> {
		while (!Thread.interrupted()) {
			try {
				Socket accepted = serverSocket.accept();
				log.info("Accepted connection from " + accepted.getInetAddress() + ":" + accepted.getPort());
				SocketReader reader = new SocketReader(accepted, consumer);
				executor.execute(reader);
			} catch (Exception e) {
				log.error("Exception in acceptor thread (it will continue to run)", e);
			}
		}
	};

	public void start() {
		executor.execute(acceptor);
	}
	
	public int getPort() {
		return port;
	}

	private class SocketReader implements Runnable {

		private Socket socket;
		private Consumer<String> consumer;

		public SocketReader(Socket socket, Consumer<String> consumer) {
			this.socket = socket;
			this.consumer = consumer;
		}

		public void run() {
			try {
				consumeLines();
			} catch (IOException e) {
				closeSocket();
			}
		}

		private void consumeLines() throws IOException {
			InputStream is = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				consumer.accept(line);
			}
			closeSocket();
		}

		private void closeSocket() {
			try {
				socket.close();
			} catch (IOException e) {
			}
			
		}
	}

}
