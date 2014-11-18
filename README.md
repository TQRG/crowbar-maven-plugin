# Crowbar maven plugin

Crowbar plugin for maven projects.
Runs test cases via surefire, so it supports both JUnit3/4 and TestNG.

As of this moment, it generates a JSON file with the diagnostic report for each submodule.

## Usage

Add the following to your `pom.xml`:
```
<build>
  <pluginManagement>
    <plugins>
      <plugin>
        <groupId>io.crowbar</groupId>
        <artifactId>crowbar-maven-plugin</artifactId>
        <version>1.0-SNAPSHOT</version>
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
        <version>1.0-SNAPSHOT</version>
        <configurations>
          <port>1234</port>
        </configurations>
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

Enable fuzzinel (default is `false`):
```
<fuzzinel>true</fuzzinel>
```

Report directory (default is `${project.build.directory}/crowbar-report`):
```
<reportDirectory>/temp/reports</reportDirectory>
```

Add arbitrary JVM options to set on the command line:
```
<argLine>-Xmx512m</argLine>
```

## Caveats
If the `argLine` parameter is set in the declaration of `maven-surefire-plugin`, it will override crowbar's request to add an agent to the test JVM. 
To circumvent this, you can add your JVM options in crowbar's `argLine` parameter.
