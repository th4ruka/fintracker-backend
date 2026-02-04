# Docker Deployment Guide

## Overview

This guide covers deploying Fin-Tracker Backend using Docker and Docker Compose.

**When to use Docker vs JAR:**
- **Use JAR deployment** (systemd): Limited resources (1GB RAM), single app, quick setup
- **Use Docker**: Need portability, multiple environments, have 2GB+ RAM

---

## Quick Start (Local Development)

### Prerequisites
- Docker installed (20.10+)
- Docker Compose installed (2.0+)

### 1. Create Environment File

```bash
# Copy example environment file
cp deployment/.env.example .env

# Edit with your values
nano .env
```

Minimum required in `.env`:
```bash
DB_PASSWORD=your_secure_password
MYSQL_ROOT_PASSWORD=root_secure_password
APP_DEFAULT_CURRENCY=LKR
```

### 2. Start All Services

```bash
# Build and start all services (app, MySQL, nginx)
docker-compose up -d

# View logs
docker-compose logs -f

# Check status
docker-compose ps
```

### 3. Test the Application

```bash
# Test directly
curl http://localhost:8080/api/v1/users

# Test via Nginx (if enabled)
curl http://localhost/api/v1/users

# Health check
curl http://localhost:8080/actuator/health
```

### 4. Stop Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: deletes database)
docker-compose down -v
```

---

## Building Docker Image

### Manual Build

```bash
# Build image
docker build -t fintracker-backend:latest .

# Run container (without MySQL)
docker run -d \
  --name fintracker-app \
  -p 8080:8080 \
  -e DB_HOST=your-mysql-host \
  -e DB_PORT=3306 \
  -e DB_NAME=fin_tracker \
  -e DB_USERNAME=fintracker \
  -e DB_PASSWORD=your_password \
  fintracker-backend:latest

# View logs
docker logs -f fintracker-app

# Stop and remove
docker stop fintracker-app
docker rm fintracker-app
```

### Multi-Architecture Build (ARM + AMD)

```bash
# Create builder
docker buildx create --name multiarch --use

# Build for multiple architectures
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t fintracker-backend:latest \
  --push \
  .
```

---

## Deploy to Oracle Cloud with Docker

### Option A: Docker without Compose (Minimal)

**1. Install Docker on Oracle Cloud Instance**

```bash
# SSH into your instance
ssh -i ~/.ssh/oracle-key.key ubuntu@YOUR_IP

# Install Docker
sudo apt update
sudo apt install -y docker.io docker-compose

# Add user to docker group
sudo usermod -aG docker ubuntu

# Log out and back in for group changes
exit
ssh -i ~/.ssh/oracle-key.key ubuntu@YOUR_IP

# Verify installation
docker --version
```

**2. Install MySQL (Traditional)**

```bash
# Install MySQL directly on host (lighter than container)
sudo apt install -y mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql

# Create database and user
sudo mysql <<EOF
CREATE DATABASE fin_tracker;
CREATE USER 'fintracker'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON fin_tracker.* TO 'fintracker'@'localhost';
FLUSH PRIVILEGES;
EOF
```

**3. Deploy Application Container**

```bash
# Clone repository
cd /opt
sudo mkdir -p fintracker
sudo chown ubuntu:ubuntu fintracker
cd fintracker
git clone https://github.com/cey-labs/fin-tracker-backend.git
cd fin-tracker-backend

# Build Docker image
docker build -t fintracker-backend:latest .

# Run container
docker run -d \
  --name fintracker-app \
  --restart unless-stopped \
  -p 8080:8080 \
  --network host \
  -e DB_HOST=localhost \
  -e DB_PORT=3306 \
  -e DB_NAME=fin_tracker \
  -e DB_USERNAME=fintracker \
  -e DB_PASSWORD=your_password \
  -e APP_DEFAULT_CURRENCY=LKR \
  -e SPRING_PROFILES_ACTIVE=prod \
  fintracker-backend:latest

# Check logs
docker logs -f fintracker-app

# Check status
docker ps
```

**4. Configure Nginx** (same as JAR deployment)

```bash
sudo cp deployment/nginx-fintracker.conf /etc/nginx/sites-available/fintracker
sudo nano /etc/nginx/sites-available/fintracker  # Update YOUR_DOMAIN_OR_IP
sudo ln -s /etc/nginx/sites-available/fintracker /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

### Option B: Full Docker Compose Stack

**Best for:** 2GB+ RAM instances or ARM instance with 24GB

