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
