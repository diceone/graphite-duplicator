package com.avanza.graphite.duplicator;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

	private final String category;
	private final PrintStream out;
	private DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");

	public Logger(String category) {
		this.category = category;
		this.out = System.err;
	}

	public Logger(Class<?> category) {
		this(category.getName());
	}

	public void info(String msg) {
		print("INFO", msg);
	}
	
	public void warn(String msg) {
		print("WARN", msg);
	}
	
	public void error(String msg) {
		print("ERROR", msg);
	}

	public void error(String msg, Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println(msg);
		e.printStackTrace(pw);
		print("ERROR", sw.toString());
	}
	
	private void print(String severity, String msg) {
		out.println(String.format("[%s] %s %s - %s", format.format(ZonedDateTime.now()), severity, category, msg));
	}
}