**1. Install Docker and Docker Compose**

```bash
# Follow same steps as Option A
```

**2. Deploy with Docker Compose**

```bash
cd /opt/fintracker/fin-tracker-backend

# Create .env file
cat > .env <<EOF
DB_PASSWORD=your_secure_password
MYSQL_ROOT_PASSWORD=root_password
APP_DEFAULT_CURRENCY=LKR
JAVA_OPTS=-Xmx512m -Xms256m -XX:+UseG1GC
EOF

# Secure the file
chmod 600 .env

# Start services
docker-compose up -d

# View logs
docker-compose logs -f

# Check health
curl http://localhost:8080/actuator/health
```

**3. Configure Firewall**

```bash
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw --force enable
```

---

## Resource Optimization for Free Tier

### For 1GB RAM AMD Instance

**Not recommended** - Use JAR deployment instead. If you must use Docker:

```yaml
# docker-compose.yml adjustments
services:
  app:
    environment:
      JAVA_OPTS: "-Xmx400m -Xms256m -XX:+UseG1GC"
    deploy:
      resources:
        limits:
          memory: 512M
```

### For 2GB RAM Instance

```yaml
services:
  app:
    environment:
      JAVA_OPTS: "-Xmx768m -Xms512m -XX:+UseG1GC"
    deploy:
      resources:
        limits:
          memory: 900M
```

### For 4GB+ RAM (ARM Instance)

```yaml
services:
  app:
    environment:
      JAVA_OPTS: "-Xmx1536m -Xms1024m -XX:+UseG1GC"
    deploy:
      resources:
        limits:
          memory: 2G
```

---

## Management Commands

### Docker Compose

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# Restart services
docker-compose restart

# View logs
docker-compose logs -f
docker-compose logs -f app     # Just app logs
docker-compose logs -f mysql   # Just MySQL logs

# Check status
docker-compose ps

# Rebuild after code changes
docker-compose up -d --build

# Scale (if needed)
docker-compose up -d --scale app=3
```

### Docker (Manual)

```bash
# List containers
docker ps

# View logs
docker logs -f fintracker-app

# Restart container
docker restart fintracker-app

# Stop container
docker stop fintracker-app

# Start container
docker start fintracker-app

# Remove container
docker rm fintracker-app

# Execute command in container
docker exec -it fintracker-app sh

# View resource usage
docker stats fintracker-app
```

### System Maintenance

```bash
# View disk usage
docker system df

# Clean up unused images/containers
docker system prune -a

# Remove unused volumes
docker volume prune

# Remove stopped containers
docker container prune
```

---

## Updating the Application

### Method 1: Rebuild and Replace

```bash
# Pull latest code
cd /opt/fintracker/fin-tracker-backend
git pull origin main

# Rebuild and restart
docker-compose up -d --build

# Or manually
docker build -t fintracker-backend:latest .
docker stop fintracker-app
docker rm fintracker-app
docker run -d --name fintracker-app ... fintracker-backend:latest
```

### Method 2: Zero-Downtime Update

```bash
# Build new image with version tag
docker build -t fintracker-backend:v2 .

# Start new container on different port
docker run -d \
  --name fintracker-app-v2 \
  -p 8081:8080 \
  ... \
  fintracker-backend:v2

# Update Nginx to point to new port
# Test new version
curl http://localhost:8081/api/v1/users

# If good, switch Nginx config and reload
sudo systemctl reload nginx

# Stop old container
docker stop fintracker-app
docker rm fintracker-app
```

---

## Backup and Restore

### Database Backup (MySQL Container)

```bash
# Backup database
docker exec fintracker-mysql \
  mysqldump -u fintracker -p'your_password' fin_tracker \
  > backup_$(date +%Y%m%d).sql

# Backup with compression
docker exec fintracker-mysql \
  mysqldump -u fintracker -p'your_password' fin_tracker \
  | gzip > backup_$(date +%Y%m%d).sql.gz
```

### Database Restore

```bash
# Restore from backup
docker exec -i fintracker-mysql \
  mysql -u fintracker -p'your_password' fin_tracker \
  < backup_20260204.sql

# Restore from compressed backup
gunzip -c backup_20260204.sql.gz | \
  docker exec -i fintracker-mysql \
  mysql -u fintracker -p'your_password' fin_tracker
```

### Volume Backup

```bash
# Backup volume data
docker run --rm \
  -v fintracker_mysql_data:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/mysql_backup.tar.gz /data
