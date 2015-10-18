# Crowbar maven plugin

Crowbar plugin for maven projects.
It runs test cases via [Surefire](https://maven.apache.org/surefire/maven-surefire-plugin/), so it supports both JUnit3 and Junit4.

As of this moment, it generates a tree visualization with the diagnostic report for each module in a project.

## Compilation and Installation

To compile the project and install it in your local maven repository, simply run the command:
```
mvn install
```

## Usage

Add the following to your `pom.xml`:
```
<build>
  <pluginManagement>
    <plugins>
      <plugin>
        <groupId>io.crowbar</groupId>
        <artifactId>crowbar-maven-plugin</artifactId>
        <version>1.1-SNAPSHOT</version>
        <configuration>
          <barinel>true</barinel>
        </configuration>
      </plugin>
    </plugins>
  </pluginManagement>
</build>
```

To run the project's test cases and the crowbar analysis, execute the command `mvn crowbar:test`.

### Connect to other servers
By default, `crowbar-maven-plugin` creates an internal `InstrumentationServer`. If you want the plugin to act solely as a client, just insert your `InstrumentationServer` port in the pom.xml. The following example sends the instrumentation information to a server listening on port 1234:
```
<build>
  <pluginManagement>
    <plugins>
      <plugin>
        <groupId>io.crowbar</groupId>
        <artifactId>crowbar-maven-plugin</artifactId>
        <version>1.1-SNAPSHOT</version>
        <configuration>
          <port>1234</port>
        </configuration>
      </plugin>
    </plugins>
  </pluginManagement>
</build>
```

### Other configurations
Statement granularity (default is `false`):
```
<statementGranularity>true</statementGranularity>
```

Enable barinel (default is `false`):
```
<barinel>true</barinel>
```

Set the maximum number of diagnostic candidates to be considered (default is `2000`):
```
<maxCandidates>10000</maxCandidates>
```

Report directory (default is `${project.build.directory}/crowbar-report`):
```
<reportDirectory>/temp/reports</reportDirectory>
```

Add arbitrary JVM options to set on the command line:
```
<argLine>-Xmx512m</argLine>
```

Restrict classes allowed for instrumentation:
```
<classesToInstrument>
  <class>fully.qualified.ClassName</class>
  <class>fully.qualified.ClassName2</class>
</classesToInstrument>
```

## Caveats
If the `argLine` parameter is set in the declaration of `maven-surefire-plugin`, it will override crowbar's request to add an agent to the test JVM. 
To circumvent this, you can add your JVM options in crowbar's `argLine` parameter.

The [`ExpectedException`](http://junit.org/apidocs/org/junit/rules/ExpectedException.html) `@Rule` for Junit4 is not currently supported.
