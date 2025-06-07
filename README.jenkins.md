# Jenkins and SonarQube Local Setup Guide

This guide provides instructions for setting up a local Jenkins server and SonarQube instance using Docker to run the CI pipeline for the create_ia_profiles project.

## Using Docker Compose (Recommended)

The easiest way to set up both Jenkins and SonarQube is to use Docker Compose, which allows you to start both services with a single command.

### Prerequisites
- Docker and Docker Compose installed on your machine
- Git installed on your machine
- At least 6GB of RAM available for Docker

### Steps to Set Up Jenkins and SonarQube with Docker Compose

1. **Start the services using Docker Compose**:
   ```powershell
   docker-compose up -d
   ```

2. **Get the initial Jenkins admin password**:
   ```powershell
   docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
   ```

3. **Access Jenkins in your browser**:
   - Open http://localhost:8080
   - Enter the initial admin password
   - Install suggested plugins
   - Create an admin user

4. **Access SonarQube in your browser**:
   - Open http://localhost:9000
   - Login with default credentials:
     - Username: admin
     - Password: admin
   - You'll be prompted to change the password

5. **Continue with the configuration steps** in the sections below:
   - Configure Jenkins Tools (Step 4 in "Setting Up Jenkins Locally")
   - Create Jenkins Credentials (Step 5 in "Setting Up Jenkins Locally")
   - Generate a SonarQube Token (Step 3 in "Setting Up SonarQube Locally")
   - Configure SonarQube for the Project (Step 4 in "Setting Up SonarQube Locally")

6. **Stopping the services**:
   ```powershell
   docker-compose down
   ```

7. **Stopping the services and removing volumes**:
   ```powershell
   docker-compose down -v
   ```

## Setting Up Jenkins Locally (Manual Method)

### Prerequisites
- Docker installed on your machine
- Git installed on your machine
- At least 4GB of RAM available for Docker

### Steps to Set Up Jenkins

1. **Run Jenkins using Docker**:
   ```powershell
   docker run -d -p 8080:8080 -p 50000:50000 --name jenkins jenkins/jenkins:lts-jdk21
   ```

2. **Get the initial admin password**:
   ```powershell
   docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
   ```

3. **Access Jenkins in your browser**:
   - Open http://localhost:8080
   - Enter the initial admin password
   - Install suggested plugins
   - Create an admin user

4. **Configure Jenkins Tools**:
   - Go to "Manage Jenkins" > "Global Tool Configuration"
   - Configure JDK:
     - Name: `JDK 21`
     - Install automatically: Check
     - Select "AdoptOpenJDK" and version "21"
   - Configure Maven:
     - Name: `Maven 3.8.0`
     - Install automatically: Check
     - Select version "3.8.0"
   - Save the configuration

5. **Create Jenkins Credentials**:
   - Go to "Manage Jenkins" > "Manage Credentials" > "Jenkins" > "Global credentials" > "Add Credentials"
   - Add the following credentials:
     - **SONAR_TOKEN**:
       - Kind: Secret text
       - Scope: Global
       - ID: SONAR_TOKEN
       - Description: SonarQube authentication token
       - Secret: Your SonarQube token (see SonarQube setup below)
     - **GITHUB_TOKEN** (if needed):
       - Kind: Secret text
       - Scope: Global
       - ID: GITHUB_TOKEN
       - Description: GitHub authentication token
       - Secret: Your GitHub personal access token

## Setting Up SonarQube Locally

### Prerequisites
- Docker installed on your machine
- At least 2GB of RAM available for Docker

### Steps to Set Up SonarQube

1. **Run SonarQube using Docker**:
   ```powershell
   docker run -d -p 9000:9000 --name sonarqube sonarqube:latest
   ```

2. **Access SonarQube in your browser**:
   - Open http://localhost:9000
   - Login with default credentials:
     - Username: admin
     - Password: admin
   - You'll be prompted to change the password