```

---

## Monitoring

### View Container Metrics

```bash
# Real-time resource usage
docker stats

# Specific container
docker stats fintracker-app

# Get container IP
docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' fintracker-app
```

### Application Health

```bash
# Health check
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics

# Inside container
docker exec fintracker-app curl http://localhost:8080/actuator/health
```

### Logs

```bash
# View logs
docker logs fintracker-app

# Follow logs
docker logs -f fintracker-app

# Last 100 lines
docker logs --tail 100 fintracker-app

# Since timestamp
docker logs --since 2024-01-01T00:00:00 fintracker-app
```

---

## Troubleshooting

### Container Won't Start

```bash
# Check logs
docker logs fintracker-app

# Check events
docker events

# Inspect container
docker inspect fintracker-app

# Check if port is in use
sudo lsof -i :8080
```

### Database Connection Issues

```bash
# Check MySQL is running
docker ps | grep mysql

# Check MySQL logs
docker logs fintracker-mysql

# Test connection from app container
docker exec fintracker-app ping mysql

# Test MySQL connection
docker exec -it fintracker-mysql mysql -u fintracker -p
```

### Out of Memory

```bash
# Check memory usage
docker stats

# Reduce JVM heap in .env
JAVA_OPTS=-Xmx400m -Xms256m

# Restart with new limits
docker-compose restart app
```

### Image Build Fails

```bash
# Clear build cache
docker builder prune -a

# Build with no cache
docker build --no-cache -t fintracker-backend:latest .

# Check disk space
df -h
```

---

## Performance Comparison

### JAR Deployment (Systemd)
- **Startup Time:** ~5-10 seconds
- **Memory Usage:** 300-500MB
- **CPU Overhead:** None
- **Disk Usage:** 50MB
- **Best for:** 1GB RAM, single app

### Docker Deployment
- **Startup Time:** ~10-15 seconds
- **Memory Usage:** 400-700MB
- **CPU Overhead:** 2-5%
- **Disk Usage:** 200-300MB
- **Best for:** 2GB+ RAM, portability

### Docker Compose (Full Stack)
- **Startup Time:** ~20-30 seconds
- **Memory Usage:** 600-900MB
- **CPU Overhead:** 5-10%
- **Disk Usage:** 500MB+
- **Best for:** 4GB+ RAM, development

---

## Security Best Practices

### 1. Use Non-Root User
Already configured in Dockerfile:
```dockerfile
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
```

### 2. Secure Environment Variables

```bash
# Use Docker secrets (Docker Swarm)
echo "your_password" | docker secret create db_password -

# Or use external secret management
# - HashiCorp Vault
# - AWS Secrets Manager
# - Azure Key Vault
```

### 3. Scan Images for Vulnerabilities

```bash
# Install Trivy
sudo apt install wget apt-transport-https gnupg lsb-release
wget -qO - https://aquasecurity.github.io/trivy-repo/deb/public.key | sudo apt-key add -
echo "deb https://aquasecurity.github.io/trivy-repo/deb $(lsb_release -sc) main" | sudo tee -a /etc/apt/sources.list.d/trivy.list
sudo apt update
sudo apt install trivy

# Scan image
trivy image fintracker-backend:latest
```

### 4. Limit Container Resources

```yaml
# In docker-compose.yml
deploy:
  resources:
    limits:
      cpus: '1.0'
      memory: 768M
```

### 5. Use Read-Only Filesystem

```yaml
# In docker-compose.yml
app:
  read_only: true
  tmpfs:
    - /tmp
```

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Build and Deploy Docker

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Build Docker image
        run: docker build -t fintracker-backend:${{ github.sha }} .

      - name: Push to registry
        run: |
          echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
          docker push fintracker-backend:${{ github.sha }}
```

---

## Conclusion

**Use Docker if:**
- ✅ You need portability across environments
- ✅ You have 2GB+ RAM available
- ✅ You're comfortable with containers
- ✅ You want reproducible builds

**Use JAR deployment if:**
- ✅ You have limited resources (1GB RAM)
- ✅ You want simplicity
- ✅ You're deploying a single application
- ✅ You want maximum performance

For Oracle Cloud Free Tier with 1GB RAM → **Use JAR deployment**
For Oracle Cloud Free Tier with ARM 24GB RAM → **Docker is fine**

---

## Further Reading

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker/)
- [Best Practices for Writing Dockerfiles](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/)
