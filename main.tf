terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# VPC
resource "aws_vpc" "revhub_vpc" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "revhub-vpc"
  }
}

# Internet Gateway
resource "aws_internet_gateway" "revhub_igw" {
  vpc_id = aws_vpc.revhub_vpc.id

  tags = {
    Name = "revhub-igw"
  }
}

# Public Subnet
resource "aws_subnet" "public_subnet" {
  vpc_id                  = aws_vpc.revhub_vpc.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "${var.aws_region}a"
  map_public_ip_on_launch = true

  tags = {
    Name = "revhub-public-subnet"
  }
}

# Route Table
resource "aws_route_table" "public_rt" {
  vpc_id = aws_vpc.revhub_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.revhub_igw.id
  }

  tags = {
    Name = "revhub-public-rt"
  }
}

# Route Table Association
resource "aws_route_table_association" "public_rta" {
  subnet_id      = aws_subnet.public_subnet.id
  route_table_id = aws_route_table.public_rt.id
}

# Security Group for EC2
resource "aws_security_group" "revhub_sg" {
  name        = "revhub-ec2-sg"
  description = "Security group for RevHub EC2 instance"
  vpc_id      = aws_vpc.revhub_vpc.id

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "revhub-sg"
  }
}

# Key Pair
resource "aws_key_pair" "revhub_key" {
  key_name   = "revhub-key"
  public_key = file("~/.ssh/id_rsa.pub")
}

# EC2 Instance
resource "aws_instance" "revhub_ec2" {
  ami                    = "ami-0c02fb55956c7d316" # Ubuntu 20.04 LTS
  instance_type          = "t3.medium"
  key_name              = aws_key_pair.revhub_key.key_name
  vpc_security_group_ids = [aws_security_group.revhub_sg.id]
  subnet_id             = aws_subnet.public_subnet.id

  user_data = <<-EOF
              #!/bin/bash
              apt-get update
              apt-get install -y docker.io docker-compose git
              systemctl start docker
              systemctl enable docker
              usermod -aG docker ubuntu
              
              # Install Docker Compose
              curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
              chmod +x /usr/local/bin/docker-compose
              
              # Clone repository
              cd /home/ubuntu
              git clone https://github.com/HemaDesingu04/revhub-fullstack.git revhub
              chown -R ubuntu:ubuntu revhub
              EOF

  tags = {
    Name = "revhub-ec2"
  }
}