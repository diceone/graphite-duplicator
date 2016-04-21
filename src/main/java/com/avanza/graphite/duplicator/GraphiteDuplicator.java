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

import static java.util.stream.Collectors.*;

import java.util.Arrays;
import java.util.List;

public class GraphiteDuplicator {
	private final Logger log = new Logger(GraphiteDuplicator.class);

	private static final String USAGE = "Usage: java -jar graphite-duplicator.jar <listenPort> <host:port> [<host:port> ...]";
	private String[] args;

	public static void main(String[] args) {
		GraphiteDuplicator graphiteDuplicator = new GraphiteDuplicator(args);
		graphiteDuplicator.start();
	}

	public GraphiteDuplicator(String[] args) {
		this.args = args;
	}

	private void start() {
		if (args.length < 2) {
			log.error(USAGE);
			System.exit(1);
		}
		String listenPort = args[0];
		int port = Integer.parseInt(listenPort);
		String[] stringEndpoints = Arrays.copyOfRange(args, 1, args.length);
		List<Endpoint> endpoints = Arrays.stream(stringEndpoints).map(Endpoint::valueOf).collect(toList());
		GraphiteDuplicatorServer duplicatorServer = new GraphiteDuplicatorServer(endpoints, port);
		try {
			duplicatorServer.start();
		} catch (Exception e) {
			log.error("Failed to start", e);
			System.exit(1);
		}
	}

}
