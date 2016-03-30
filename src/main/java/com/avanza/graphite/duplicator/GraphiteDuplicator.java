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
		duplicatorServer.start();
	}

}
