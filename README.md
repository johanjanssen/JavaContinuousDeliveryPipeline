## General remarks
- This setup is tested on Windows. Running it on another OS might require modifications.

## Files and directories
- Configured application: contains an application including all the configuration
- Pipeline: contains Docker compose files
- Start application: contains an application without all the configuration
- removeAll.sh: removes all containers, images and volumes
- removeContainersAndVolumesExceptNexus.sh: removes all containes and all volumes except for Nexus

## Credentials

These are the credentials used in the tools. Some are set by default, other's have to be set manually.

| Tool      | Username | Password    | Configured    |
|-----------|----------|-------------|---------------|
| Gitlab    | root     | admin123    | Manually      |
| Jira      | admin    | admin       | Manually      |
| SonarQube | admin    | admin       | Default       |
| Nexus     | admin    | admin123    | Default       |
| Jenkins   | admin    | admin       | Manually      |

---
# ***Configure the Docker environment***
The complete stack of tools is provided for in the form of a docker-compose file. 

### Memory and CPU for docker
The full stack of tools will use quite a lot of resources. Please select as much CPUs (total CPUs-1) and memory as possible.

- Goto Docker -> Settings -> Advanced
- ***Change CPU's and memory***
- Click Apply
- Goto Daemon
- ***Configure Docker registry***: localhost:8107
- Click Apply

# ***Start the Docker containers***
- Open a shell **with Docker support**. It should do something when you type 'docker info'.
- Goto the directory containing ```docker-compose.yml```
- Execute the following command: ```docker-compose up```

### View the result
- Execute the following command in another shell: ```docker ps```
- Goto the URL's of the different applications and check if they are running.

