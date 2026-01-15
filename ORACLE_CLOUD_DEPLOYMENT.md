# Oracle Cloud Free Tier Deployment Guide

## Complete Guide to Deploy Fin-Tracker-Backend on Oracle Cloud Always Free Tier

This guide will walk you through deploying your Spring Boot application on Oracle Cloud's Forever Free tier, which includes:
- 2 AMD VMs (1GB RAM each) OR 4 ARM Ampere cores + 24GB RAM
- 2 Oracle Autonomous Databases OR MySQL on VM
- 200GB block storage
- 10TB outbound data transfer/month

**Estimated Time:** 60-90 minutes
**Cost:** $0 (Forever Free)

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Part 1: Oracle Cloud Account Setup](#part-1-oracle-cloud-account-setup)
3. [Part 2: Create Compute Instance (VM)](#part-2-create-compute-instance-vm)
4. [Part 3: Configure Security Rules](#part-3-configure-security-rules)
5. [Part 4: Connect to Your Instance](#part-4-connect-to-your-instance)
6. [Part 5: Install Dependencies](#part-5-install-dependencies)
7. [Part 6: Setup MySQL Database](#part-6-setup-mysql-database)
8. [Part 7: Deploy Spring Boot Application](#part-7-deploy-spring-boot-application)
9. [Part 8: Configure Application as Service](#part-8-configure-application-as-service)
10. [Part 9: Setup Nginx Reverse Proxy (Optional)](#part-9-setup-nginx-reverse-proxy-optional)
11. [Part 10: Domain and SSL Setup (Optional)](#part-10-domain-and-ssl-setup-optional)
12. [Maintenance and Updates](#maintenance-and-updates)
13. [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before starting, ensure you have:

- [ ] Valid email address
- [ ] Credit/debit card (for verification only, won't be charged)
- [ ] Phone number for verification
- [ ] SSH client installed (Terminal on Mac/Linux, PuTTY on Windows)
- [ ] Git installed locally
- [ ] Basic Linux command line knowledge

---

## Part 1: Oracle Cloud Account Setup

### Step 1.1: Create Oracle Cloud Account

1. Go to https://www.oracle.com/cloud/free/
2. Click **"Start for free"**
3. Fill in your information:
   - Country/Territory
   - Full name
   - Email address
   - Password
4. Click **"Verify my email"**
5. Check your email and click verification link
6. Complete the account setup:
   - Choose **"Individual"** or **"Company"**
   - Enter address details
   - Enter phone number for SMS verification
   - Enter credit card (for identity verification only)

   **Note:** Oracle won't charge you unless you explicitly upgrade to paid account

7. Accept terms and click **"Start my free trial"**
8. Wait for account provisioning (can take 5-30 minutes)

### Step 1.2: Access Oracle Cloud Console

1. Go to https://cloud.oracle.com/
2. Enter your **Cloud Account Name** (sent to your email)
3. Click **"Next"**
4. Sign in with your email and password
5. You'll see the Oracle Cloud Console dashboard

---

## Part 2: Create Compute Instance (VM)

### Step 2.1: Choose VM Type

Oracle Free Tier offers two options:
- **Option A:** 2x AMD VMs (1GB RAM each) - Choose this for simplicity
- **Option B:** 1x ARM VM (up to 4 cores, 24GB RAM total) - Better specs but ARM architecture

**Recommendation:** Start with AMD VM (easier compatibility with Java)

### Step 2.2: Create AMD Compute Instance

1. From Oracle Cloud Console, click **☰ Menu** (top left)
2. Navigate to **Compute** → **Instances**
3. Ensure you're in your **home region** (check top right)
4. Click **"Create Instance"**

**Configure the instance:**

**Name:** `fin-tracker-backend-prod`

**Placement:**
- Leave default (Availability Domain will be selected automatically)

**Image and Shape:**
- Click **"Edit"** next to "Image and shape"
- **Image:**
  - Click **"Change Image"**
  - Select **"Canonical Ubuntu"** (22.04 or latest LTS)
  - Click **"Select Image"**

- **Shape:**
  - Click **"Change Shape"**
  - Select **"Virtual Machine"**
  - Select **"AMD"** under Specialty and previous generation
  - Choose **VM.Standard.E2.1.Micro** (1 core, 1GB RAM) - **Always Free eligible**
  - Click **"Select Shape"**

**Networking:**
- Leave **"Create new virtual cloud network"** selected
- VCN Name: `fin-tracker-vcn`
- Subnet Name: `fin-tracker-subnet`
- Ensure **"Assign a public IPv4 address"** is checked

**Add SSH Keys:**
- Select **"Generate a key pair for me"**
- Click **"Save Private Key"** - Download and save this file securely!
- Click **"Save Public Key"** - Optional but recommended
- **IMPORTANT:** You cannot recover this private key later!

**Boot Volume:**
- Leave default (47-50GB) - All free tier eligible

5. Click **"Create"**
6. Wait for provisioning (2-5 minutes)
7. Instance will show **"RUNNING"** status with a green icon
8. **Note down the Public IP address** - You'll need this!

### Step 2.3: Create ARM Instance (Alternative - More Resources)

If you want more RAM and CPU (recommended for production):

1. Follow steps 1-3 above
2. **Name:** `fin-tracker-backend-arm`
3. **Shape:**
   - Click **"Change Shape"**
   - Select **"Ampere"**
   - Choose **VM.Standard.A1.Flex**
   - Set OCPUs: **4** (maximum free)
   - Set Memory: **24 GB** (maximum free)
   - Click **"Select Shape"**
4. Continue with same steps for Image, Networking, and SSH keys

**Note:** You can use up to 4 cores and 24GB RAM total across all ARM instances. You could create:
- 1 VM with 4 cores, 24GB RAM (recommended)
- 2 VMs with 2 cores, 12GB RAM each
- 4 VMs with 1 core, 6GB RAM each

---

## Part 3: Configure Security Rules

By default, Oracle Cloud blocks most incoming traffic. You need to open ports for your application.

### Step 3.1: Configure Security List

1. From your instance details page, under **"Instance details"**
2. Click on the **"Subnet"** link (e.g., `fin-tracker-subnet`)
3. Click on the **"Security List"** (e.g., `Default Security List for fin-tracker-vcn`)
4. Click **"Add Ingress Rules"**

**Add these rules one by one:**

**Rule 1: HTTP (Port 80)**
- Source CIDR: `0.0.0.0/0`
- IP Protocol: `TCP`
- Source Port Range: `All`
- Destination Port Range: `80`
- Description: `HTTP traffic`
- Click **"Add Ingress Rules"**

**Rule 2: HTTPS (Port 443)**
- Source CIDR: `0.0.0.0/0`
- IP Protocol: `TCP`
- Source Port Range: `All`
- Destination Port Range: `443`
- Description: `HTTPS traffic`
- Click **"Add Ingress Rules"**

**Rule 3: Spring Boot (Port 8080) - Optional, only if not using Nginx**
- Source CIDR: `0.0.0.0/0`
- IP Protocol: `TCP`
- Source Port Range: `All`
- Destination Port Range: `8080`
- Description: `Spring Boot app`
- Click **"Add Ingress Rules"**

### Step 3.2: Configure Ubuntu Firewall

Once connected to your instance (next section), run:

```bash
# Allow SSH (already allowed)
sudo ufw allow 22/tcp

# Allow HTTP
sudo ufw allow 80/tcp

# Allow HTTPS
sudo ufw allow 443/tcp

# Allow Spring Boot (if not using Nginx)
sudo ufw allow 8080/tcp

# Enable firewall
sudo ufw --force enable

# Check status
sudo ufw status
```

---

## Part 4: Connect to Your Instance

### Step 4.1: Prepare SSH Key (First Time Only)

**On Mac/Linux:**
```bash
# Move the private key to SSH directory
mkdir -p ~/.ssh
mv ~/Downloads/ssh-key-*.key ~/.ssh/oracle-fin-tracker.key

# Set correct permissions (REQUIRED)
chmod 400 ~/.ssh/oracle-fin-tracker.key
```

**On Windows:**
- Use PuTTY or Windows Terminal with OpenSSH
- If using PuTTY, convert the key using PuTTYgen first

### Step 4.2: Connect via SSH

Replace `YOUR_PUBLIC_IP` with the IP address from Part 2:

```bash
ssh -i ~/.ssh/oracle-fin-tracker.key ubuntu@YOUR_PUBLIC_IP
```

**Example:**
```bash
ssh -i ~/.ssh/oracle-fin-tracker.key ubuntu@158.101.100.50
```

If prompted about host authenticity, type `yes`

You should see:
```
Welcome to Ubuntu 22.04 LTS
...
ubuntu@fin-tracker-backend-prod:~$
```

🎉 **You're now connected to your Oracle Cloud instance!**

---

## Part 5: Install Dependencies

Run these commands on your Oracle Cloud instance:

### Step 5.1: Update System

```bash
sudo apt update
sudo apt upgrade -y
```

### Step 5.2: Install Java 21

```bash
# Add Adoptium (Eclipse Temurin) repository
wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo apt-key add -
echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list

# Update and install Java 21
sudo apt update
sudo apt install -y temurin-21-jdk

# Verify installation
java -version
```

You should see:
```
openjdk version "21.0.x"
```

### Step 5.3: Install MySQL 8.0

```bash
# Install MySQL Server
sudo apt install -y mysql-server

# Start MySQL service
sudo systemctl start mysql
sudo systemctl enable mysql

# Verify MySQL is running
sudo systemctl status mysql
```

### Step 5.4: Install Git and other tools

```bash
sudo apt install -y git curl wget unzip
```

### Step 5.5: Install Maven (Optional - for building on server)

```bash
sudo apt install -y maven

# Verify
mvn -version
```

---

## Part 6: Setup MySQL Database

### Step 6.1: Secure MySQL Installation

```bash
sudo mysql_secure_installation
```

Answer the prompts:
- **Validate password component?** N (or Y if you want strong passwords)
- **Remove anonymous users?** Y
- **Disallow root login remotely?** Y
- **Remove test database?** Y
- **Reload privilege tables?** Y

### Step 6.2: Create Database and User

```bash
# Login to MySQL as root
sudo mysql
```

In MySQL prompt, run:

```sql
-- Create database
CREATE DATABASE fin_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user (replace 'your_password' with a strong password)
CREATE USER 'fintracker'@'localhost' IDENTIFIED BY 'your_strong_password_here';

-- Grant privileges
GRANT ALL PRIVILEGES ON fin_tracker.* TO 'fintracker'@'localhost';

-- Flush privileges
FLUSH PRIVILEGES;

-- Verify database
SHOW DATABASES;

-- Exit
EXIT;
```

### Step 6.3: Test Database Connection

```bash
mysql -u fintracker -p fin_tracker
```

Enter the password you created. If successful, you'll see:
```
mysql>
```

Type `EXIT;` to exit.

---

## Part 7: Deploy Spring Boot Application

### Step 7.1: Create Application Directory

```bash
# Create directory for the application
sudo mkdir -p /opt/fintracker
sudo chown ubuntu:ubuntu /opt/fintracker
cd /opt/fintracker
```

### Step 7.2: Clone Your Repository

```bash
# Clone your repository
git clone https://github.com/cey-labs/fin-tracker-backend.git
cd fin-tracker-backend

# Checkout deployment branch
git checkout claude/aws-deployment-options-Ze6ps
```

### Step 7.3: Build the Application

**Option A: Build on Server (Recommended)**

```bash
# Build using Maven wrapper
./mvnw clean package -DskipTests

# The JAR file will be in target/ directory
ls -lh target/*.jar
```

**Option B: Build Locally and Upload**

On your local machine:
```bash
# Build the JAR
./mvnw clean package -DskipTests

# Upload to server (replace YOUR_PUBLIC_IP)
scp -i ~/.ssh/oracle-fin-tracker.key \
  target/fintracker-backend-0.0.1-SNAPSHOT.jar \
  ubuntu@YOUR_PUBLIC_IP:/opt/fintracker/fin-tracker-backend/
```

### Step 7.4: Create Environment Variables File

```bash
# Create .env file for configuration
sudo nano /opt/fintracker/fin-tracker-backend/.env
```

Add this content (replace with your values):

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=fin_tracker
DB_USERNAME=fintracker
DB_PASSWORD=your_strong_password_here

# Application Configuration
APP_DEFAULT_CURRENCY=LKR
SPRING_PROFILES_ACTIVE=prod

# JVM Options
JAVA_OPTS=-Xmx768m -Xms512m
```

Save and exit (Ctrl+X, then Y, then Enter)

### Step 7.5: Create Production Application Properties

The production config will be created in the next step with proper environment variable interpolation.

### Step 7.6: Test Run the Application

```bash
# Set environment variables
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=fin_tracker
export DB_USERNAME=fintracker
export DB_PASSWORD=your_strong_password_here
export APP_DEFAULT_CURRENCY=LKR

# Run the application
java -jar target/fintracker-backend-0.0.1-SNAPSHOT.jar
```

Watch the logs. You should see:
```
Started FinTrackerBackendApplication in X.XXX seconds
```

The Flyway migrations should run automatically and create all tables.

**Test the API:**

Open a new terminal (keep the app running) and SSH into the server again:

```bash
curl http://localhost:8080/api/v1/users
```

You should get a response (likely empty array: `[]`)

If everything works, press **Ctrl+C** to stop the application.

---

## Part 8: Configure Application as Service

To keep your application running 24/7 and auto-restart on crashes/reboots, create a systemd service.

### Step 8.1: Create Systemd Service File

The service file will be created using the deployment script in the repository.

```bash
# Use the service file from the repository
sudo cp /opt/fintracker/fin-tracker-backend/deployment/fintracker.service /etc/systemd/system/
```

### Step 8.2: Start and Enable Service

```bash
# Reload systemd to recognize new service
sudo systemctl daemon-reload

# Start the service
sudo systemctl start fintracker

# Enable auto-start on boot
sudo systemctl enable fintracker

# Check status
sudo systemctl status fintracker
```

You should see:
```
● fintracker.service - Fin Tracker Backend Spring Boot Application
   Loaded: loaded (/etc/systemd/system/fintracker.service; enabled)
   Active: active (running) since ...
```

### Step 8.3: View Logs

```bash
# View real-time logs
sudo journalctl -u fintracker -f

# View last 100 lines
sudo journalctl -u fintracker -n 100

# View logs from today
sudo journalctl -u fintracker --since today
```

### Step 8.4: Service Management Commands

```bash
# Start service
sudo systemctl start fintracker

# Stop service
sudo systemctl stop fintracker

# Restart service
sudo systemctl restart fintracker

# Check status
sudo systemctl status fintracker

# Enable auto-start
sudo systemctl enable fintracker

# Disable auto-start
sudo systemctl disable fintracker
```

---

## Part 9: Setup Nginx Reverse Proxy (Optional but Recommended)

Nginx will:
- Serve your app on port 80 (HTTP)
- Enable SSL/HTTPS later
- Add security headers
- Handle gzip compression
- Act as load balancer if needed

### Step 9.1: Install Nginx

```bash
sudo apt install -y nginx
```

### Step 9.2: Configure Nginx

```bash
# Create Nginx configuration
sudo nano /etc/nginx/sites-available/fintracker
```

Use the nginx configuration from the repository or paste this:

```nginx
server {
    listen 80;
    server_name YOUR_DOMAIN_OR_IP;

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # Logging
    access_log /var/log/nginx/fintracker-access.log;
    error_log /var/log/nginx/fintracker-error.log;

    # Proxy settings
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Health check endpoint
    location /actuator/health {
        proxy_pass http://localhost:8080/actuator/health;
        access_log off;
    }
}
```

Replace `YOUR_DOMAIN_OR_IP` with:
- Your domain name if you have one (e.g., `api.fintracker.com`)
- Your Oracle Cloud public IP if you don't (e.g., `158.101.100.50`)

Save and exit (Ctrl+X, Y, Enter)

### Step 9.3: Enable Configuration

```bash
# Create symbolic link
sudo ln -s /etc/nginx/sites-available/fintracker /etc/nginx/sites-enabled/

# Remove default site
sudo rm /etc/nginx/sites-enabled/default

# Test configuration
sudo nginx -t
```

You should see:
```
nginx: configuration file /etc/nginx/nginx.conf test is successful
```

### Step 9.4: Start Nginx

```bash
# Restart Nginx
sudo systemctl restart nginx

# Enable auto-start
sudo systemctl enable nginx

# Check status
sudo systemctl status nginx
```

### Step 9.5: Test the Setup

From your local machine:
```bash
curl http://YOUR_PUBLIC_IP/api/v1/users
```

You should get a JSON response!

---

## Part 10: Domain and SSL Setup (Optional)

### Step 10.1: Point Domain to Server

If you have a domain name:

1. Go to your domain registrar (Namecheap, GoDaddy, etc.)
2. Add an **A Record**:
   - Name: `api` (or `@` for root domain)
   - Value: Your Oracle Cloud public IP
   - TTL: 300 (5 minutes)
3. Wait for DNS propagation (5-30 minutes)

Test:
```bash
ping api.yourdomain.com
```

### Step 10.2: Install SSL Certificate with Let's Encrypt

```bash
# Install Certbot
sudo apt install -y certbot python3-certbot-nginx

# Get certificate (replace with your domain)
sudo certbot --nginx -d api.yourdomain.com

# Follow the prompts:
# - Enter email address
# - Agree to terms
# - Choose whether to redirect HTTP to HTTPS (choose 2 for redirect)
```

Certbot will automatically:
- Obtain SSL certificate
- Configure Nginx for HTTPS
- Set up auto-renewal

### Step 10.3: Test SSL

Visit: `https://api.yourdomain.com/api/v1/users`

Check SSL rating: https://www.ssllabs.com/ssltest/

### Step 10.4: Auto-renewal

Certbot automatically sets up renewal. Test it:

```bash
# Dry run renewal
sudo certbot renew --dry-run
```

Certificates auto-renew every 60 days.

---

## Maintenance and Updates

### Update Application

```bash
# SSH into server
ssh -i ~/.ssh/oracle-fin-tracker.key ubuntu@YOUR_PUBLIC_IP

# Navigate to app directory
cd /opt/fintracker/fin-tracker-backend

# Pull latest changes
git pull origin main

# Rebuild
./mvnw clean package -DskipTests

# Restart service
sudo systemctl restart fintracker

# Check logs
sudo journalctl -u fintracker -f
```

### Database Backup

```bash
# Create backup directory
mkdir -p ~/backups

# Backup database
sudo mysqldump -u fintracker -p fin_tracker > ~/backups/fin_tracker_$(date +%Y%m%d_%H%M%S).sql

# Compress backup
gzip ~/backups/fin_tracker_*.sql
```

### Automate Backups (Cron)

```bash
# Edit crontab
crontab -e

# Add this line (daily backup at 2 AM)
0 2 * * * sudo mysqldump -u fintracker -p'your_password' fin_tracker | gzip > ~/backups/fin_tracker_$(date +\%Y\%m\%d_\%H\%M\%S).sql.gz

# Keep only last 7 days (run at 3 AM)
0 3 * * * find ~/backups -name "fin_tracker_*.sql.gz" -mtime +7 -delete
```

### Restore Database

```bash
# Unzip backup
gunzip ~/backups/fin_tracker_20260115_020000.sql.gz

# Restore
mysql -u fintracker -p fin_tracker < ~/backups/fin_tracker_20260115_020000.sql
```

### Monitor Resources

```bash
# Check disk usage
df -h

# Check memory
free -h

# Check CPU and memory in real-time
htop

# Check application logs
sudo journalctl -u fintracker --since "1 hour ago"
```

---

## Troubleshooting

### Application Won't Start

```bash
# Check service status
sudo systemctl status fintracker

# Check logs
sudo journalctl -u fintracker -n 100

# Common issues:
# 1. Database connection failed - Check MySQL is running
sudo systemctl status mysql

# 2. Port already in use
sudo lsof -i :8080

# 3. Permission issues
ls -la /opt/fintracker/fin-tracker-backend/
```

### Database Connection Issues

```bash
# Test MySQL connection
mysql -u fintracker -p fin_tracker

# Check MySQL is running
sudo systemctl status mysql

# View MySQL logs
sudo journalctl -u mysql -n 50

# Restart MySQL
sudo systemctl restart mysql
```

### Nginx Issues

```bash
# Test configuration
sudo nginx -t

# Check status
sudo systemctl status nginx

# View error logs
sudo tail -f /var/log/nginx/fintracker-error.log

# Restart Nginx
sudo systemctl restart nginx
```

### Can't Connect from Outside

1. **Check Oracle Cloud Security List**
   - Go to OCI Console → Compute → Instance → Subnet → Security List
   - Ensure ingress rules for ports 80, 443, 8080 exist

2. **Check Ubuntu Firewall**
   ```bash
   sudo ufw status
   sudo ufw allow 80/tcp
   sudo ufw allow 443/tcp
   ```

3. **Check Nginx is running**
   ```bash
   sudo systemctl status nginx
   ```

4. **Check application is running**
   ```bash
   sudo systemctl status fintracker
   curl http://localhost:8080/api/v1/users
   ```

### High Memory Usage

```bash
# Check memory
free -h

# Edit JVM options in .env file
sudo nano /opt/fintracker/fin-tracker-backend/.env

# Reduce memory (for 1GB instance)
JAVA_OPTS=-Xmx768m -Xms512m

# Restart application
sudo systemctl restart fintracker
```

### View Application Logs

```bash
# Real-time logs
sudo journalctl -u fintracker -f

# Last 100 lines
sudo journalctl -u fintracker -n 100

# Logs from today
sudo journalctl -u fintracker --since today

# Logs with errors only
sudo journalctl -u fintracker -p err
```

---

## Performance Optimization Tips

### 1. Enable Swap (for 1GB RAM instances)

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

### 2. Optimize JVM for Low Memory

In `/opt/fintracker/fin-tracker-backend/.env`:
```bash
JAVA_OPTS=-Xmx768m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

### 3. Enable Nginx Caching

Add to nginx configuration:
```nginx
# Cache static content
location ~* \.(jpg|jpeg|png|gif|ico|css|js)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
}
```

### 4. Monitor Performance

```bash
# Install monitoring tools
sudo apt install -y htop iotop nethogs

# Monitor in real-time
htop  # CPU and RAM
iotop  # Disk I/O
nethogs  # Network usage
```

---

## Security Best Practices

### 1. Keep System Updated

```bash
# Update regularly
sudo apt update && sudo apt upgrade -y

# Enable automatic security updates
sudo apt install -y unattended-upgrades
sudo dpkg-reconfigure --priority=low unattended-upgrades
```

### 2. Disable Root Login

```bash
sudo nano /etc/ssh/sshd_config
```

Ensure these lines:
```
PermitRootLogin no
PasswordAuthentication no
```

Restart SSH:
```bash
sudo systemctl restart sshd
```

### 3. Install Fail2ban (Prevent Brute Force)

```bash
sudo apt install -y fail2ban

# Start and enable
sudo systemctl start fail2ban
sudo systemctl enable fail2ban
```

### 4. Use Environment Variables (Never Hardcode Passwords)

Always use the `.env` file for sensitive data, never commit passwords to Git.

---

## Cost Monitoring

Even on free tier, monitor your usage:

1. Go to OCI Console
2. Click **☰ Menu** → **Billing & Cost Management** → **Cost Analysis**
3. Set up budget alerts:
   - **☰ Menu** → **Billing & Cost Management** → **Budgets**
   - Create alert at $1 to catch accidental paid usage

---

## Next Steps

Now that your application is deployed:

1. **Test all API endpoints** thoroughly
2. **Set up monitoring** (consider Oracle Cloud Monitoring or third-party like UptimeRobot)
3. **Configure backups** (automate database dumps)
4. **Set up CI/CD** (GitHub Actions to auto-deploy on push)
5. **Add Spring Boot Actuator** endpoints for health checks
6. **Monitor logs** regularly for errors
7. **Document your API** (consider Swagger/OpenAPI)

---

## Useful Commands Reference

```bash
# Application
sudo systemctl start|stop|restart|status fintracker
sudo journalctl -u fintracker -f

# MySQL
sudo systemctl start|stop|restart|status mysql
mysql -u fintracker -p fin_tracker

# Nginx
sudo systemctl start|stop|restart|status nginx
sudo nginx -t

# Firewall
sudo ufw status
sudo ufw allow PORT/tcp

# System
htop  # Monitor resources
df -h  # Disk usage
free -h  # Memory usage
top  # Process monitor

# Logs
sudo journalctl -u fintracker -f  # App logs
sudo tail -f /var/log/nginx/fintracker-error.log  # Nginx logs
```

---

## Support and Resources

- **Oracle Cloud Docs:** https://docs.oracle.com/en-us/iaas/
- **Spring Boot Docs:** https://spring.io/projects/spring-boot
- **Nginx Docs:** https://nginx.org/en/docs/
- **MySQL Docs:** https://dev.mysql.com/doc/

---

## Congratulations! 🎉

Your Fin-Tracker-Backend is now deployed on Oracle Cloud Free Tier and will run 24/7 at no cost!

Your application is accessible at:
- HTTP: `http://YOUR_PUBLIC_IP/api/v1/users`
- HTTPS (if configured): `https://api.yourdomain.com/api/v1/users`

**Remember to:**
- Keep your SSH private key safe
- Regularly update your system and application
- Monitor logs for issues
- Back up your database

Happy coding! 🚀
