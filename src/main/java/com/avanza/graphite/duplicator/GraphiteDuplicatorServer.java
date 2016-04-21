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

import java.util.ArrayList;
import java.util.List;

public class GraphiteDuplicatorServer {

	private final Logger log = new Logger(GraphiteDuplicatorServer.class);
	private List<EndpointSender> endpoints = new ArrayList<>();
	private int port;
	private TcpReceiver tcpReceiver;
	private UdpReceiver udpReceiver;

	public GraphiteDuplicatorServer(List<Endpoint> endpoints, int port) {
		this.port = port;
		for (Endpoint endpoint : endpoints) {
			this.endpoints.add(new EndpointSender(endpoint));
		}
	}

	public void start() {
		log.info("Starting with listen port " + port + " and endpoints: " + endPointsToString());
		for (EndpointSender endpointSender : endpoints) {
			endpointSender.start();
		}
		tcpReceiver = new TcpReceiver(port, this::enqueueMsg);
		tcpReceiver.start();
		udpReceiver = new UdpReceiver(port, this::enqueueMsg);
		udpReceiver.start();
	}
	
	
	private String endPointsToString() {
		return String.join(", ", endpoints.stream().map(Object::toString).collect(toList()));
	}
	
	private void enqueueMsg(String msg) {
		endpoints.forEach(endpointSender -> endpointSender.enqueueMsg(msg));
	}

}