---
# ***Configure Nexus cache***
- Open .m2/settings.xml in your User directory (or create one if it does not exist
- Add the following content
```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.1.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.1.0 http://maven.apache.org/xsd/settings-1.1.0.xsd">
  <mirrors>
    <mirror>
      <id>central</id>
      <name>central</name>
      <url>http://localhost:8105/repository/maven-central/</url>
      <mirrorOf>*</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```

---
# ***Create a Spring REST application***

### Create the Maven configuration
- Goto the URL: http://start.spring.io/
- Add the 'Web' dependency
- Click: Generate project

### Create an application with a REST endpoint
- Extract the zip file with the generated project
- Open the project in your IDE
- Create a Maven REST application based on this guide: https://spring.io/guides/gs/rest-service/

### Optionally use these code blocks from the guide:
```java
public class Greeting {

    private final long id;
    private final String content;

    public Greeting(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
```

```java
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return new Greeting(counter.incrementAndGet(),
                            String.format(template, name));
    }
}
```

### Option 1: Run the application in the IDE
- Run the application from within the IDE

### Option 2: Run the application standalone with Maven
- Run the application: ```mvn spring-boot:run```

### Option 3: Run the application with Java
- Create a FAT jar: ```mvn package```
- Run the application: ```java -jar target/[jarfile]```

### View the result
- Browse to: http://localhost:8080/greeting
- Browse to: http://localhost:8080/greeting?name=Anna

---
# ***Create GitLab repository***

### Create a Git repository for your project
- Goto the GitLab URL: http://localhost:8101
- Change the password: admin123
- Login with username: root
- Click: Create a project
- Enter the Project name: TestApp
- Set the Visibility Level: Public
- Click: Create Project
- Read the instructions

# ***Add project to GitLab***
- Follow the GitLab instructions in GitLab for: Existing folder
- Make sure to execute the command INSIDE the directory where the pom.xml is located

### View the result
- Verify if the code is visible in GitLab

---
# ***Configure Jenkins***

### Login and change password
- Retrieve the initial administrator password with the ***Command Prompt***: ```docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword```
- Goto the Jenkins url: http://localhost:8103
- Enter the initial administrator password
- Plugins are already installed so close the Customize Jenkins view
- Click: Start using Jenkins

### Configure password
- Click: admin
- Click: configure
- Enter password and confirm password: admin
- Click: save

### Configure Java 11 in Jenkins
- Click: Manage Jenkins
- Click: Global Tool Configuration
- Click: Add JDK
- Enter name: Java11
- Disable: Install automatically
- Enter JAVA_HOME: /usr/lib/jvm/jdk-11.0.1
- Click: Save

### Configure Maven in Jenkins
- Click: Manage Jenkins
- Click: Global Tool Configuration
- Click: Add Maven
- Enter name: Maven
- Disable: Install automatically
- Enter MAVEN_HOME: /usr/share/maven
- Click: Save

### Disable XSRF token in Jenkins
- Goto the main Jenkins screen
- Click: Manage Jenkins -> Configure Global Security
- Disable: Prevent Cross Site Request Forgery exploits (scroll down a bit)
- Click: Save

---
# ***Create a Jenkins pipeline***
### Configure Pipeline
- Goto the Jenkins URL: http://localhost:8103
- Click: New Item
- Enter item name: Pipeline
- Select: Pipeline
- Click: OK
- Enable Discard Old Builds
- Set the Max # of builds to keep to 10
- Enable: Do not allow concurrent builds
- Go to the Definition part of the Pipeline section
- Select the following from the Definition dropdown: Pipeline script from SCM
- Select SCM: Git
- Enter Repository URL: http://gitlab:8101/root/testapp.git
- Click: Save

---
# ***Configure GitLab to allow Webhooks***
- Goto the admin area: click on the wrench at the tob
- Click: Settings
- Click: Network
- Click 'Expand' for the Outbound requests
- Enable: Allow requests to the local network from hooks and services
- Click: Save changes

---
# ***Create a Webhook in GitLab to the Jenkins pipeline***
- Goto the project on GitLab
- Click: Settings
- Click: Integrations
- Enter the URL: http://admin:admin@jenkins:8080/job/Pipeline/build
- Disable: Enable SSL verification
- Click: Add webhook

---
# ***Create a Jenkinsfile***

### Create your first Jenkinsfile
- Goto the directory of the application you created
- Create file: Jenkinsfile (so no Jenkinsfile.txt!)
- Change the Jenkins file based on the configuration below:
```groovy
pipeline {
	agent any	

	tools {
		jdk 'Java11'
	}

	stages {  	
		stage('Maven package') {
			steps {
				sh 'mvn package'
			}
		}
	}
}
```
- Add, commit and push changes to Git

### View the result
- Click on the project in Jenkins
- View the deployment pipeline

---
# ***Configure SonarQube***
- Goto the SonarQube URL: http://localhost:8104
- Log in
- Enter 'jenkins' as the name for the API token
- Click: generate
- Copy the token and save it for later
- Click: Continue
- Click: Java
- Click: Maven
- Copy: the Maven command
- Click: Finish this tutorial

---
# ***Add SonarQube to Jenkinsfile***
- Remove the current stage from the Jenkinsfile
- Add the following to the Jenkinsfile:
```groovy
environment { 		
	SONARQUBE_LOGIN_TOKEN = '[your-SonarQube-token]'
}

stages {
	stage('Clean') {
		steps {
			sh 'mvn clean'
		}
	}  	
	stage('Unit tests with coverage') {
		steps {
			sh 'mvn org.jacoco:jacoco-maven-plugin:prepare-agent install -Dmaven.test.failure.ignore=false'
		}
	}
	
	stage('SonarQube analysis') {
		steps {
			sh 'mvn sonar:sonar -Dsonar.host.url=http://sonarqube:9000 -Dsonar.login=${SONARQUBE_LOGIN_TOKEN}'
		}
	}
}
```

### View the result
- Git add, commit and push the changes
- Goto the SonarQube URL: http://localhost:8104
- Click on your project

---
# ***Commit hash as version number for the artifacts***
- Change the pom.xml
```<version>0.0.1-SNAPSHOT</version>``` to 	```<version>0.0.1</version>```
- Add the following to the pom.xml in the ***build*** section:
```xml
<!-- For mvn package -->
<finalName>${project.artifactId}-${project.version}-${git.commit.id}</finalName>
```
```xml
<!-- To use Git commit hash in artifactname as the buildNumber -->
<plugin>
	<groupId>pl.project13.maven</groupId>
	<artifactId>git-commit-id-plugin</artifactId>
	<executions>
		<execution>
			<phase>validate</phase>
			<goals>
				<goal>revision</goal>
			</goals>
		</execution>
	</executions>
	<configuration>
		<verbose>false</verbose>
	</configuration>
</plugin>
<!-- For mvn install / deploy -->
<plugin>
	<groupId>org.codehaus.gmaven</groupId>
	<artifactId>groovy-maven-plugin</artifactId>
	<version>2.0</version>
	<executions>
		<execution>
			<phase>verify</phase>
			<goals>
				<goal>execute</goal>
			</goals>
			<configuration>
				<source>project.artifact.version='${project.artifact.version}-${git.commit.id}';</source>
			</configuration>
		</execution>
	</executions>
</plugin>
```
- Execute: ```mvn package```


---
# ***Configure Nexus release repository***
- Add the following to the pom.xml
```xml
<distributionManagement>
	<repository>
		<id>nexus</id>
		<url>http://nexus:8081/repository/maven-releases/</url>
	</repository>
</distributionManagement>
```

---
# ***Add Nexus to Jenkinsfile***
- Add the following at the end of the Jenkinsfile:
```groovy
stage('Upload artifacts') {
	steps {
		sh 'mvn deploy -Dmaven.test.skip=true -Dmaven.install.skip=true'	
	}
}
```
- Commit and push to the Git server

### View the result
- View the Console Output of the Jenkins build
- Check if the artifacts (such as the jar file) are visible in Nexus


---
# ***Create Dockerfile for the application***
- Create a file in the root of the project named: Dockerfile
- Add the following content to the Dockerfile:
```
FROM openjdk:11.0.1-jre-slim

ADD /target/*.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
```

---
# ***Add Docker to Jenkinsfile***
- Change and add the following settings to the 'environment'
```groovy
DOCKER_REGISTRY_PORT = '8107'
APPLICATION_PORT = '8110'
```
- Add the following as the last stages
```groovy
stage('Retrieve commit id') {
  steps {
	sh "git rev-parse HEAD > commitid"
	script {
		commitid = readFile('commitid').trim()
	}
  }
}
stage('Create docker image') {
	steps {
		sh "docker build -t 127.0.0.1:${DOCKER_REGISTRY_PORT}/testapp:latest -t 127.0.0.1:${DOCKER_REGISTRY_PORT}/testapp:'${commitid}' ."
		sh "docker push 127.0.0.1:${DOCKER_REGISTRY_PORT}/testapp:'${commitid}'"
		sh "docker push 127.0.0.1:${DOCKER_REGISTRY_PORT}/testapp:latest"
	}
}
stage('Deploy to test') {
	steps {
		// Stop container if it's running. Always return true, so build does not fail if the container does not exist.
		sh "docker stop testapp || true && docker rm testapp || true"
		sh "docker run -d --name testapp -p ${APPLICATION_PORT}:8080 127.0.0.1:${DOCKER_REGISTRY_PORT}/testapp:'${commitid}'"
		sh "docker network connect --alias testapp pipeline_default testapp"
	}
}	
```

### View the result
- Watch the Jenkins pipeline
- Verify if the application is running (PORT is set in the Jenkinsfile): http://localhost:8110/greeting


---
# ***Configure Gatling***
- Add the following to the pom.xml
```xml
<dependency>
	<groupId>io.gatling.highcharts</groupId>
	<artifactId>gatling-charts-highcharts</artifactId>
	<version>3.0.1.1</version>
	<scope>test</scope>
</dependency>
```

```xml
<plugin>
	<groupId>io.gatling</groupId>
	<artifactId>gatling-maven-plugin</artifactId>
	<version>3.0.1</version>
</plugin>
```

---
# ***Add Gatling test***
- Create the script named BasicSimulation.scala  in [application-name]/src/test/scala/gatling
```scala
package gatling

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BasicSimulation extends Simulation {
  val scn = scenario("My scenario").repeat(3) {
    exec(
      http("Ping")
        .get("http://testapp:8080/greeting")
        .check(status.is(200))
    ).pause(10 millisecond)
  }

  setUp(scn.inject(
     rampUsers(1000) during(20 seconds)
  )).assertions(global.successfulRequests.percent.is(100))	 
}
```

---
# ***Add Gatling to Jenkinsfile***
- Change the Jenkins file and add the following stage as the last stage
```groovy
stage('Performance analysis') {
	steps {
		sh 'mvn gatling:test -Dgatling.simulationClass=gatling.BasicSimulation'
		// Archive results for Jenkins visualization
		gatlingArchive()
	}
}
```
- Add, commit and push to Git

### View the result
- Goto Jenkins
- Click: Pipeline
- Click: Gatling
- For a detailed report scroll down and click on a report, or click on a build and then click on Gatling.
- Tip 1: Click: Open in a new window
- Tip 2: Open a build number and view the Gatling report for that specific build

---
# ***OPTIONAL part starts here***

---
# ***Configure OWASP Dependency Check***
- Add the plugin to the pom.xml:
```xml
<plugin>
	<groupId>org.owasp</groupId>
	<artifactId>dependency-check-maven</artifactId>
	<version>4.0.2</version>
	<configuration>
		<cveUrl12Modified>http://owaspcache:80/nvdcve-modified.xml.gz</cveUrl12Modified>
		<cveUrl20Modified>http://owaspcache:80/nvdcve-2.0-modified.xml.gz</cveUrl20Modified>
		<cveUrl12Base>http://owaspcache:80/nvdcve-%d.xml</cveUrl12Base>
		<cveUrl20Base>http://owaspcache:80/nvdcve-2.0-%d.xml</cveUrl20Base>
		<retireJsAnalyzerEnabled>false</retireJsAnalyzerEnabled>
	</configuration>
	<executions>
		<execution>
			<goals>
				<goal>check</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

---
# ***Add OWASP vulnerability***
- Add a dependency with vulnerabilities to the pom.xml
```xml
<!-- Only added as vulnerability for the OWASP dependency check -->
<dependency>
	<groupId>com.google.guava</groupId>
	<artifactId>guava</artifactId>
	<version>14.0</version>
</dependency>
```

---
# ***Add OWASP dependency check to Jenkinsfile***
- Add a stage to the Jenkinsfile (before SonarQube)
```groovy
stage('Security analysis') {
	steps {
		// Scan for known CVE's in project. Fail on severity 8+ CVE's.
		sh 'mvn org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=8 -B'
	}
}
```
- Add, commit and push changes to Git

### View the result
- Goto Jenkins
- Click: Pipeline
- Click: build
- Click: Workspaces
- Click: target
- View: dependency-check-report
- A part of the information is also visible in the Jenkins Console Output
- It's possible to integrate the results within SonarQube with a plugin.


---
# ***Add mutation analysis plugin to SonarQube***
- Goto the SonarQube URL: http://localhost:8104
- Login
- Click: Administration -> Marketplace
- Search for: Mutation Analysis (replaces the Pitest plugin)
- Click: Install (for the Mutation Analysis plugin)
- Scroll to the top of the page
- Click: Restart
- Click (again): Restart
- Wait for the restart to finish

---
# ***Add mutations to SonarQube quality profile***
- Login
- Click: Quality Profiles
- Click: 'down arrow' for Java Sonar way
- Click: copy
- Enter: 'My way' as the new name
- Click: Copy
- Click: Actions
- Click: Set as Default
- Click: Activate More
- Click repository: MutationAnalysis
- Click: Bulk Change
- Click: Activate In My Way
- Click: apply
- Click: close

---
# ***Configure PiTest***
- Add the following content:
```xml
<plugin>
	<groupId>org.pitest</groupId>
	<artifactId>pitest-maven</artifactId>
	<version>1.4.3</version>
	<configuration>
		<outputFormats>
			<outputFormat>XML</outputFormat>
		</outputFormats>
	</configuration>
</plugin>
```

---
# ***Add example code***
### Add a class to your project under /src/main/java/com/example/demo
```java
package com.example.demo;

public class PiTestExample {	
	
	public int getValueOrBoundary(int value, int boundary) {
		if (value < boundary) {
			return value;
		}
		return boundary;
	}
}
```

---
# ***Add example test code***
### Add a Test class to your project under /src/test/java/com/example/demo
```java
package com.example.demo;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PiTestExampleTest {

	private PiTestExample piTestExample = new PiTestExample();

	// This test is pretty lousy, and PIT will create several mutants that will survive this test:
	@Test
	public void testGetValueOrBoundary() {
		assertTrue(piTestExample.getValueOrBoundary(20, 100) < 101);
	}
}
```

---
# ***Add PiTest to Jenkinsfile***
- Add the following to the Jenkinsfile before the SonarQube stage:
```groovy
stage('Mutation tests') {
	steps {
		sh 'mvn org.pitest:pitest-maven:mutationCoverage'
	}
}
```

### View the result
- Git add, commit and push the changes
- Wait until the Jenkins job completed
- Goto the SonarQube URL: http://localhost:8104
- Click on your project
- Click: Bugs
- View the survived mutants (PiTest)

---
# ***Java 11 String.repeat***



### Add a Java 11 feature to the controller:
```java
@RequestMapping("/hello")
public String hello() {
	return "hello ".repeat(5);
}
```
