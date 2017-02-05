# keyvalues

## usage
### Setup
Before running, you need to have:
- 8080 port free. Checkup using 
```
lsof -i tcp:8080
```
- maven and proper settings.xml with a valid repo 
- jdk8

### Building and running
```
mvn clean install
mvn dependency:copy-dependencies
java -cp "target/dependency/*:target/keyvalues.jar" com.twitter.App
```
### Logs
- All logs should be in log4j-application.log under the root project directory. 

### examples
This root GET returns the size of the current key map
```
curl localhost:8080 
```

This POST creates a key=http/port with value=443
```
curl --data "value=443" localhost:8080/http/port 
```

This creates a callback on http/port with url=http://www.example.com
```
curl --data "url=http://www.example.com" localhost:8080/http/port/callback
```

This GET returns a list of callbacks for http/port in JSON format.
```
curl localhost:8080/http/port/callback
[{"name":callback-1,"url":"http://www.example.com"}]
```

This Delete deletes an existing callback for http/port
```
curl -X DELETE "localhost:8080/http/port/callback/callback-1"
```

## Assumptions and Limitations
- If calling callback fails (callback host not responding, callback host error, or anything other than an ovbious false), it returns true by default for checks.
- The callback-id is strictly increasing, even with some deletion happening in between. This can be solved by using a bitset of 32 bits (int) to record used ids, and keep a min for unused id. (TODO)
- Root resource (localhost:8080) returns the size of the key/value map, as a string.
