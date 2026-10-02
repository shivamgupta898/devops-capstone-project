pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'shivamgupta898/simple-java-app'
        SCANNER_HOME = tool 'sonar-scanner'
        S3_BUCKET = 'shivam-capstone-artifacts-2026'
        AWS_DEFAULT_REGION = 'ap-south-1'
    }

    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean package -DskipTests=false'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Archive Artifact to AWS S3') {
            steps {
                withCredentials([
                    string(credentialsId: 'aws-access-key-id', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'aws-secret-access-key', variable: 'AWS_SECRET_ACCESS_KEY')
                ]) {
                    sh """
                        echo "Uploading build artifact to AWS S3 bucket: ${S3_BUCKET}..."
                        aws s3 cp target/simple-java-app-1.0.0.jar s3://${S3_BUCKET}/builds/build-${BUILD_NUMBER}/simple-java-app-${BUILD_NUMBER}.jar
                        aws s3 cp target/simple-java-app-1.0.0.jar s3://${S3_BUCKET}/builds/latest/simple-java-app.jar
                        aws s3 ls s3://${S3_BUCKET}/builds/build-${BUILD_NUMBER}/
                    """
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh """
                        ${SCANNER_HOME}/bin/sonar-scanner \
                        -Dsonar.projectKey=devops-capstone-app \
                        -Dsonar.projectName=devops-capstone-app \
                        -Dsonar.sources=src/main/java \
                        -Dsonar.java.binaries=target/classes
                    """
                }
            }
        }

        stage('Build & Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                    sh """
                        echo "\$DOCKER_PASSWORD" | docker login -u "\$DOCKER_USERNAME" --password-stdin
                        docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} -t ${DOCKER_IMAGE}:latest .
                        docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}
                        docker push ${DOCKER_IMAGE}:latest
                    """
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh """
                    kubectl apply -f k8s/deployment.yml
                    kubectl rollout restart deployment/capstone-app
                    kubectl rollout status deployment/capstone-app --timeout=90s
                """
            }
        }
    }

    post {
        success {
            echo "CI/CD Pipeline executed successfully! Application deployed to Kubernetes and artifact archived to S3."
        }
        failure {
            echo "Pipeline execution failed. Check console output."
        }
    }
}