3. **Generate a SonarQube Token**:
   - Go to "My Account" > "Security"
   - Enter a token name (e.g., "jenkins-token")
   - Click "Generate"
   - Copy the generated token (you won't be able to see it again)
   - Use this token as the SONAR_TOKEN in Jenkins credentials

4. **Configure SonarQube for the Project**:
   - Go to "Administration" > "Projects" > "Management"
   - Click "Create Project"
   - Project key: `create_ia_profiles`
   - Display name: `create_ia_profiles`
   - Click "Set Up"
   - Select "Locally" for analysis method
   - Select "Maven" as the build technology
   - Follow the instructions to run a local analysis (this will be handled by Jenkins)

## Creating a Jenkins Pipeline

1. **Create a New Pipeline Job**:
   - Go to Jenkins dashboard
   - Click "New Item"
   - Enter a name (e.g., "create_ia_profiles")
   - Select "Pipeline"
   - Click "OK"

2. **Configure the Pipeline**:
   - In the "Pipeline" section, select "Pipeline script from SCM"
   - SCM: Git
   - Repository URL: Your repository URL
   - Credentials: Add your Git credentials if needed
   - Branch Specifier: `*/main` (or your default branch)
   - Script Path: `Jenkinsfile`
   - Save the configuration

3. **Run the Pipeline**:
   - Click "Build Now" to start the pipeline
   - The pipeline will execute the stages defined in the Jenkinsfile:
     - Checkout: Checks out the code from the repository
     - Build: Builds the application using Maven wrapper
     - Test: Runs tests using Maven wrapper
     - Code Quality: Runs code quality checks and SonarQube analysis

## Troubleshooting

### Jenkins Issues

1. **Permission Issues with Maven Wrapper**:
   - If Jenkins cannot execute the Maven wrapper, you may need to make it executable:
   ```powershell
   git update-index --chmod=+x mvnw.cmd
   ```

2. **Plugin Installation Issues**:
   - If you encounter issues with plugin installations, go to "Manage Jenkins" > "Manage Plugins" > "Advanced" and check "Check Now" to update the plugin index.

### SonarQube Issues

1. **Connection Issues Between Jenkins and SonarQube**:
   - Ensure both containers can communicate with each other
   - If running on the same host, you can use host.docker.internal as the hostname to connect from one container to another

2. **Memory Issues with SonarQube**:
   - If SonarQube fails to start or crashes, increase the memory allocation for Docker

## Additional Configuration

### Configuring SonarQube in Jenkins

When using Docker Compose, you need to configure Jenkins to use the SonarQube service name instead of localhost:

1. Go to "Manage Jenkins" > "Configure System"
2. Find the "SonarQube servers" section
3. Click "Add SonarQube"
4. Name: SonarQube
5. Server URL: 
   - For Docker Compose: http://sonarqube:9000
   - For manual setup: http://localhost:9000
6. Server authentication token: Select your SONAR_TOKEN credential
7. Save the configuration

Note: The Jenkinsfile has been updated to use http://sonarqube:9000 as the SonarQube URL when running in Docker Compose. This works because both services are in the same Docker network and can communicate using service names as hostnames.

### Running SonarQube Analysis Manually

If you need to run SonarQube analysis manually (outside of Jenkins):

For manual setup (SonarQube running on localhost):
```powershell
mvnw.cmd verify sonar:sonar -Psonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=your-sonar-token
```

For Docker Compose setup (from host machine):
```powershell
mvnw.cmd verify sonar:sonar -Psonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=your-sonar-token
```

For Docker Compose setup (from another container in the same network):
```powershell
mvnw.cmd verify sonar:sonar -Psonar -Dsonar.host.url=http://sonarqube:9000 -Dsonar.login=your-sonar-token
```

## References

- [Jenkins Documentation](https://www.jenkins.io/doc/)
- [SonarQube Documentation](https://docs.sonarqube.org/)
- [Jenkins Pipeline Syntax](https://www.jenkins.io/doc/book/pipeline/syntax/)
- [SonarQube Scanner for Maven](https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-maven/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Docker Networking](https://docs.docker.com/network/)
