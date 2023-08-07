# malicious-dependencies

Slightly malicious dependency ([analyzer/spring-build-analyzer](analyzer)) and a demonstration project ([demo](demo)). This project is intended to highlight the issues of including untrusted dependencies in your builds.

* The `analyzer` is a multi-module maven build controlled by a threat actor.
* The `demo` application is a sample application that could have been written by developers at your organitzation.

**TL;DR** - the `analyzer` is a compile time dependency, but could have been a build plugin or test scoped dependency, that injects a backdoor into any spring-boot application built.

## demonstration

**The project requires Maven and Java 17.**

First build and install (locally) the `spring-build-analyzer` by running:

**DO NOT** shorten the following to `mvn clean install` as things may not work.

```bash
cd analyzer
mvn clean
mvn install
cd ..
```

Next, in a different terminal, open netcat to listen on port 9999:

```bash
nc -l -p 9999
```

The `demo` application is a completely separate project that uses the `spring-build-analyzer` JAR. Compile and run the demo application:

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
$ nc -l -p 9999
whoami
jeremy
```

## Explanation

The `spring-build-analyzer` uses an annotation processor to inject a reverse shell into any spring-boot application that is compiled while the `spring-build-analyzer` is on the compile-time classpath. If you look at the `demo` project you will see that the `spring-build-analyzer` is just a standard dependency:

```xml
<dependency>
   <groupId>io.github.jeremylong.spring.analyzer</groupId>
   <artifactId>spring-build-analyzer</artifactId>
   <version>0.0.1-SNAPSHOT</version>
   <scope>compile</scope>
</dependency>
```

While the above is demonstrating the problem using a standard dependency and annotation processing - this could have been a build plugin, a test dependency, a transitive dependency, etc. Anything running during the build can modify the build output. This type of attack does not have to rely on annotation processing.

If you look at the source code for the `spring-build-analyzer` - you will not see the annotation processor that injects the malicious code. This is actually injected by the `build-helper` project during the test execution. This is demoing yet another way to inject code.

The injected code starts a benign reverse shell as it only connects back to localhost on port 9999. This is just a demonstration of what can go wrong at build time. The possibilities are limitless as there are so many different ways to subvert applications and inject backdoors.


## Reproducible Builds

The `demo` project is set up to create re-producible builds. This is useful for understanding that if the build has been compromised by including a malicious dependency or plugin - it doesn't matter where you build the project it is **Reproducibly Compromised**.

```bash
$ shasum -a 256 target/demo-0.0.1-SNAPSHOT.jar
6a4c421550323dd63431753a46f08c80fbf39b744ac173d637bdcb090f176664  target/demo-0.0.1-SNAPSHOT.jar

$ unzip -v target/demo-0.0.1-SNAPSHOT.jar
...
```

## Debugging

Did the above walk through not work? There might be a few reasons:

1. `curl localhost:8080` didn't return `Greetings from Spring Boot!`: 

   - Something is already running on port 8080. When the demo app is not running - ensure that nothing is running on port 8080.

2. No connection was made back to `nc -l -p 9999`.
   - Use alternative options to start the reverse shell: `nc -lp 9999`, `nc -nvlp 9999`
   - Ensure nothing is running on port `9999`. Alternatively, update the port in the [CtxtListener source](https://github.com/jeremylong/malicious-dependencies/blob/61d6b23de532f5236cd9dfd34d473d1f198f70a3/analyzer/build-helper/src/test/java/io/github/jeremylong/spring/build/analyzer/SensorDrop.java#L31-L32) and rerun the above steps.
   - From the root of the project, after building the demo app validate that the `CtxtListener.class` exists: `ls demo/target/classes/io/github/jeremylong/spring/analyzer/demo/CtxtListener.class`. If the class does not exist, consider adding debugging statements [here](https://github.com/jeremylong/malicious-dependencies/blob/61d6b23de532f5236cd9dfd34d473d1f198f70a3/analyzer/build-helper/src/test/java/io/github/jeremylong/spring/build/analyzer/SensorDrop.java#L82) and re-installing the `spring-build-analyzer` and rebuilding the demo application.
   - If the `CtxtListener.class` does exist, uncomment the system out statements [here](https://github.com/jeremylong/malicious-dependencies/blob/61d6b23de532f5236cd9dfd34d473d1f198f70a3/analyzer/build-helper/src/test/java/io/github/jeremylong/spring/build/analyzer/SensorDrop.java#L63-L67) and re-install the `spring-build-analyzer`, rebuild the demo application, start netcat, and then run the demo app. The added debugging output may show what is going wrong on your system.


