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

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread factory for creating named threads. Threads are daemon threads per default.
 * 
 * @author Kristoffer Erlandsson
 */
public class NamedThreadFactory implements ThreadFactory {

	private final ThreadGroup group;
	private final AtomicInteger threadNumber = new AtomicInteger(1);
	private final String namePrefix;
	private boolean daemon;

	/**
	 * Creates a daemon thread with the specified name prefix. Thread names will be namePrefix-<threadId>. Thread ID is
	 * incremented each time a thread is created using this factory.
	 * 
	 * @param namePrefix
	 *            not null.
	 */
	public NamedThreadFactory(String namePrefix) {
		this.namePrefix = Objects.requireNonNull(namePrefix);
		group = getThreadGroup();
		daemon = true;
	}

	/**
	 * Creates a thread with the specified daemon mode.
	 */
	public NamedThreadFactory(String namePrefix, boolean daemon) {
		this(namePrefix);
		this.daemon = daemon;
	}

	private ThreadGroup getThreadGroup() {
		// Done in the same way as java.util.concurrent.Executors.DefaultThreadFactory.
		SecurityManager s = System.getSecurityManager();
		return (s != null) ? s.getThreadGroup() :
				Thread.currentThread().getThreadGroup();
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(group, r, namePrefix + "-" + threadNumber.getAndIncrement());
		t.setDaemon(daemon);
		return t;
	}
}
