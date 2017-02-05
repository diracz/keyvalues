# keyvalues

## usage
### Setup
Before running, you need to have:
- 8080 port free. Checkup using 
```
lsof -i tcp:8080
```
- mvn and jdk8 setup

### Building and running
- mvn clean install
- mvn dependency:copy-dependencies
- java -cp "target/dependency/*:target/keyvalues.jar" com.twitter.App
