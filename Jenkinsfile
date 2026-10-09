pipeline {
    agent any

    tools {
        jdk 'JDK-21'
        maven 'Maven-3.9.16'
    }

    environment {
        DOCKER_CREDS = credentials('dockerhub-creds')
        AWS_KEY      = credentials('aws-access-key-id')
        AWS_SECRET   = credentials('aws-secret-access-key')
        GITHUB_CREDS = credentials('github-credentials')
        DOCKER_USER  = 'shivamgupta898'
        S3_BUCKET    = 'shivam-capstone-artifacts-2026'
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
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Archive Artifact to AWS S3') {
            steps {
                sh """
                    export AWS_ACCESS_KEY_ID=\$AWS_KEY
                    export AWS_SECRET_ACCESS_KEY=\$AWS_SECRET
                    echo "Uploading build artifact to AWS S3 bucket: ${S3_BUCKET}..."
                    aws s3 cp target/simple-java-app-1.0.0.jar s3://${S3_BUCKET}/builds/build-${BUILD_NUMBER}/simple-java-app-${BUILD_NUMBER}.jar
                    aws s3 cp target/simple-java-app-1.0.0.jar s3://${S3_BUCKET}/builds/latest/simple-java-app.jar
                    aws s3 ls s3://${S3_BUCKET}/builds/build-${BUILD_NUMBER}/
                """
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '/var/lib/jenkins/tools/hudson.plugins.sonar.SonarRunnerInstallation/sonar-scanner/bin/sonar-scanner \
                        -Dsonar.projectKey=devops-capstone-app \
                        -Dsonar.projectName=devops-capstone-app \
                        -Dsonar.sources=src/main/java \
                        -Dsonar.java.binaries=target/classes'
                }
            }
        }

        stage('Build & Push Docker Images') {
            steps {
                sh """
                    echo "$DOCKER_CREDS_PSW" | docker login -u "$DOCKER_CREDS_USR" --password-stdin

                    # Backend Image
                    docker build -t ${DOCKER_USER}/simple-java-app:${BUILD_NUMBER} -t ${DOCKER_USER}/simple-java-app:latest .
                    docker push ${DOCKER_USER}/simple-java-app:${BUILD_NUMBER}
                    docker push ${DOCKER_USER}/simple-java-app:latest

                    # Frontend Image
                    docker build -t ${DOCKER_USER}/capstone-frontend:${BUILD_NUMBER} -t ${DOCKER_USER}/capstone-frontend:latest ./frontend
                    docker push ${DOCKER_USER}/capstone-frontend:${BUILD_NUMBER}
                    docker push ${DOCKER_USER}/capstone-frontend:latest
                """
            }
        }

        stage('Update Git Manifests for ArgoCD') {
            steps {
                sh """
                    echo "Updating Kubernetes manifests with new build tag: ${BUILD_NUMBER}"

                    # Update deployment image tags
                    sed -i "s|image: ${DOCKER_USER}/simple-java-app:.*|image: ${DOCKER_USER}/simple-java-app:${BUILD_NUMBER}|g" k8s/deployment.yml
                    sed -i "s|image: ${DOCKER_USER}/capstone-frontend:.*|image: ${DOCKER_USER}/capstone-frontend:${BUILD_NUMBER}|g" k8s/frontend-deployment.yml

                    # Configure git identity
                    git config user.name "jenkins-bot"
                    git config user.email "jenkins@capstone.local"

                    # Commit and push via authenticated GitHub credentials
                    git add k8s/deployment.yml k8s/frontend-deployment.yml
                    git commit -m "ci(argocd): bump container images to build-${BUILD_NUMBER} [skip ci]" || echo "No changes to commit"
                    git push https://${GITHUB_CREDS_USR}:${GITHUB_CREDS_PSW}@github.com/shivamgupta898/devops-capstone-project.git HEAD:main
                """
            }
        }
    }

    post {
        success {
            echo "CI pipeline completed successfully! ArgoCD is reconciling the deployment on Kubernetes."
        }
        failure {
            echo "Pipeline failed. Review build console logs."
        }
    }
}