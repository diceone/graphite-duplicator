package com.avanza.graphite.duplicator;

import java.net.InetSocketAddress;
import java.util.Objects;

public class Endpoint {

	private final String address;
	private final int port;

	public Endpoint(String adress, int port) {
		this.address = Objects.requireNonNull(adress);
		if (port < 0 || port > 65536) {
			throw new IllegalArgumentException("Illegal port");
		}
		if (adress.isEmpty()) {
			throw new IllegalArgumentException("Adress can not be empty");
		}
		this.port = port;
	}

	public static Endpoint valueOf(String s) {
		String[] split = s.split(":");
		if (split.length != 2) {
			throw new IllegalArgumentException("String must be on format <address:port>");
		}
		int port = parsePort(split[1]);
		return new Endpoint(split[0], port);
	}

	private static int parsePort(String portString) {
		try {
			return Integer.parseInt(portString);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Port must be a valid integer");
		}
	}
	
	public InetSocketAddress toInetAddress() {
		return new InetSocketAddress(address, port);
	}

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	@Override
	public String toString() {
		return address + ":" + port;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Endpoint other = (Endpoint) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

}
