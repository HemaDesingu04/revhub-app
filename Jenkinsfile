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
                    sh './mvnw test'
                }
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'revHubBack/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Test Frontend') {
            steps {
                dir('RevHub') {
                    sh 'npm ci'
                    sh 'npm run test -- --watch=false --browsers=ChromeHeadless'
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
                        sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy image ${BACKEND_IMAGE}:${BUILD_NUMBER}"
                    }
                }
                stage('Scan Frontend') {
                    steps {
                        sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy image ${FRONTEND_IMAGE}:${BUILD_NUMBER}"
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
                        sh "docker push ${BACKEND_IMAGE}:${BUILD_NUMBER}"
                        sh "docker push ${BACKEND_IMAGE}:latest"
                        sh "docker push ${FRONTEND_IMAGE}:${BUILD_NUMBER}"
                        sh "docker push ${FRONTEND_IMAGE}:latest"
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
            sh 'docker system prune -f'
            cleanWs()
        }
        success {
            slackSend(
                channel: '#deployments',
                color: 'good',
                message: "✅ RevHub deployment successful! Build: ${BUILD_NUMBER}"
            )
        }
        failure {
            slackSend(
                channel: '#deployments',
                color: 'danger',
                message: "❌ RevHub deployment failed! Build: ${BUILD_NUMBER}"
            )
        }
    }
}

def deployToEnvironment(environment) {
    sshagent([EC2_SSH_KEY]) {
        sh """
            ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} '
                # Create deployment directory if it doesn't exist
                mkdir -p /home/ubuntu/revhub-${environment}
                cd /home/ubuntu/revhub-${environment}
                
                # Download docker-compose file
                curl -O https://raw.githubusercontent.com/HemaDesingu04/revhub-app/main/docker-compose.prod.yml
                
                # Update image tags
                sed -i "s|image: .*revhub-backend.*|image: ${BACKEND_IMAGE}:${BUILD_NUMBER}|g" docker-compose.prod.yml
                sed -i "s|image: .*revhub-frontend.*|image: ${FRONTEND_IMAGE}:${BUILD_NUMBER}|g" docker-compose.prod.yml
                
                # Deploy
                docker-compose -f docker-compose.prod.yml pull
                docker-compose -f docker-compose.prod.yml up -d
                
                # Health check
                sleep 30
                curl -f http://localhost:8080/api/actuator/health || exit 1
                curl -f http://localhost/ || exit 1
                
                # Clean up
                docker image prune -f
            '
        """
    }
}