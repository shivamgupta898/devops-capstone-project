pipeline {
    agent any

    environment {
        BACKEND_IMAGE  = 'shivamgupta898/simple-java-app'
        FRONTEND_IMAGE = 'shivamgupta898/capstone-frontend'
        SCANNER_HOME   = tool 'sonar-scanner'
        S3_BUCKET      = 'shivam-capstone-artifacts-2026'
        AWS_DEFAULT_REGION = 'ap-south-1'
    }

    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test Backend') {
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

        stage('Build & Push Docker Images') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                    sh """
                        echo "\$DOCKER_PASSWORD" | docker login -u "\$DOCKER_USERNAME" --password-stdin
                        
                        # Backend Image Build & Push
                        docker build -t ${BACKEND_IMAGE}:${BUILD_NUMBER} -t ${BACKEND_IMAGE}:latest .
                        docker push ${BACKEND_IMAGE}:${BUILD_NUMBER}
                        docker push ${BACKEND_IMAGE}:latest

                        # Frontend Image Build & Push
                        docker build -t ${FRONTEND_IMAGE}:${BUILD_NUMBER} -t ${FRONTEND_IMAGE}:latest ./frontend
                        docker push ${FRONTEND_IMAGE}:${BUILD_NUMBER}
                        docker push ${FRONTEND_IMAGE}:latest
                    """
                }
            }
        }

        stage('Deploy 3-Tier to Kubernetes') {
            steps {
                sh """
                    # 1. Tier 3: Database Rollout
                    kubectl apply -f k8s/mysql-deployment.yml
                    kubectl rollout status deployment/mysql --timeout=120s

                    # 2. Tier 2: Backend REST API Rollout (ClusterIP)
                    kubectl apply -f k8s/deployment.yml
                    kubectl rollout restart deployment/capstone-app
                    kubectl rollout status deployment/capstone-app --timeout=90s

                    # 3. Tier 1: Frontend Web UI Rollout (NodePort 30080)
                    kubectl apply -f k8s/frontend-deployment.yml
                    kubectl rollout restart deployment/capstone-frontend
                    kubectl rollout status deployment/capstone-frontend --timeout=90s
                """
            }
        }
    }

    post {
        success {
            echo "3-Tier Microservices Architecture deployed successfully to Kubernetes!"
        }
        failure {
            echo "Pipeline execution failed. Check console output."
        }
    }
}