#!/bin/bash

#############################################
# Fin-Tracker Backend - Server Setup Script
# For Oracle Cloud Ubuntu Instance
#############################################

set -e  # Exit on error

echo "================================================"
echo "Fin-Tracker Backend - Automated Server Setup"
echo "================================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# Check if running as root
if [ "$EUID" -eq 0 ]; then
    print_error "Please do not run this script as root or with sudo"
    exit 1
fi

print_info "This script will install and configure:"
print_info "  - Java 21"
print_info "  - MySQL 8.0"
print_info "  - Maven"
print_info "  - Nginx"
print_info "  - Git and other tools"
echo ""
read -p "Continue? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    print_error "Installation cancelled"
    exit 1
fi

echo ""
print_info "Starting installation..."
echo ""

#############################################
# Step 1: Update System
#############################################
print_info "Step 1: Updating system packages..."
sudo apt update
sudo apt upgrade -y
print_success "System updated"
echo ""

#############################################
# Step 2: Install Java 21
#############################################
print_info "Step 2: Installing Java 21..."

# Check if Java 21 is already installed
if java -version 2>&1 | grep -q "21\."; then
    print_success "Java 21 already installed"
else
    # Add Adoptium repository
    wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo apt-key add -
    echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list

    sudo apt update
    sudo apt install -y temurin-21-jdk
    print_success "Java 21 installed"
fi

java -version
echo ""

#############################################
# Step 3: Install MySQL
#############################################
print_info "Step 3: Installing MySQL 8.0..."

if systemctl is-active --quiet mysql; then
    print_success "MySQL already installed and running"
else
    sudo apt install -y mysql-server
    sudo systemctl start mysql
    sudo systemctl enable mysql
    print_success "MySQL installed and started"
fi
echo ""

#############################################
# Step 4: Install Git, Maven, and Tools
#############################################
print_info "Step 4: Installing development tools..."
sudo apt install -y git curl wget unzip maven htop
print_success "Development tools installed"
echo ""

#############################################
# Step 5: Install Nginx
#############################################
print_info "Step 5: Installing Nginx..."

if systemctl is-active --quiet nginx; then
    print_success "Nginx already installed and running"
else
    sudo apt install -y nginx
    sudo systemctl start nginx
    sudo systemctl enable nginx
    print_success "Nginx installed and started"
fi
echo ""

#############################################
# Step 6: Configure Firewall
#############################################
print_info "Step 6: Configuring UFW firewall..."

# Install UFW if not present
sudo apt install -y ufw

# Configure firewall rules
sudo ufw --force reset
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow 22/tcp comment 'SSH'
sudo ufw allow 80/tcp comment 'HTTP'
sudo ufw allow 443/tcp comment 'HTTPS'
sudo ufw allow 8080/tcp comment 'Spring Boot'
sudo ufw --force enable

print_success "Firewall configured"
sudo ufw status
echo ""

#############################################
# Step 7: Create Application Directory
#############################################
print_info "Step 7: Creating application directory..."

APP_DIR="/opt/fintracker"
sudo mkdir -p $APP_DIR
sudo chown $USER:$USER $APP_DIR

print_success "Application directory created at $APP_DIR"
echo ""

#############################################
# Step 8: Configure MySQL Database
#############################################
print_info "Step 8: Configuring MySQL database..."
echo ""

# Prompt for database password
read -sp "Enter password for MySQL user 'fintracker': " DB_PASSWORD
echo ""
read -sp "Confirm password: " DB_PASSWORD_CONFIRM
echo ""

if [ "$DB_PASSWORD" != "$DB_PASSWORD_CONFIRM" ]; then
    print_error "Passwords do not match!"
    exit 1
fi

if [ -z "$DB_PASSWORD" ]; then
    print_error "Password cannot be empty!"
    exit 1
fi

# Create database and user
sudo mysql <<MYSQL_SCRIPT
CREATE DATABASE IF NOT EXISTS fin_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'fintracker'@'localhost' IDENTIFIED BY '$DB_PASSWORD';
GRANT ALL PRIVILEGES ON fin_tracker.* TO 'fintracker'@'localhost';
FLUSH PRIVILEGES;
MYSQL_SCRIPT

print_success "Database 'fin_tracker' created"
print_success "User 'fintracker' created with privileges"
echo ""

#############################################
# Step 9: Create Environment File
#############################################
print_info "Step 9: Creating environment configuration..."

ENV_FILE="$APP_DIR/.env"
cat > $ENV_FILE <<EOF
# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=fin_tracker
DB_USERNAME=fintracker
DB_PASSWORD=$DB_PASSWORD

# Application Configuration
APP_DEFAULT_CURRENCY=LKR
SPRING_PROFILES_ACTIVE=prod

# JVM Options (adjust based on available RAM)
JAVA_OPTS=-Xmx768m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200
EOF

chmod 600 $ENV_FILE
print_success "Environment file created at $ENV_FILE"
echo ""

#############################################
# Step 10: Create Swap File (if RAM <= 2GB)
#############################################
TOTAL_RAM=$(free -g | awk '/^Mem:/{print $2}')
if [ "$TOTAL_RAM" -le 2 ]; then
    print_info "Step 10: Creating swap file (low RAM detected)..."

    if [ -f /swapfile ]; then
        print_success "Swap file already exists"
    else
        sudo fallocate -l 2G /swapfile
        sudo chmod 600 /swapfile
        sudo mkswap /swapfile
        sudo swapon /swapfile

        # Make permanent
        if ! grep -q '/swapfile' /etc/fstab; then
            echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
        fi

        print_success "2GB swap file created"
    fi
    free -h
else
    print_info "Step 10: Skipping swap creation (sufficient RAM)"
fi
echo ""

#############################################
# Summary
#############################################
echo ""
echo "================================================"
print_success "Server setup completed successfully!"
echo "================================================"
echo ""
print_info "Next steps:"
echo "  1. Clone your repository to $APP_DIR"
echo "  2. Build the application: ./mvnw clean package"
echo "  3. Copy and configure systemd service file"
echo "  4. Start the application service"
echo ""
print_info "Quick start commands:"
echo "  cd $APP_DIR"
echo "  git clone https://github.com/cey-labs/fin-tracker-backend.git"
echo "  cd fin-tracker-backend"
echo "  ./mvnw clean package -DskipTests"
echo ""
print_info "Database credentials saved in: $ENV_FILE"
print_info "Keep this file secure and never commit it to Git!"
echo ""
print_info "MySQL connection test:"
echo "  mysql -u fintracker -p fin_tracker"
echo ""
print_success "Installation complete! Happy coding! 🚀"
echo ""
