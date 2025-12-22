# RevHub EC2 Deployment Guide

This guide will help you deploy the RevHub application on AWS EC2 using GitHub, Docker, Jenkins, and Docker Hub.

## Architecture Overview

- **Frontend**: Angular 18 application served via Nginx
- **Backend**: Spring Boot application with Java 17
- **Database**: MySQL + MongoDB (containerized)
- **Container Registry**: Docker Hub
- **Deployment**: AWS EC2 with Docker Compose
- **CI/CD**: Jenkins Pipeline

## Prerequisites

1. **AWS Account** with EC2 permissions
2. **AWS CLI** installed and configured
3. **Terraform** installed (v1.0+)
4. **Docker** and **Docker Hub** account
5. **Jenkins** server with required plugins
6. **GitHub** repository
7. **SSH Key Pair** for EC2 access

## Step 1: Setup AWS Infrastructure

1. **Configure AWS CLI**:
   ```bash
   aws configure
   ```

2. **Deploy Infrastructure**:
   ```bash
   terraform init
   terraform plan
   terraform apply
   ```

## Step 2: Setup Jenkins

### Required Jenkins Plugins:
- AWS Steps Plugin
- Docker Pipeline Plugin
- GitHub Integration Plugin
- Pipeline Plugin

### Jenkins Configuration:

1. **Add AWS Credentials**:
   - Go to Jenkins → Manage Jenkins → Manage Credentials
   - Add AWS Access Key and Secret Key

2. **Create Pipeline Job**:
   - New Item → Pipeline
   - Configure GitHub repository
   - Set Pipeline script from SCM
   - Point to Jenkinsfile in repository

## Step 3: Setup GitHub Repository

1. **Push code to GitHub**:
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin <your-github-repo-url>
   git push -u origin main
   ```

2. **Configure GitHub Webhooks**:
   - Repository Settings → Webhooks
   - Add Jenkins webhook URL
   - Set to trigger on push events

## Step 4: Environment Configuration

### Backend Configuration (application.properties):

Update the following properties for AWS deployment:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://<RDS_ENDPOINT>/revhubteam4?useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=rootpassword123

# MongoDB Configuration (if using DocumentDB)
spring.data.mongodb.uri=mongodb://<DOCUMENTDB_ENDPOINT>:27017/revhubteam4

# CORS Configuration
app.cors.allowed-origins=https://<ALB_DNS_NAME>
```

### Frontend Configuration:

Update API endpoints in Angular services to point to ALB:

```typescript
// environment.prod.ts
export const environment = {
  production: true,
  apiUrl: 'https://<ALB_DNS_NAME>/api'
};
```

## Step 5: Deployment Process

### Manual Deployment:
```bash
chmod +x deploy.sh
./deploy.sh
```

### Automated Deployment via Jenkins:
1. Push code to GitHub
2. Jenkins automatically triggers build
3. Pipeline builds Docker images
4. Images pushed to ECR
5. ECS services updated with new images

## Step 6: Monitoring and Logging

### CloudWatch Logs:
- Backend logs: `/ecs/revhub-backend`
- Frontend logs: `/ecs/revhub-frontend`

### Health Checks:
- Backend: `http://<ALB_DNS>/api/actuator/health`
- Frontend: `http://<ALB_DNS>/`

## Step 7: Security Considerations

1. **Secrets Management**:
   - Use AWS Secrets Manager for database passwords
   - Use AWS Parameter Store for configuration

2. **Network Security**:
   - RDS in private subnets
   - Security groups with minimal required access
   - HTTPS termination at ALB (add SSL certificate)

3. **IAM Roles**:
   - Least privilege principle
   - Separate roles for different services

## Troubleshooting

### Common Issues:

1. **ECS Task Fails to Start**:
   - Check CloudWatch logs
   - Verify environment variables
   - Check security group rules

2. **Database Connection Issues**:
   - Verify RDS security group allows ECS access
   - Check database credentials
   - Ensure RDS is in correct subnets

3. **Load Balancer Health Check Failures**:
   - Verify health check paths
   - Check application startup time
   - Review security group rules

### Useful Commands:

```bash
# Check ECS service status
aws ecs describe-services --cluster revhub-cluster --services revhub-backend-service

# View ECS task logs
aws logs get-log-events --log-group-name /ecs/revhub-backend --log-stream-name <stream-name>

# Update ECS service
aws ecs update-service --cluster revhub-cluster --service revhub-backend-service --force-new-deployment
```

## Cost Optimization

1. **Use Fargate Spot** for non-production environments
2. **Auto Scaling** based on CPU/memory utilization
3. **RDS Reserved Instances** for production
4. **CloudWatch** monitoring to optimize resource usage

## Next Steps

1. **Add HTTPS**: Configure SSL certificate with ACM
2. **Domain Setup**: Route 53 for custom domain
3. **Monitoring**: Set up CloudWatch alarms
4. **Backup**: Configure automated RDS backups
5. **CDN**: Add CloudFront for static assets

## Support

For issues and questions:
- Check CloudWatch logs
- Review AWS documentation
- Contact your DevOps team