# spring-build-analyzer

Slightly malicious dependency (spring-build-analyzer) and a demonstration project (demo). This project is intended to highlight the issues of including untrusted dependencies in your builds.

## building

While there is a base `pom.xml` - the two projects should be built interdependently:

```bash
cd spring-build-analyzer
mvn install -DskipTests=true
cd ..
```

Next, in a different terminal, open netcat to listen on port 9999:

```bash
nc -l 9999
```

Then compile and run the demo application:

```bash
cd demo
mvn package -DskipTests=true
java -jar ./target/demo-0.0.1-SNAPSHOT.jar
```

In a third terminal, validate that the demo application is working as expected:

```bash
curl localhost:8080
```

You should receive back `Greetings from Spring Boot!`. Very exciting, isn't it?

Last, return to the tab with the netcat listener and the reverse shell should have connected; you can test by running `whoami`:

```bash
$ nc -l 9999
whoami
jeremy
```

## Explanation

The `spring-build-analyzer` uses an annotation processor to inject a reverse shell into any spring-boot application that is compiled while the `spring-boot-analyzer` is on the classpath. If you look at the `demo` project you will see that the `spring-boot-analyzer` looks like just a standard dependency:

```xml
<dependency>
    <groupId>io.github.jeremylong.spring.analyzer</groupId>
    <artifactId>spring-build-analyzer</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <scope>compile</scope>
</dependency>
```

Currently, the reverse shell is benign as it only connects back to localhost on port 9999. This is just a demonstration of what can go wrong at build time.

## Reproducible Builds

The `demo` project is setup to create re-producible builds. This is useful for understanding that if the build has been compromised by including a malicious dependency or plugin - it doesn't matter where you build the project it is **Reproducibly Compromised**.

```bash
$ shasum -a 256 target/demo-0.0.1-SNAPSHOT.jar
5a88a9ede7ed5bc4822ba84edef93b0d10aeba4aceca60c2b98636d5ec3d448c  target/demo-0.0.1-SNAPSHOT.jar

$ unzip -v target/demo-0.0.1-SNAPSHOT.jar
...
```