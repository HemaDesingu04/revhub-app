# RevHub EC2 Deployment - Step by Step Guide

## 1. Prerequisites Setup

### Generate SSH Key Pair
```bash
ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa
```

### Create Docker Hub Account
- Sign up at https://hub.docker.com
- Create repositories: `revhub-backend` and `revhub-frontend`

## 2. AWS Infrastructure Deployment

### Deploy EC2 with Terraform
```bash
# Initialize Terraform
terraform init

# Plan deployment
terraform plan

# Deploy infrastructure
terraform apply -auto-approve

# Get EC2 IP
terraform output ec2_public_ip
```

## 3. Configure EC2 Instance

### SSH into EC2
```bash
ssh -i ~/.ssh/id_rsa ubuntu@<EC2_PUBLIC_IP>
```

### Run setup script on EC2
```bash
# Copy setup script to EC2
scp -i ~/.ssh/id_rsa ec2-setup.sh ubuntu@<EC2_PUBLIC_IP>:/home/ubuntu/

# SSH and run setup
ssh -i ~/.ssh/id_rsa ubuntu@<EC2_PUBLIC_IP>
chmod +x ec2-setup.sh
./ec2-setup.sh
```

## 4. GitHub Repository Setup

### Push code to GitHub
```bash
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/your-username/Team4_RevHub.git
git push -u origin main
```

### Update repository URLs in files
- Update `main.tf` line 85: Replace with your GitHub repo URL
- Update `docker-compose.prod.yml`: Replace `your-dockerhub-username` with your Docker Hub username

## 5. Jenkins Setup (Option 1)

### Install Jenkins on separate server or local machine

### Required Jenkins Plugins
- Docker Pipeline
- SSH Agent
- GitHub Integration

### Add Credentials in Jenkins
1. **Docker Hub Credentials**
   - ID: `dockerhub-credentials`
   - Username/Password: Your Docker Hub credentials

2. **EC2 SSH Key**
   - ID: `ec2-ssh-key`
   - SSH Private Key: Content of `~/.ssh/id_rsa`

3. **EC2 Public IP**
   - ID: `EC2_PUBLIC_IP`
   - Secret text: Your EC2 public IP

### Create Jenkins Pipeline
1. New Item → Pipeline
2. Pipeline script from SCM
3. Repository URL: Your GitHub repo
4. Script Path: `Jenkinsfile`

## 6. GitHub Actions Setup (Option 2)

### Add GitHub Secrets
Go to Repository → Settings → Secrets and variables → Actions

Add these secrets:
- `DOCKER_HUB_USERNAME`: Your Docker Hub username
- `DOCKER_HUB_TOKEN`: Docker Hub access token
- `EC2_HOST`: EC2 public IP
- `EC2_SSH_KEY`: Content of `~/.ssh/id_rsa` private key

### Create GitHub Actions Workflow
```bash
mkdir -p .github/workflows
cp github-actions.yml .github/workflows/deploy.yml
git add .github/workflows/deploy.yml
git commit -m "Add GitHub Actions workflow"
git push
```

## 7. Manual Deployment (Option 3)

### Build and push images locally
```bash
# Login to Docker Hub
docker login

# Build images
docker build -t your-username/revhub-backend:latest ./revHubBack
docker build -t your-username/revhub-frontend:latest ./RevHub

# Push images
docker push your-username/revhub-backend:latest
docker push your-username/revhub-frontend:latest
```

### Deploy to EC2
```bash
# Update docker-compose.prod.yml with your Docker Hub username
# Copy to EC2
scp -i ~/.ssh/id_rsa docker-compose.prod.yml ubuntu@<EC2_IP>:/home/ubuntu/revhub/

# SSH and deploy
ssh -i ~/.ssh/id_rsa ubuntu@<EC2_IP>
cd /home/ubuntu/revhub
docker-compose -f docker-compose.prod.yml up -d
```

## 8. Verify Deployment

### Check application status
```bash
# SSH to EC2
ssh -i ~/.ssh/id_rsa ubuntu@<EC2_IP>

# Check containers
docker ps

# Check logs
docker-compose -f docker-compose.prod.yml logs
```

### Access application
- **Frontend**: http://EC2_PUBLIC_IP
- **Backend API**: http://EC2_PUBLIC_IP:8080
- **Health Check**: http://EC2_PUBLIC_IP:8080/actuator/health

## 9. Monitoring and Maintenance

### View logs
```bash
# All services
docker-compose -f docker-compose.prod.yml logs

# Specific service
docker-compose -f docker-compose.prod.yml logs backend
```

### Update deployment
```bash
# Pull latest images
docker-compose -f docker-compose.prod.yml pull

# Restart services
docker-compose -f docker-compose.prod.yml up -d
```

### Backup data
```bash
# Backup MySQL
docker exec revhub-mysql mysqldump -u root -proot revhubteam4 > backup.sql

# Backup MongoDB
docker exec revhub-mongo mongodump --db revhubteam4 --out /backup
```

## 10. Troubleshooting

### Common Issues

**Container fails to start:**
```bash
docker-compose -f docker-compose.prod.yml logs <service-name>
```

**Port conflicts:**
```bash
sudo netstat -tulpn | grep :80
sudo netstat -tulpn | grep :8080
```

**Database connection issues:**
```bash
# Check if databases are running
docker ps | grep mysql
docker ps | grep mongo

# Test database connection
docker exec -it revhub-mysql mysql -u root -proot
docker exec -it revhub-mongo mongo
```

**Memory issues:**
```bash
# Check system resources
free -h
df -h
docker system df
```

### Useful Commands
```bash
# Stop all services
docker-compose -f docker-compose.prod.yml down

# Remove all containers and volumes
docker-compose -f docker-compose.prod.yml down -v

# Clean up Docker system
docker system prune -a

# Update EC2 security group if needed (via AWS Console)
# Allow ports: 22 (SSH), 80 (HTTP), 8080 (Backend)
```