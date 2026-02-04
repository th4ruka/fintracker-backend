# JAR Deployment Plan - Oracle Cloud Free Tier

## Executive Summary

**Goal:** Deploy Fin-Tracker Backend to Oracle Cloud's Forever Free Tier using direct JAR deployment

**Total Time:** 60-90 minutes (or 15 minutes with automation)
**Cost:** $0/month (Forever Free)
**Difficulty:** ⭐⭐ Beginner-friendly

---

## Quick Reference

### Option A: Automated Setup (15 minutes) ⚡ RECOMMENDED

```bash
1. Create Oracle Cloud instance (10 min)
2. SSH into instance
3. Run: ./deployment/setup-server.sh
4. Run: ./mvnw clean package -DskipTests
5. Run: sudo systemctl start fintracker
6. Done! ✅
```

### Option B: Manual Setup (60-90 minutes) 📚

Follow the detailed plan below for full understanding and control.

---

## Deployment Architecture

```
┌─────────────────────────────────────────────────────┐
│  Oracle Cloud VM (Ubuntu 22.04 - 1GB RAM)          │
│                                                     │
│  ┌───────────────────────────────────────────────┐ │
│  │  Internet (Port 80/443)                       │ │
│  └────────────────┬──────────────────────────────┘ │
│                   │                                 │
│                   ▼                                 │
│  ┌───────────────────────────────────────────────┐ │
│  │  Nginx Reverse Proxy                          │ │
│  │  - SSL/TLS termination                        │ │
│  │  - Rate limiting (10 req/s)                   │ │
│  │  - Security headers                           │ │
│  │  - Gzip compression                           │ │
│  └────────────────┬──────────────────────────────┘ │
│                   │                                 │
│                   ▼                                 │
│  ┌───────────────────────────────────────────────┐ │
│  │  Spring Boot App (Port 8080)                  │ │
│  │  - Runs as systemd service                    │ │
│  │  - Auto-restart on failure                    │ │
│  │  - JAR: fintracker-backend.jar                │ │
│  │  - JVM: 768MB max heap                        │ │
│  │  - User: ubuntu (non-root)                    │ │
│  └────────────────┬──────────────────────────────┘ │
│                   │                                 │
│                   ▼                                 │
│  ┌───────────────────────────────────────────────┐ │
│  │  MySQL 8.0 (Port 3306)                        │ │
│  │  - Database: fin_tracker                      │ │
│  │  - User: fintracker (localhost only)          │ │
│  │  - Flyway migrations auto-run                 │ │
│  └───────────────────────────────────────────────┘ │
│                                                     │
│  Firewall (UFW):                                   │
│  - Port 22 (SSH)     ✅ Open                       │
│  - Port 80 (HTTP)    ✅ Open                       │
│  - Port 443 (HTTPS)  ✅ Open                       │
│  - Port 3306 (MySQL) ❌ Blocked (internal only)    │
└─────────────────────────────────────────────────────┘
```

---

## Phase 1: Oracle Cloud Setup (20-30 minutes)

### Step 1.1: Create Oracle Cloud Account (10 minutes)

