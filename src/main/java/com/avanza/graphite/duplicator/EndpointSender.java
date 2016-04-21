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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class EndpointSender {

	private static final int LOG_FAILED_ENQUEUES_DELAY_SECONDS = 10;

	private final Logger log = new Logger(EndpointSender.class);

	private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(250000);
	private final Endpoint endpoint;
	private long lastFailedEnqueueLog = System.nanoTime() - TimeUnit.SECONDS.toNanos(LOG_FAILED_ENQUEUES_DELAY_SECONDS);
	private final AtomicLong numFailedEnqueuesSinceLastLog = new AtomicLong();

	public EndpointSender(Endpoint endpoint) {
		this.endpoint = endpoint;
	}

	public void enqueueMsg(String msg) {
		if (!queue.offer(msg)) {
			registerFailedEnqueue(msg);
		}
	}

	private void registerFailedEnqueue(String msg) {
		numFailedEnqueuesSinceLastLog.incrementAndGet();
		if (System.nanoTime() - lastFailedEnqueueLog > TimeUnit.SECONDS.toNanos(LOG_FAILED_ENQUEUES_DELAY_SECONDS)) {
			log.warn("Endpoint " + endpoint + " - failed to enqueue message: '" + msg
					+ "' this message was repeated (for different msgs) "
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
		thread.setName("EndpointSender_" + endpoint);
		thread.start();
	}

	@Override
	public String toString() {
		return endpoint.toString();
	}

}
