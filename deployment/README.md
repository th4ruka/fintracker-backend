# Deployment Configuration Files

This directory contains all necessary configuration files for deploying the Fin-Tracker Backend to Oracle Cloud Free Tier or any Ubuntu server.

## Files in this Directory

### 1. `ORACLE_CLOUD_DEPLOYMENT.md` (Root Directory)
Complete step-by-step guide for deploying to Oracle Cloud Free Tier.

**Location:** `/ORACLE_CLOUD_DEPLOYMENT.md` (in project root)

### 2. `setup-server.sh`
Automated server setup script that installs and configures all dependencies.

**What it does:**
- Installs Java 21
- Installs MySQL 8.0
- Installs Nginx
- Configures firewall (UFW)
- Creates application directory
- Sets up MySQL database and user
- Creates environment configuration file
- Sets up swap file (if RAM <= 2GB)

**Usage:**
```bash
# On your Oracle Cloud instance
chmod +x setup-server.sh
./setup-server.sh
```

### 3. `fintracker.service`
Systemd service file for running the Spring Boot application as a system service.

**Installation:**
```bash
sudo cp fintracker.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable fintracker
sudo systemctl start fintracker
```

**Management:**
```bash
# Start service
sudo systemctl start fintracker

# Stop service
sudo systemctl stop fintracker

# Restart service
sudo systemctl restart fintracker

# Check status
sudo systemctl status fintracker

# View logs
sudo journalctl -u fintracker -f
```

### 4. `nginx-fintracker.conf`
Nginx reverse proxy configuration with security headers, rate limiting, and SSL support.

**Installation:**
```bash
sudo cp nginx-fintracker.conf /etc/nginx/sites-available/fintracker

# Edit and replace YOUR_DOMAIN_OR_IP with your actual domain or IP
sudo nano /etc/nginx/sites-available/fintracker

# Enable the site
sudo ln -s /etc/nginx/sites-available/fintracker /etc/nginx/sites-enabled/

# Remove default site
sudo rm /etc/nginx/sites-enabled/default

# Test configuration
sudo nginx -t

# Restart Nginx
sudo systemctl restart nginx
```

### 5. `.env.example`
Template for environment variables.

**Setup:**
```bash
# Copy to /opt/fintracker/.env
sudo cp .env.example /opt/fintracker/.env

# Edit with your actual values
sudo nano /opt/fintracker/.env

# Secure the file
sudo chmod 600 /opt/fintracker/.env
```

### 6. `application-prod.yml` (Main Resources)
Production Spring Boot configuration with optimized settings.

**Location:** `src/main/resources/application-prod.yml`

## Quick Start Deployment

### Prerequisites
- Oracle Cloud account (or any Ubuntu 20.04+ server)
- SSH access to the server
- Git repository access

### Step 1: Prepare Server (5 minutes)
```bash
# SSH into your server
ssh -i ~/.ssh/oracle-fin-tracker.key ubuntu@YOUR_IP

# Clone the repository
git clone https://github.com/cey-labs/fin-tracker-backend.git
cd fin-tracker-backend/deployment

# Run automated setup
chmod +x setup-server.sh
./setup-server.sh
```

### Step 2: Build Application (3 minutes)
```bash
# Build the JAR file
cd /opt/fintracker/fin-tracker-backend
./mvnw clean package -DskipTests
```

### Step 3: Configure Service (2 minutes)
```bash
# Copy systemd service file
sudo cp deployment/fintracker.service /etc/systemd/system/

# Reload systemd and start service
sudo systemctl daemon-reload
sudo systemctl enable fintracker
sudo systemctl start fintracker

# Check status
sudo systemctl status fintracker
```

### Step 4: Configure Nginx (3 minutes)
```bash
# Copy Nginx config
sudo cp deployment/nginx-fintracker.conf /etc/nginx/sites-available/fintracker

# Edit and replace YOUR_DOMAIN_OR_IP
sudo nano /etc/nginx/sites-available/fintracker

# Enable site
sudo ln -s /etc/nginx/sites-available/fintracker /etc/nginx/sites-enabled/
sudo rm /etc/nginx/sites-enabled/default

# Test and restart
sudo nginx -t
sudo systemctl restart nginx
```

### Step 5: Test (1 minute)
```bash
# Test from server
curl http://localhost:8080/api/v1/users

# Test from your computer
curl http://YOUR_PUBLIC_IP/api/v1/users
```

**Total Time:** ~15 minutes

## Environment Variables

The application reads configuration from environment variables:

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `DB_HOST` | MySQL host | localhost | Yes |
| `DB_PORT` | MySQL port | 3306 | Yes |
| `DB_NAME` | Database name | fin_tracker | Yes |
| `DB_USERNAME` | Database user | fintracker | Yes |
| `DB_PASSWORD` | Database password | - | **Yes** |
| `APP_DEFAULT_CURRENCY` | Default currency | LKR | No |
| `SPRING_PROFILES_ACTIVE` | Spring profile | prod | No |
| `JAVA_OPTS` | JVM options | -Xmx768m... | No |

