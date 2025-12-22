# Single Repository Deployment Process

## Repository Structure
```
revhub-fullstack/
├── revHubBack/          # Spring Boot Backend
├── RevHub/              # Angular Frontend  
├── docker-compose.yml   # Development
├── docker-compose.prod.yml # Production
├── Jenkinsfile         # CI/CD Pipeline
├── main.tf             # AWS Infrastructure
├── deploy.sh           # Deployment Script
└── README.md
```

## Complete Deployment Process

### 1. Create Single GitHub Repository (2 minutes)
```bash
# Create new repository on GitHub: revhub-fullstack
# Push your current code
git init
git add .
git commit -m "Initial fullstack commit"
git remote add origin https://github.com/YOUR-USERNAME/revhub-fullstack.git
git push -u origin main
```

### 2. Update Configuration (1 minute)
Replace `YOUR-USERNAME` and `YOUR-DOCKERHUB-USERNAME` in:
- `main.tf` (line 85)
- `docker-compose.prod.yml` 
- `deploy.sh`

### 3. Deploy Infrastructure (5 minutes)
```bash
terraform init
terraform apply -auto-approve
EC2_IP=$(terraform output -raw ec2_public_ip)
```

### 4. Setup EC2 (3 minutes)
```bash
scp -i ~/.ssh/id_rsa ec2-setup.sh ubuntu@$EC2_IP:/home/ubuntu/
ssh -i ~/.ssh/id_rsa ubuntu@$EC2_IP "chmod +x ec2-setup.sh && ./ec2-setup.sh"
```

### 5. Deploy Application (5 minutes)
```bash
# Build and push images
docker login
docker build -t YOUR-DOCKERHUB-USERNAME/revhub-backend:latest ./revHubBack
docker build -t YOUR-DOCKERHUB-USERNAME/revhub-frontend:latest ./RevHub
docker push YOUR-DOCKERHUB-USERNAME/revhub-backend:latest
docker push YOUR-DOCKERHUB-USERNAME/revhub-frontend:latest

# Deploy to EC2
scp -i ~/.ssh/id_rsa docker-compose.prod.yml ubuntu@$EC2_IP:/home/ubuntu/revhub/
ssh -i ~/.ssh/id_rsa ubuntu@$EC2_IP "cd /home/ubuntu/revhub && docker-compose -f docker-compose.prod.yml up -d"
```

### 6. Access Application
- Frontend: http://EC2_IP
- Backend: http://EC2_IP:8080

## CI/CD Options

### Jenkins Pipeline
- Single pipeline builds both frontend and backend
- Deploys to single EC2 instance
- Triggered by GitHub webhook

### GitHub Actions
- Single workflow file
- Builds both applications
- Deploys to EC2 via SSH

## Total Time: ~15 minutes for complete deployment