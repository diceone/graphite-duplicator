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