## Architecture

```
┌─────────────┐
│   Internet  │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────┐
│  Oracle Cloud VM (Ubuntu)           │
│                                     │
│  ┌──────────────────────────────┐  │
│  │  Nginx (Port 80/443)         │  │
│  │  - Reverse Proxy             │  │
│  │  - SSL/TLS                   │  │
│  │  - Rate Limiting             │  │
│  └───────────┬──────────────────┘  │
│              │                      │
│              ▼                      │
│  ┌──────────────────────────────┐  │
│  │  Spring Boot (Port 8080)     │  │
│  │  - REST API                  │  │
│  │  - Business Logic            │  │
│  │  - Flyway Migrations         │  │
│  └───────────┬──────────────────┘  │
│              │                      │
│              ▼                      │
│  ┌──────────────────────────────┐  │
│  │  MySQL 8.0 (Port 3306)       │  │
│  │  - Database Storage          │  │
│  └──────────────────────────────┘  │
│                                     │
└─────────────────────────────────────┘
```

## Security Considerations

1. **Firewall (UFW)**
   - Only ports 22, 80, 443, 8080 are open
   - SSH access restricted to key-based auth

2. **Nginx**
   - Rate limiting enabled (10 req/s)
   - Security headers configured
   - SSL/TLS support ready

3. **Database**
   - MySQL user has access only to fin_tracker database
   - Localhost-only connections
   - Strong password required

4. **Environment Variables**
   - Sensitive data not in code
   - File permissions: 600 (owner read/write only)

5. **Application**
   - Runs as non-root user (ubuntu)
   - Resource limits configured
   - No external management endpoints exposed

## Monitoring & Maintenance

### View Logs
```bash
# Application logs
sudo journalctl -u fintracker -f

# Nginx access logs
sudo tail -f /var/log/nginx/fintracker-access.log

# Nginx error logs
sudo tail -f /var/log/nginx/fintracker-error.log

# MySQL logs
sudo journalctl -u mysql -f
```

### Health Check
```bash
# Check application health
curl http://localhost:8080/actuator/health

# Via Nginx
curl http://YOUR_IP/actuator/health
```

### Resource Monitoring
```bash
# CPU and Memory
htop

# Disk usage
df -h

# Check service status
sudo systemctl status fintracker mysql nginx
```

### Database Backup
```bash
# Manual backup
mysqldump -u fintracker -p fin_tracker > backup_$(date +%Y%m%d).sql

# Automated daily backup (cron)
0 2 * * * mysqldump -u fintracker -p'password' fin_tracker | gzip > ~/backups/fin_tracker_$(date +\%Y\%m\%d).sql.gz
```

### Update Application
```bash
cd /opt/fintracker/fin-tracker-backend

# Pull latest changes
git pull origin main

# Rebuild
./mvnw clean package -DskipTests

# Restart service
sudo systemctl restart fintracker

# Verify
sudo systemctl status fintracker
sudo journalctl -u fintracker -f
```

## Troubleshooting

### Application Won't Start
```bash
# Check logs
sudo journalctl -u fintracker -n 100 --no-pager

# Common causes:
# 1. Database connection failed
sudo systemctl status mysql

# 2. Port already in use
sudo lsof -i :8080

# 3. Out of memory
free -h
```

### Cannot Connect from Internet
```bash
# Check Oracle Cloud security list (via OCI Console)
# Check UFW firewall
sudo ufw status

# Check Nginx
sudo systemctl status nginx
sudo nginx -t

# Check application
curl http://localhost:8080/api/v1/users
```

### Database Issues
```bash
# Check MySQL status
sudo systemctl status mysql

# Test connection
mysql -u fintracker -p fin_tracker

# Check Flyway migrations
# (Migrations run automatically on application start)
```

## Performance Tuning

### For 1GB RAM Instance
- JVM: `-Xmx768m -Xms512m`
- Connection pool: 10 max connections
- Enable swap: 2GB recommended

### For 2GB+ RAM Instance
- JVM: `-Xmx1536m -Xms1024m`
- Connection pool: 20 max connections
- Swap optional

### Database Optimization
```sql
-- Add indexes for frequently queried columns
-- (Already included in migration V3)

-- Check slow queries
SHOW FULL PROCESSLIST;
```

## SSL/HTTPS Setup

After deploying, secure your application with Let's Encrypt:

```bash
# Install Certbot
sudo apt install -y certbot python3-certbot-nginx

# Get certificate (replace with your domain)
sudo certbot --nginx -d api.yourdomain.com

# Auto-renewal is configured automatically
# Test renewal
sudo certbot renew --dry-run
```

## Support

For issues or questions:
1. Check the main deployment guide: `../ORACLE_CLOUD_DEPLOYMENT.md`
2. Review application logs: `sudo journalctl -u fintracker -f`
3. Check project documentation

## License

Same as main project license.
