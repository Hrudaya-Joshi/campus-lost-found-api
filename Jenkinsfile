pipeline {
    agent any

    triggers {
        // Poll SCM every 2 minutes for changes
        pollSCM('H/2 * * * *')
    }

    environment {
        APP_NAME = 'campus-lost-found-api'
    }

    stages {
        stage('Checkout SCM') {
            steps {
                checkout scm
            }
        }

        stage('Maven Build') {
            steps {
                echo 'Compiling Campus Lost & Found API...'
                script {
                    if (isUnix()) {
                        sh './mvnw clean compile || mvn clean compile'
                    } else {
                        bat 'mvn.cmd clean compile'
                    }
                }
            }
        }

        stage('Automated Testing') {
            steps {
                echo 'Executing automated unit and integration tests...'
                script {
                    if (isUnix()) {
                        sh './mvnw test || mvn test'
                    } else {
                        bat 'mvn.cmd test'
                    }
                }
            }
        }

        stage('Package Application') {
            steps {
                echo 'Packaging executable Spring Boot JAR...'
                script {
                    if (isUnix()) {
                        sh './mvnw package -DskipTests || mvn package -DskipTests'
                    } else {
                        bat 'mvn.cmd package -DskipTests'
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Publishing JUnit test reports...'
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
        }
        success {
            echo 'Archiving built executable JAR artifact...'
            archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true, allowEmptyArchive: false
            echo "CI Pipeline completed successfully for ${env.JOB_NAME} build #${env.BUILD_NUMBER}."
        }
        failure {
            echo "CI Pipeline failed on ${env.JOB_NAME} build #${env.BUILD_NUMBER}. Please check logs for errors."
        }
    }
}
