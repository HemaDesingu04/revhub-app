pipeline {
    agent any
    
    environment {
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials')
        AWS_CREDENTIALS = credentials('aws-credentials')
        EC2_HOST = credentials('ec2-host')
        EC2_SSH_KEY = credentials('ec2-ssh-key')
        EC2_USER = 'ubuntu'
        DOCKER_HUB_REPO = 'hemalakshmi08'
        BACKEND_IMAGE = "${DOCKER_HUB_REPO}/revhub-backend"
        FRONTEND_IMAGE = "${DOCKER_HUB_REPO}/revhub-frontend"
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo "Building from branch: ${env.BRANCH_NAME}"
            }
        }
        
        stage('Test Backend') {
            steps {
                dir('revHubBack') {
                    bat './mvnw.cmd test'
                }
            }
            post {
                always {
                    junit testResults: 'revHubBack/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Test Frontend') {
            steps {
                dir('RevHub') {
                    bat 'npm ci'
                    bat 'npm run test -- --watch=false --browsers=ChromeHeadless'
                }
            }
        }
        
        stage('Build Backend Image') {
            steps {
                dir('revHubBack') {
                    script {
                        def backendImage = docker.build("${BACKEND_IMAGE}:${BUILD_NUMBER}")
                        backendImage.tag("latest")
                    }
                }
            }
        }
        
        stage('Build Frontend Image') {
            steps {
                dir('RevHub') {
                    script {
                        def frontendImage = docker.build("${FRONTEND_IMAGE}:${BUILD_NUMBER}")
                        frontendImage.tag("latest")
                    }
                }
            }
        }
        
        stage('Security Scan') {
            parallel {
                stage('Scan Backend') {
                    steps {
                        bat "docker run --rm aquasec/trivy image ${BACKEND_IMAGE}:${BUILD_NUMBER}"
                    }
                }
                stage('Scan Frontend') {
                    steps {
                        bat "docker run --rm aquasec/trivy image ${FRONTEND_IMAGE}:${BUILD_NUMBER}"
                    }
                }
            }
        }
        
        stage('Push to Docker Hub') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                }
            }
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-credentials') {
                        bat "docker push ${BACKEND_IMAGE}:${BUILD_NUMBER}"
                        bat "docker push ${BACKEND_IMAGE}:latest"
                        bat "docker push ${FRONTEND_IMAGE}:${BUILD_NUMBER}"
                        bat "docker push ${FRONTEND_IMAGE}:latest"
                    }
                }
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    deployToEnvironment('staging')
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                }
            }
            steps {
                script {
                    deployToEnvironment('production')
                }
            }
        }
    }
    
    post {
        always {
            bat 'docker system prune -f'
            cleanWs()
        }
        success {
            echo "✅ RevHub deployment successful! Build: ${BUILD_NUMBER}"
        }
        failure {
            echo "❌ RevHub deployment failed! Build: ${BUILD_NUMBER}"
        }
    }
}

def deployToEnvironment(environment) {
    echo "Deploying to ${environment} environment"
    echo "Backend Image: ${BACKEND_IMAGE}:${BUILD_NUMBER}"
    echo "Frontend Image: ${FRONTEND_IMAGE}:${BUILD_NUMBER}"
}