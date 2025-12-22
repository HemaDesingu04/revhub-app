#!/bin/bash

# EC2 Setup Script for RevHub Application
# Run this script on your EC2 instance to prepare it for deployment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 Setting up EC2 instance for RevHub deployment${NC}"

# Update system
echo -e "${YELLOW}📦 Updating system packages...${NC}"
sudo apt-get update -y
sudo apt-get upgrade -y

# Install Docker
echo -e "${YELLOW}🐳 Installing Docker...${NC}"
if ! command -v docker &> /dev/null; then
    # Add Docker's official GPG key
    sudo apt-get install ca-certificates curl gnupg lsb-release -y
    sudo mkdir -p /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    
    # Add Docker repository
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    
    # Install Docker Engine
    sudo apt-get update -y
    sudo apt-get install docker-ce docker-ce-cli containerd.io docker-compose-plugin -y
    
    # Add user to docker group
    sudo usermod -aG docker ubuntu
    
    # Start and enable Docker
    sudo systemctl start docker
    sudo systemctl enable docker
    
    echo -e "${GREEN}✅ Docker installed successfully${NC}"
else
    echo -e "${GREEN}✅ Docker is already installed${NC}"
fi

# Install Docker Compose (standalone)
echo -e "${YELLOW}🔧 Installing Docker Compose...${NC}"
if ! command -v docker-compose &> /dev/null; then
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    echo -e "${GREEN}✅ Docker Compose installed successfully${NC}"
else
    echo -e "${GREEN}✅ Docker Compose is already installed${NC}"
fi

# Install AWS CLI
echo -e "${YELLOW}☁️  Installing AWS CLI...${NC}"
if ! command -v aws &> /dev/null; then
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
    sudo apt-get install unzip -y
    unzip awscliv2.zip
    sudo ./aws/install
    rm -rf awscliv2.zip aws/
    echo -e "${GREEN}✅ AWS CLI installed successfully${NC}"
else
    echo -e "${GREEN}✅ AWS CLI is already installed${NC}"
fi

# Install other useful tools
echo -e "${YELLOW}🛠️  Installing additional tools...${NC}"
sudo apt-get install -y \
    curl \
    wget \
    git \
    htop \
    nano \
    vim \
    jq \
    tree \
    net-tools \
    nginx

# Configure firewall
echo -e "${YELLOW}🔥 Configuring firewall...${NC}"
sudo ufw allow ssh
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 8080/tcp
sudo ufw --force enable

# Create application directories
echo -e "${YELLOW}📁 Creating application directories...${NC}"
mkdir -p /home/ubuntu/revhub-production
mkdir -p /home/ubuntu/revhub-staging
mkdir -p /home/ubuntu/logs
mkdir -p /home/ubuntu/backups

# Set up log rotation
echo -e "${YELLOW}📝 Setting up log rotation...${NC}"
sudo tee /etc/logrotate.d/revhub > /dev/null <<EOF
/home/ubuntu/logs/*.log {
    daily
    missingok
    rotate 7
    compress
    delaycompress
    notifempty
    copytruncate
}
EOF

# Create monitoring script
echo -e "${YELLOW}📊 Creating monitoring script...${NC}"
tee /home/ubuntu/monitor.sh > /dev/null <<'EOF'
#!/bin/bash

# Simple monitoring script for RevHub
echo "=== RevHub System Status ==="
echo "Date: $(date)"
echo ""

echo "=== System Resources ==="
echo "CPU Usage:"
top -bn1 | grep "Cpu(s)" | awk '{print $2 $3}'
echo ""

echo "Memory Usage:"
free -h
echo ""

echo "Disk Usage:"
df -h /
echo ""

echo "=== Docker Status ==="
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ""

echo "=== Service Health Checks ==="
echo -n "Backend Health: "
curl -s -f http://localhost:8080/api/actuator/health > /dev/null && echo "✅ OK" || echo "❌ FAILED"

echo -n "Frontend Health: "
curl -s -f http://localhost/ > /dev/null && echo "✅ OK" || echo "❌ FAILED"

echo ""
echo "=== Recent Logs ==="
echo "Backend logs (last 5 lines):"
docker logs revhub-backend --tail 5 2>/dev/null || echo "Backend container not running"

echo ""
echo "Frontend logs (last 5 lines):"
docker logs revhub-frontend --tail 5 2>/dev/null || echo "Frontend container not running"
EOF

chmod +x /home/ubuntu/monitor.sh

# Create backup script
echo -e "${YELLOW}💾 Creating backup script...${NC}"
tee /home/ubuntu/backup.sh > /dev/null <<'EOF'
#!/bin/bash

# Backup script for RevHub
BACKUP_DIR="/home/ubuntu/backups"
DATE=$(date +%Y%m%d_%H%M%S)

echo "Starting backup at $(date)"

# Create backup directory
mkdir -p $BACKUP_DIR

# Backup MySQL database
echo "Backing up MySQL database..."
docker exec revhub-mysql mysqldump -u root -prootpassword123 revhubteam4 > $BACKUP_DIR/mysql_backup_$DATE.sql

# Backup MongoDB database
echo "Backing up MongoDB database..."
docker exec revhub-mongo mongodump --db revhubteam4 --out /tmp/mongo_backup_$DATE
docker cp revhub-mongo:/tmp/mongo_backup_$DATE $BACKUP_DIR/

# Backup application configuration
echo "Backing up configuration files..."
tar -czf $BACKUP_DIR/config_backup_$DATE.tar.gz /home/ubuntu/revhub-production /home/ubuntu/revhub-staging

# Clean up old backups (keep last 7 days)
find $BACKUP_DIR -name "*backup*" -mtime +7 -delete

echo "Backup completed at $(date)"
EOF

chmod +x /home/ubuntu/backup.sh

# Set up cron jobs
echo -e "${YELLOW}⏰ Setting up cron jobs...${NC}"
(crontab -l 2>/dev/null; echo "0 2 * * * /home/ubuntu/backup.sh >> /home/ubuntu/logs/backup.log 2>&1") | crontab -
(crontab -l 2>/dev/null; echo "*/5 * * * * /home/ubuntu/monitor.sh >> /home/ubuntu/logs/monitor.log 2>&1") | crontab -

