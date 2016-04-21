# graphite-duplicator

graphite-duplicator listens for Graphite metrics (line format) on TCP and/or UDP and propagates
these metrics to one more more TCP endpoints.

## Usage
```
java -jar graphite-duplicator.jar <listenPort> <host:port> [<host:port> ...]
```

E.g.
```
java -jar graphite-duplicator.jar 2003 localhost:12003 graphite-standby.company.com:2003
```
