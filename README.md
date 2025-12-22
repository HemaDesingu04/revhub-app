# RevHub - Full Stack Application

A complete social platform built with Angular frontend and Spring Boot backend.

## Architecture
- **Frontend**: Angular 18 + Nginx
- **Backend**: Spring Boot + Java 17
- **Databases**: MySQL + MongoDB
- **Deployment**: AWS EC2 + Docker

## Quick Start

### Local Development
```bash
docker-compose up -d
```

### Production Deployment
```bash
# 1. Deploy AWS infrastructure
terraform apply -auto-approve

# 2. Run deployment script
./deploy.sh
```

## Access
- **Frontend**: http://localhost (production: http://EC2_IP)
- **Backend**: http://localhost:8080 (production: http://EC2_IP:8080)

## Repository Structure
```
├── revHubBack/          # Spring Boot Backend
├── RevHub/              # Angular Frontend
├── docker-compose.yml   # Local development
├── docker-compose.prod.yml # Production deployment
├── Jenkinsfile         # CI/CD pipeline
├── main.tf             # AWS infrastructure
└── deploy.sh           # Deployment script
```

## Deployment Options
1. **Manual**: Run `deploy.sh`
2. **Jenkins**: Automated CI/CD pipeline
3. **GitHub Actions**: Push to trigger deployment

See `SINGLE_REPO_DEPLOYMENT.md` for detailed instructions.