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
		while (true) {
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