# Configure Nginx (optional reverse proxy)
echo -e "${YELLOW}🌐 Configuring Nginx...${NC}"
sudo tee /etc/nginx/sites-available/revhub > /dev/null <<'EOF'
server {
    listen 80;
    server_name _;

    # Frontend
    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }

    # Backend API
    location /api {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }

    # Health check endpoint
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
EOF

# Enable the site
sudo ln -sf /etc/nginx/sites-available/revhub /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t && sudo systemctl reload nginx

# Create environment file template
echo -e "${YELLOW}📄 Creating environment template...${NC}"
tee /home/ubuntu/.env.template > /dev/null <<'EOF'
# Copy this file to .env and update the values

# Database Configuration
MYSQL_ROOT_PASSWORD=rootpassword123
MYSQL_DATABASE=revhubteam4
MYSQL_USER=revhub
MYSQL_PASSWORD=revhubpass

# MongoDB Configuration
MONGO_ROOT_USERNAME=root
MONGO_ROOT_PASSWORD=rootpassword123
MONGO_DATABASE=revhubteam4

# Application Configuration
JWT_SECRET=your-super-secret-jwt-key-here
JWT_EXPIRATION=86400
CORS_ALLOWED_ORIGINS=http://your-domain.com

# Email Configuration
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password

# Docker Hub
DOCKER_HUB_USERNAME=your-dockerhub-username
EOF

# Set up SSH key for GitHub (optional)
echo -e "${YELLOW}🔑 Setting up SSH key for GitHub...${NC}"
if [ ! -f /home/ubuntu/.ssh/id_rsa ]; then
    ssh-keygen -t rsa -b 4096 -C "ubuntu@$(hostname)" -f /home/ubuntu/.ssh/id_rsa -N ""
    echo -e "${YELLOW}📋 Add this public key to your GitHub account:${NC}"
    cat /home/ubuntu/.ssh/id_rsa.pub
fi

# Final system configuration
echo -e "${YELLOW}⚙️  Final system configuration...${NC}"

# Increase file limits
echo "ubuntu soft nofile 65536" | sudo tee -a /etc/security/limits.conf
echo "ubuntu hard nofile 65536" | sudo tee -a /etc/security/limits.conf

# Configure swap (if not exists)
if [ ! -f /swapfile ]; then
    sudo fallocate -l 2G /swapfile
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
fi

# Set timezone
sudo timedatectl set-timezone UTC

echo -e "${GREEN}🎉 EC2 setup completed successfully!${NC}"
echo -e "${YELLOW}📋 Next steps:${NC}"
echo "1. Copy your .env file to /home/ubuntu/revhub-production/.env"
echo "2. Update Docker Hub username in your deployment scripts"
echo "3. Configure your CI/CD pipeline with this EC2 instance"
echo "4. Run your deployment script or trigger your CI/CD pipeline"
echo ""
echo -e "${YELLOW}📊 Useful commands:${NC}"
echo "- Monitor system: /home/ubuntu/monitor.sh"
echo "- Backup data: /home/ubuntu/backup.sh"
echo "- View logs: docker logs <container-name>"
echo "- Check services: docker ps"
echo ""
echo -e "${YELLOW}🔄 Please reboot the system to ensure all changes take effect:${NC}"
echo "sudo reboot"