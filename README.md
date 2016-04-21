# graphite-duplicator

graphite-duplicator listens for Graphite metrics (line format) on TCP and/or UDP and forwards 
these metrics to one more more TCP endpoints.

## Usage
Download the latest release and run:

```
java -jar graphite-duplicator.jar <listenPort> <host:port> [<host:port> ...]
```

E.g.
```
java -jar graphite-duplicator.jar 2003 localhost:12003 graphite-standby.company.com:2003
```

graphite-duplicator listens for TCP connections and UDP packets on the same port.

## Use Cases

* Send your metrics to a hot standby instance.
* Setting up a new server? Send your metrics to both the new and old server until you are confident the new one works and has created all whisper files.
* Testing out a new backend that accepts metrics in Graphite format? Duplicate your metrics to the new backend to test it out.
* ...