**What you'll do:**
- Sign up at https://www.oracle.com/cloud/free/
- Verify email and phone
- Add credit card (for verification only, won't be charged)

**What you'll get:**
- Cloud account with Forever Free tier
- Access to Oracle Cloud Console

**Detailed Guide:** See `ORACLE_CLOUD_DEPLOYMENT.md` Part 1

---

### Step 1.2: Create Compute Instance (10 minutes)

**Configure:**
- **Name:** `fin-tracker-backend-prod`
- **Image:** Ubuntu 22.04 LTS
- **Shape:** VM.Standard.E2.1.Micro (1 core, 1GB RAM) - Always Free
- **Networking:** Auto-create VCN with public IP
- **SSH Keys:** Download and save private key securely

**What you'll get:**
- Running VM with public IP address
- SSH access with private key

**Command to connect:**
```bash
ssh -i ~/.ssh/oracle-fin-tracker.key ubuntu@YOUR_PUBLIC_IP
```

**Detailed Guide:** See `ORACLE_CLOUD_DEPLOYMENT.md` Part 2

---

### Step 1.3: Configure Security Rules (5 minutes)

**Open these ports in Oracle Cloud Console:**

| Port | Protocol | Purpose | Source |
|------|----------|---------|--------|
| 22 | TCP | SSH | 0.0.0.0/0 |
| 80 | TCP | HTTP | 0.0.0.0/0 |
| 443 | TCP | HTTPS | 0.0.0.0/0 |
| 8080 | TCP | Spring Boot (optional) | 0.0.0.0/0 |

**Also configure Ubuntu firewall:**
```bash
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw --force enable
```

**What you'll get:**
- Access to your application from internet
- Secure SSH access

**Detailed Guide:** See `ORACLE_CLOUD_DEPLOYMENT.md` Part 3

---

## Phase 2: Server Setup (15-20 minutes)

**Prerequisites:** SSH access to your Oracle Cloud instance

### Step 2.1: Update System (2 minutes)

```bash
sudo apt update
sudo apt upgrade -y
```

**What this does:**
- Updates package lists
- Upgrades all system packages
- Ensures security patches

---

### Step 2.2: Install Java 21 (3 minutes)

```bash
# Add Adoptium repository
wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo apt-key add -
echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list

# Install Java 21
sudo apt update
sudo apt install -y temurin-21-jdk

# Verify
java -version
```

**Expected output:**
```
openjdk version "21.0.x"
OpenJDK Runtime Environment Temurin-21+35
```

**What this does:**
- Installs Eclipse Temurin JDK 21
- Required to run your Spring Boot application

---

### Step 2.3: Install MySQL 8.0 (3 minutes)

```bash
# Install MySQL
sudo apt install -y mysql-server

# Start and enable
sudo systemctl start mysql
sudo systemctl enable mysql

# Verify
sudo systemctl status mysql
```

**What this does:**
- Installs MySQL 8.0 database server
- Configures auto-start on boot

---

### Step 2.4: Install Additional Tools (2 minutes)

```bash
sudo apt install -y git curl wget maven nginx
```

**What this does:**
- **git:** Clone repository
- **maven:** Build application (optional)
- **nginx:** Reverse proxy
- **curl/wget:** Testing and downloads

---

### Step 2.5: Create Swap File (5 minutes) - IMPORTANT for 1GB RAM

```bash
# Create 2GB swap file
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile

# Make permanent
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Verify
free -h
```

**Why this matters:**
- 1GB RAM is tight for Java applications
- Swap prevents Out-Of-Memory crashes
- Acts as safety buffer

**What you'll see:**
```
              total        used        free      shared  buff/cache   available
Mem:           978M        150M        650M        1.0M        178M        750M
Swap:          2.0G          0B        2.0G
```

**Automated Alternative:**
```bash
# All steps above in one command
./deployment/setup-server.sh
```

**Detailed Guide:** See `ORACLE_CLOUD_DEPLOYMENT.md` Part 5

---

## Phase 3: Database Setup (5-10 minutes)

### Step 3.1: Secure MySQL (2 minutes)

```bash
sudo mysql_secure_installation
```

**Answer prompts:**
- Validate password? **N** (or Y for strict passwords)
- Remove anonymous users? **Y**
- Disallow root login remotely? **Y**
- Remove test database? **Y**
- Reload privileges? **Y**

---

### Step 3.2: Create Database and User (3 minutes)

```bash
# Login to MySQL
sudo mysql
```

**Run these SQL commands:**
```sql
-- Create database
CREATE DATABASE fin_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER 'fintracker'@'localhost' IDENTIFIED BY 'YOUR_STRONG_PASSWORD_HERE';

-- Grant privileges
GRANT ALL PRIVILEGES ON fin_tracker.* TO 'fintracker'@'localhost';

-- Apply changes
FLUSH PRIVILEGES;

-- Verify
SHOW DATABASES;

-- Exit
EXIT;
```

**What you created:**
- Database: `fin_tracker`
- User: `fintracker` (localhost only)
- Permissions: Full access to fin_tracker database

---

### Step 3.3: Test Connection (1 minute)

```bash
mysql -u fintracker -p fin_tracker
```

Enter your password. If successful:
```
Welcome to the MySQL monitor.
mysql>
```

Type `EXIT;` to exit.

**Detailed Guide:** See `ORACLE_CLOUD_DEPLOYMENT.md` Part 6

---

## Phase 4: Application Deployment (10-15 minutes)

### Step 4.1: Create Application Directory (1 minute)

```bash
# Create directory
sudo mkdir -p /opt/fintracker
sudo chown ubuntu:ubuntu /opt/fintracker
cd /opt/fintracker
```

**Directory structure:**
```
/opt/fintracker/
├── fin-tracker-backend/        (your application)
│   ├── src/
│   ├── target/
│   │   └── fintracker-backend-0.0.1-SNAPSHOT.jar
│   ├── pom.xml
│   └── deployment/
└── .env                        (environment variables)
```

---

### Step 4.2: Clone Repository (2 minutes)

```bash
cd /opt/fintracker
git clone https://github.com/cey-labs/fin-tracker-backend.git
cd fin-tracker-backend

# Checkout the deployment branch
git checkout claude/aws-deployment-options-Ze6ps
```

**What you'll get:**
- Full source code
- Deployment scripts
- Configuration files

---

### Step 4.3: Build Application (5 minutes)

```bash
cd /opt/fintracker/fin-tracker-backend

# Build JAR file (skip tests for faster build)
./mvnw clean package -DskipTests
```

**What happens:**
- Maven downloads dependencies (~2 minutes)
- Compiles Java code (~1 minute)
- Packages into JAR file (~1 minute)

**Expected output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 2:34 min
```

**JAR location:**
```
/opt/fintracker/fin-tracker-backend/target/fintracker-backend-0.0.1-SNAPSHOT.jar
```

**JAR size:** ~50MB

---

### Step 4.4: Create Environment Configuration (2 minutes)

```bash
# Create .env file
nano /opt/fintracker/.env
```

**Add this content:**
```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=fin_tracker
DB_USERNAME=fintracker
DB_PASSWORD=YOUR_STRONG_PASSWORD_HERE

# Application Configuration
APP_DEFAULT_CURRENCY=LKR
SPRING_PROFILES_ACTIVE=prod

# JVM Options
JAVA_OPTS=-Xmx768m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

**Replace:** `YOUR_STRONG_PASSWORD_HERE` with your MySQL password

**Save:** Ctrl+X, Y, Enter

**Secure the file:**
```bash
chmod 600 /opt/fintracker/.env
```

---

### Step 4.5: Test Run (2 minutes)

```bash
# Source environment variables
set -a
source /opt/fintracker/.env
set +a

# Run application
java -jar target/fintracker-backend-0.0.1-SNAPSHOT.jar
```

**What to look for:**
```
Started FinTrackerBackendApplication in 8.234 seconds
Flyway migration completed successfully
```

**Test in another terminal:**
```bash
curl http://localhost:8080/api/v1/users
```

**Expected:** `[]` (empty array)

**Stop the test:** Ctrl+C

**Detailed Guide:** See `ORACLE_CLOUD_DEPLOYMENT.md` Part 7

---

## Phase 5: Configure as System Service (5 minutes)

### Step 5.1: Install Systemd Service (2 minutes)

```bash
# Copy service file
sudo cp /opt/fintracker/fin-tracker-backend/deployment/fintracker.service /etc/systemd/system/

# Reload systemd
sudo systemctl daemon-reload
```

**What this does:**
- Installs service definition
- Enables systemd management
- Configures auto-restart

---

### Step 5.2: Start Service (1 minute)

```bash
# Start the service
sudo systemctl start fintracker

# Enable auto-start on boot
sudo systemctl enable fintracker

# Check status
sudo systemctl status fintracker
```

**Expected output:**
```
● fintracker.service - Fin Tracker Backend Spring Boot Application
   Loaded: loaded (/etc/systemd/system/fintracker.service; enabled)
   Active: active (running) since Mon 2024-02-04 10:30:45 UTC; 5s ago
```

---

### Step 5.3: Monitor Logs (2 minutes)

```bash
# View real-time logs
sudo journalctl -u fintracker -f

# View last 50 lines
sudo journalctl -u fintracker -n 50
```

**What to verify:**
- ✅ Application starts successfully
- ✅ Flyway migrations run
- ✅ No errors in logs
- ✅ Port 8080 listening

**Service Management Commands:**
```bash
sudo systemctl start fintracker     # Start service
sudo systemctl stop fintracker      # Stop service
sudo systemctl restart fintracker   # Restart service
sudo systemctl status fintracker    # Check status
```

**Detailed Guide:** See `ORACLE_CLOUD_DEPLOYMENT.md` Part 8

---

## Phase 6: Configure Nginx Reverse Proxy (10 minutes)

### Step 6.1: Install Nginx (1 minute)

```bash
sudo apt install -y nginx
```

---

### Step 6.2: Configure Nginx (3 minutes)

```bash
# Copy configuration
sudo cp /opt/fintracker/fin-tracker-backend/deployment/nginx-fintracker.conf /etc/nginx/sites-available/fintracker

# Edit configuration
sudo nano /etc/nginx/sites-available/fintracker
```

**Find and replace:**
```nginx
server_name YOUR_DOMAIN_OR_IP;
```

**Replace with your public IP:**
```nginx
server_name 158.101.100.50;  # Your actual IP
```

**Or if you have a domain:**
```nginx
server_name api.yourdomain.com;
```

**Save:** Ctrl+X, Y, Enter

---

### Step 6.3: Enable Configuration (2 minutes)

```bash
# Create symbolic link
sudo ln -s /etc/nginx/sites-available/fintracker /etc/nginx/sites-enabled/

# Remove default site
sudo rm /etc/nginx/sites-enabled/default

# Test configuration
sudo nginx -t
```

**Expected output:**
```
nginx: configuration file /etc/nginx/nginx.conf test is successful
```

---

### Step 6.4: Start Nginx (1 minute)

```bash
# Restart Nginx
sudo systemctl restart nginx

# Enable auto-start
sudo systemctl enable nginx

# Check status
sudo systemctl status nginx
```

---

### Step 6.5: Test Full Stack (3 minutes)

**From your local machine:**
```bash
# Test via Nginx (port 80)
curl http://YOUR_PUBLIC_IP/api/v1/users

# Test health endpoint
curl http://YOUR_PUBLIC_IP/actuator/health
```

**Expected:**
```json
[]
```

**Also test in browser:**
- Open: `http://YOUR_PUBLIC_IP/api/v1/users`
- Should see: `[]`

🎉 **Your application is now live!**

**Detailed Guide:** See `ORACLE_CLOUD_DEPLOYMENT.md` Part 9

---

## Phase 7: Optional - Domain & SSL (15-20 minutes)

### Step 7.1: Point Domain to Server (5 minutes)

**At your domain registrar:**
1. Go to DNS management
2. Add A Record:
   - **Name:** `api` (or `@` for root)
   - **Type:** `A`
   - **Value:** Your Oracle Cloud public IP
   - **TTL:** 300

**Wait 5-30 minutes for DNS propagation**

**Test:**
```bash
ping api.yourdomain.com
```

---

### Step 7.2: Install SSL Certificate (10 minutes)

```bash
# Install Certbot
sudo apt install -y certbot python3-certbot-nginx

# Get certificate
sudo certbot --nginx -d api.yourdomain.com
```

**Certbot will ask:**
1. Enter email: `your@email.com`
2. Agree to terms: `Y`
3. Share email: `N`
4. Redirect HTTP to HTTPS? `2` (Yes, redirect)

**What Certbot does:**
- Obtains SSL certificate from Let's Encrypt
- Configures Nginx for HTTPS
- Sets up auto-renewal (every 60 days)

**Test SSL:**
```bash
curl https://api.yourdomain.com/api/v1/users
```

**Browser test:**
- Visit: `https://api.yourdomain.com/api/v1/users`
- Check for 🔒 padlock icon

**Detailed Guide:** See `ORACLE_CLOUD_DEPLOYMENT.md` Part 10

---

## Resource Usage After Deployment

### Memory Breakdown (1GB Instance)

```
Component         RAM Usage    Percentage
─────────────────────────────────────────
OS (Ubuntu)       ~200 MB      20%
MySQL             ~150 MB      15%
Spring Boot       ~400 MB      40%
Nginx             ~10 MB       1%
System Services   ~50 MB       5%
─────────────────────────────────────────
Total Used        ~810 MB      81%
Free RAM          ~190 MB      19%
Swap (2GB)        ~0 MB        Available for peaks
```

✅ **Comfortable fit with headroom**

---

## Maintenance Commands

### Application Management

```bash
# Restart application
sudo systemctl restart fintracker

# View logs
sudo journalctl -u fintracker -f

# Check status
sudo systemctl status fintracker

# Stop application
sudo systemctl stop fintracker
```

---

### Update Application

```bash
# Navigate to app directory
cd /opt/fintracker/fin-tracker-backend

# Pull latest code
git pull origin main

# Rebuild
./mvnw clean package -DskipTests

# Restart service
sudo systemctl restart fintracker

# Verify
sudo journalctl -u fintracker -f
```

---

### Database Backup

```bash
# Create backup directory
mkdir -p ~/backups

# Backup database
mysqldump -u fintracker -p fin_tracker > ~/backups/fin_tracker_$(date +%Y%m%d_%H%M%S).sql

# Compress
gzip ~/backups/fin_tracker_*.sql

# List backups
ls -lh ~/backups/
```

---

### Monitor Resources

```bash
# Check memory
free -h

# Check disk
df -h

# Check CPU and processes
htop

# Check service status
systemctl status fintracker mysql nginx
```

---

## Troubleshooting Quick Reference

### Application Won't Start

```bash
# Check logs
sudo journalctl -u fintracker -n 100 --no-pager

# Check if port is in use
sudo lsof -i :8080

# Verify MySQL is running
sudo systemctl status mysql

# Check environment file
cat /opt/fintracker/.env
```

---

### Cannot Connect from Internet

```bash
# 1. Check Oracle Cloud security rules (via console)
# 2. Check Ubuntu firewall
sudo ufw status

# 3. Check Nginx
sudo systemctl status nginx
sudo nginx -t

# 4. Check application
curl http://localhost:8080/api/v1/users

# 5. Check from outside
curl http://YOUR_PUBLIC_IP/api/v1/users
```

---

### Database Connection Failed

```bash
# Check MySQL is running
sudo systemctl status mysql

# Test connection
mysql -u fintracker -p fin_tracker

# Check credentials in .env
cat /opt/fintracker/.env

# View MySQL logs
sudo journalctl -u mysql -n 50
```

---

### Out of Memory

```bash
# Check memory usage
free -h

# Check if swap is active
swapon --show

# Reduce JVM heap (in .env)
JAVA_OPTS=-Xmx512m -Xms256m

# Restart
sudo systemctl restart fintracker
```

---

## Success Checklist

After deployment, verify:

- [ ] Oracle Cloud instance running
- [ ] SSH access working
- [ ] Java 21 installed
- [ ] MySQL 8.0 installed and secured
- [ ] Database `fin_tracker` created
- [ ] User `fintracker` created with permissions
- [ ] Application JAR built successfully
- [ ] Environment variables configured
- [ ] Systemd service installed and running
- [ ] Application accessible on port 8080 (localhost)
- [ ] Nginx installed and configured
- [ ] Application accessible on port 80 (public)
- [ ] Health check endpoint responding
- [ ] Flyway migrations completed
- [ ] No errors in logs
- [ ] Firewall rules configured
- [ ] (Optional) Domain pointing to server
- [ ] (Optional) SSL certificate installed

---

## Cost Breakdown

```
Oracle Cloud Free Tier:
├── Compute (1GB RAM VM)     $0/month ✅ Forever Free
├── Storage (50GB)           $0/month ✅ Forever Free
├── Network (10TB/month)     $0/month ✅ Forever Free
├── Public IP                $0/month ✅ Forever Free
└── Total                    $0/month 🎉
```

**No hidden costs. No time limit. Forever free.**

---

## Performance Metrics

Expected performance on 1GB instance:

```
Metric              Value
─────────────────────────────────
Startup Time        5-10 seconds
Memory Usage        400-500 MB
Response Time       30-50ms (p95)
Throughput          ~500 req/sec
Cold Start          8-10 seconds
Restart Time        5-8 seconds
```

---

## Next Steps After Deployment

1. **Test all API endpoints thoroughly**
2. **Set up automated backups** (daily cron job)
3. **Monitor logs** for first 24 hours
4. **Configure monitoring** (UptimeRobot, Pingdom)
5. **Document your API** (Swagger/OpenAPI)
6. **Set up CI/CD** (GitHub Actions)
7. **Create staging environment** (if needed)

---

## Support Resources

- **Full Deployment Guide:** `ORACLE_CLOUD_DEPLOYMENT.md`
- **Deployment Comparison:** `DEPLOYMENT_COMPARISON.md`
- **Docker Alternative:** `DOCKER_DEPLOYMENT.md`
- **Configuration Files:** `deployment/` directory
- **Automated Setup:** `deployment/setup-server.sh`

---

## Summary

**You will deploy:**
- ✅ Spring Boot application as JAR
- ✅ MySQL 8.0 database
- ✅ Nginx reverse proxy
- ✅ Systemd service management
- ✅ Firewall configuration
- ✅ SSL/HTTPS (optional)

**Total cost:** $0/month on Oracle Free Tier

**Time investment:**
- Automated: ~15 minutes
- Manual: ~60-90 minutes

**Result:** Production-ready REST API running 24/7

**Ready to deploy?** Start with Step 1! 🚀
