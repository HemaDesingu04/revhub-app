#!/bin/bash

# RevHub Deployment Script
# This script deploys the RevHub application to AWS EC2

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
DOCKER_HUB_USERNAME=${DOCKER_HUB_USERNAME:-"hemalakshmi08"}
BUILD_NUMBER=${BUILD_NUMBER:-$(date +%Y%m%d%H%M%S)}
ENVIRONMENT=${ENVIRONMENT:-"production"}

echo -e "${GREEN}🚀 Starting RevHub Deployment${NC}"
echo -e "${YELLOW}Environment: $ENVIRONMENT${NC}"
echo -e "${YELLOW}Build Number: $BUILD_NUMBER${NC}"

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo -e "${YELLOW}📋 Checking prerequisites...${NC}"

if ! command_exists docker; then
    echo -e "${RED}❌ Docker is not installed${NC}"
    exit 1
fi

if ! command_exists docker-compose; then
    echo -e "${RED}❌ Docker Compose is not installed${NC}"
    exit 1
fi

if ! command_exists aws; then
    echo -e "${RED}❌ AWS CLI is not installed${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Prerequisites check passed${NC}"

# Build and push Docker images
echo -e "${YELLOW}🔨 Building Docker images...${NC}"

# Build backend
echo -e "${YELLOW}Building backend image...${NC}"
docker build -t ${DOCKER_HUB_USERNAME}/revhub-backend:${BUILD_NUMBER} ./revHubBack
docker tag ${DOCKER_HUB_USERNAME}/revhub-backend:${BUILD_NUMBER} ${DOCKER_HUB_USERNAME}/revhub-backend:latest

# Build frontend
echo -e "${YELLOW}Building frontend image...${NC}"
docker build -t ${DOCKER_HUB_USERNAME}/revhub-frontend:${BUILD_NUMBER} ./RevHub
docker tag ${DOCKER_HUB_USERNAME}/revhub-frontend:${BUILD_NUMBER} ${DOCKER_HUB_USERNAME}/revhub-frontend:latest

echo -e "${GREEN}✅ Docker images built successfully${NC}"

# Push to Docker Hub
echo -e "${YELLOW}📤 Pushing images to Docker Hub...${NC}"

if [ -z "$DOCKER_HUB_TOKEN" ]; then
    echo -e "${YELLOW}Please login to Docker Hub:${NC}"
    docker login
else
    echo "$DOCKER_HUB_TOKEN" | docker login -u "$DOCKER_HUB_USERNAME" --password-stdin
fi

docker push ${DOCKER_HUB_USERNAME}/revhub-backend:${BUILD_NUMBER}
docker push ${DOCKER_HUB_USERNAME}/revhub-backend:latest
docker push ${DOCKER_HUB_USERNAME}/revhub-frontend:${BUILD_NUMBER}
docker push ${DOCKER_HUB_USERNAME}/revhub-frontend:latest

echo -e "${GREEN}✅ Images pushed to Docker Hub${NC}"

# Deploy to AWS EC2
if [ -n "$EC2_HOST" ]; then
    echo -e "${YELLOW}🚀 Deploying to AWS EC2...${NC}"
    
    # Create deployment directory on EC2
    ssh -o StrictHostKeyChecking=no ubuntu@$EC2_HOST "mkdir -p /home/ubuntu/revhub-${ENVIRONMENT}"
    
    # Copy docker-compose file to EC2
    scp docker-compose.prod.yml ubuntu@$EC2_HOST:/home/ubuntu/revhub-${ENVIRONMENT}/
    scp .env.example ubuntu@$EC2_HOST:/home/ubuntu/revhub-${ENVIRONMENT}/.env
    
    # Update image tags and deploy
    ssh -o StrictHostKeyChecking=no ubuntu@$EC2_HOST "
        cd /home/ubuntu/revhub-${ENVIRONMENT}
        
        # Update image tags
        sed -i 's|image: .*revhub-backend.*|image: ${DOCKER_HUB_USERNAME}/revhub-backend:${BUILD_NUMBER}|g' docker-compose.prod.yml
        sed -i 's|image: .*revhub-frontend.*|image: ${DOCKER_HUB_USERNAME}/revhub-frontend:${BUILD_NUMBER}|g' docker-compose.prod.yml
        
        # Deploy
        docker-compose -f docker-compose.prod.yml pull
        docker-compose -f docker-compose.prod.yml up -d
        
        # Wait for services to start
        echo 'Waiting for services to start...'
        sleep 60
        
        # Health check
        echo 'Performing health checks...'
        curl -f http://localhost:8080/api/actuator/health || echo 'Backend health check failed'
        curl -f http://localhost/ || echo 'Frontend health check failed'
        
        # Clean up old images
        docker image prune -f
    "
    
    echo -e "${GREEN}✅ Deployment to EC2 completed${NC}"
else
    echo -e "${YELLOW}⚠️  EC2_HOST not set, skipping EC2 deployment${NC}"
fi

# Run local deployment if no EC2_HOST
if [ -z "$EC2_HOST" ]; then
    echo -e "${YELLOW}🏠 Running local deployment...${NC}"
    
    # Update docker-compose with new image tags
    sed -i.bak "s|image: .*revhub-backend.*|image: ${DOCKER_HUB_USERNAME}/revhub-backend:${BUILD_NUMBER}|g" docker-compose.prod.yml
    sed -i.bak "s|image: .*revhub-frontend.*|image: ${DOCKER_HUB_USERNAME}/revhub-frontend:${BUILD_NUMBER}|g" docker-compose.prod.yml
    
    # Deploy locally
    docker-compose -f docker-compose.prod.yml pull
    docker-compose -f docker-compose.prod.yml up -d
    
    echo -e "${GREEN}✅ Local deployment completed${NC}"
    echo -e "${YELLOW}Frontend: http://localhost${NC}"
    echo -e "${YELLOW}Backend: http://localhost:8080${NC}"
fi

# Cleanup
echo -e "${YELLOW}🧹 Cleaning up...${NC}"
docker system prune -f

echo -e "${GREEN}🎉 Deployment completed successfully!${NC}"

# Display deployment information
echo -e "${YELLOW}📊 Deployment Summary:${NC}"
echo -e "Environment: $ENVIRONMENT"
echo -e "Build Number: $BUILD_NUMBER"
echo -e "Backend Image: ${DOCKER_HUB_USERNAME}/revhub-backend:${BUILD_NUMBER}"
echo -e "Frontend Image: ${DOCKER_HUB_USERNAME}/revhub-frontend:${BUILD_NUMBER}"

if [ -n "$EC2_HOST" ]; then
    echo -e "EC2 Host: $EC2_HOST"
    echo -e "Frontend URL: http://$EC2_HOST"
    echo -e "Backend URL: http://$EC2_HOST:8080"
fi