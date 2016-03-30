package com.avanza.graphite.duplicator;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public class TcpGraphiteEndpointSender {

	private static final int LOG_FAILED_ENQUEUES_DELAY_SECONDS = 10;

	private final Logger log = new Logger(TcpGraphiteEndpointSender.class);

	private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(250000);
	private final Endpoint endpoint;
	private long lastFailedEnqueueLog = System.nanoTime() - TimeUnit.SECONDS.toNanos(LOG_FAILED_ENQUEUES_DELAY_SECONDS);
	private final AtomicLong numFailedEnqueuesSinceLastLog = new AtomicLong();

	private Predicate<String> filter;

	public TcpGraphiteEndpointSender(Endpoint endpoint, Predicate<String> filter) {
		this.endpoint = endpoint;
		this.filter = filter;
	}

	public TcpGraphiteEndpointSender(Endpoint endpoint) {
		this(endpoint, x -> true);
	}

	public void enqueueMsg(String msg) {
		if (filter.test(msg)) {
			if (!queue.offer(msg)) {
				registerFailedEnqueue(msg);
			}
		}
	}

	private void registerFailedEnqueue(String msg) {
		numFailedEnqueuesSinceLastLog.incrementAndGet();
		if (System.nanoTime() - lastFailedEnqueueLog > TimeUnit.SECONDS.toNanos(LOG_FAILED_ENQUEUES_DELAY_SECONDS)) {
			log.warn("Endpoint " + endpoint + " - failed to enqueue message: '" + msg + "' this message was repeated (for different msgs) "
					+ (numFailedEnqueuesSinceLastLog.get() - 1) + " times before this log");
			numFailedEnqueuesSinceLastLog.set(0);
			lastFailedEnqueueLog = System.nanoTime();
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
		thread.setName("TcpGraphiteEndpointSender_" + endpoint);
		thread.start();
	}

	@Override
	public String toString() {
		return endpoint.toString();
	}
	
	
}
