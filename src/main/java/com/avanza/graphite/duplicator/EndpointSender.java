package com.avanza.graphite.duplicator;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class EndpointSender {

	private final Logger log = new Logger(EndpointSender.class);

	private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(250000);
	private final Endpoint endpoint;

	public EndpointSender(Endpoint endpoint) {
		this.endpoint = endpoint;
	}

	public void enqueueMsg(String msg) {
		if (!queue.offer(msg)) {
			log.warn("Failed to enqueue msg '" + msg + ", queue size: " + queue.size() + ", endpoint: " + endpoint);
		}
	}

	public void start() {
		EndpointConnection endpointConnection = new EndpointConnection(endpoint);
		Thread thread = new Thread(() -> {
			while (!Thread.interrupted()) {
				try {
					String msg = queue.take();
					endpointConnection.send(msg + "\n");
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		});
		thread.setName("EndpontSender_" + endpoint);
		thread.start();
	}

	@Override
	public String toString() {
		return endpoint.toString();
	}
	
	
}
