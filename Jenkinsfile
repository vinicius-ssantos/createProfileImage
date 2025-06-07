pipeline {
    agent any

    tools {
        // Define the Maven tool installation to use
        maven 'Maven 3.8.0'
        jdk 'JDK 21'
    }

    environment {
        // Environment variables for SonarQube
        SONAR_TOKEN = credentials('SONAR_TOKEN')
        GITHUB_TOKEN = credentials('GITHUB_TOKEN')
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout code from the repository
                checkout scm
            }
        }

        stage('Build') {
            steps {
                // Build the application using Maven wrapper
                bat 'mvnw.cmd clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                // Run tests using Maven wrapper
                bat 'mvnw.cmd test'
            }
            post {
                always {
                    // Publish JUnit test results
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Code Quality') {
            steps {
                // Run code quality checks and SonarQube analysis
                // When using Docker Compose, SonarQube is available at http://sonarqube:9000
                bat 'mvnw.cmd verify sonar:sonar -Psonar -Dsonar.host.url=http://sonarqube:9000'
            }
            post {
                always {
                    // Publish JaCoCo code coverage report
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java'
                    )
                }
            }
        }
    }

    post {
        always {
            // Clean up workspace after build
            cleanWs()
        }
    }
}
