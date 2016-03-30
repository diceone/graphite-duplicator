package com.avanza.graphite.duplicator;

import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.avanza.graphite.duplicator.Endpoint.Type;

public class GraphiteDuplicatorServer {

	private final Logger log = new Logger(GraphiteDuplicatorServer.class);
	private List<TcpGraphiteEndpointSender> endpoints = new ArrayList<>();
	private int port;
	private TcpReceiver tcpReceiver;
	private UdpReceiver udpReceiver;
	private static final Predicate<String> INFLUX_FILTER = metric -> {
		return metric.contains("astrix.beans");
	};

	public GraphiteDuplicatorServer(List<Endpoint> endpoints, int port) {
		this.port = port;
		for (Endpoint endpoint : endpoints) {
			if (endpoint.getType().equals(Type.INFLUX)) {
				// TODO dynamic filter handling
				this.endpoints.add(new TcpGraphiteEndpointSender(endpoint, INFLUX_FILTER));
			} else {
				this.endpoints.add(new TcpGraphiteEndpointSender(endpoint));
			}
		}
	}

	public void start() {
		log.info("Starting with listen port " + port + " and endpoints: " + endPointsToString());
		for (TcpGraphiteEndpointSender endpointSender : endpoints) {
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
