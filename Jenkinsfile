pipeline {
    agent any
    tools {
        gradle 'Gradle-7.4.2'
    }
    environment {
        JDBC_URL = credentials('JDBC_URL')
        JDBC_USER = credentials('JDBC_USER')
        JDBC_PASSWORD = credentials('JDBC_PASSWORD')
    }
    stages {

        stage('Source') {
            steps {
                git branch: 'master',
                    changelog: false,
                    poll: false,
                    url: 'https://github.com/evil-face/jdbc-example'
            }
        }

        stage('Clean') {
            steps {
                echo "Cleaning..."
                sh 'gradle clean'
            }
        }

        stage('Test') {
                steps {
                    echo "Running tests..."
                    sh 'gradle test'
                }
        }

        stage('Static scan') {
                steps {
                    withSonarQubeEnv(installationName: 'sq1') {
                        echo "Running static code scans..."
                        sh 'gradle sonarqube'
                    }
                }
        }

        stage('Build') {
                steps {
                    echo "Creating build..."
                    sh 'gradle build'
                }
        }

    }
    post {
        success {
            deploy adapters: [tomcat9(credentialsId: 'local-tomcat-creds', path: '',
            url: 'http://localhost:8080')],
            contextPath: '/jdbc',
            onFailure: false,
            war: '**/*.war'
        }
    }
}