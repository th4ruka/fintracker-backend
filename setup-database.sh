#!/bin/bash

# Fin Tracker Database Setup Script
# This script sets up the MySQL database for the Fin Tracker application

set -e

echo "=========================================="
echo "Fin Tracker Database Setup"
echo "=========================================="
echo ""

# Database configuration
DB_NAME="fin_tracker"
DB_USER="fintracker_user"
DB_PASSWORD="fintracker_password"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if MySQL is installed
if ! command -v mysql &> /dev/null; then
    echo -e "${RED}Error: MySQL is not installed${NC}"
    echo "Please install MySQL first:"
    echo "  sudo apt update"
    echo "  sudo apt install mysql-server"
    exit 1
fi

# Check if MySQL service is running
if ! sudo systemctl is-active --quiet mysql; then
    echo -e "${YELLOW}MySQL service is not running. Starting it...${NC}"
    sudo systemctl start mysql
    echo -e "${GREEN}MySQL service started${NC}"
fi

echo "Creating database and user..."
echo ""

# Create database and user
sudo mysql -u root <<EOF
-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS ${DB_NAME};

-- Create user if it doesn't exist
CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';

-- Grant privileges
GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USER}'@'localhost';
FLUSH PRIVILEGES;

-- Verify database creation
USE ${DB_NAME};
SELECT 'Database created successfully!' AS Status;
EOF

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}✓ Database setup completed successfully!${NC}"
    echo ""
    echo "Database details:"
    echo "  Database name: ${DB_NAME}"
    echo "  Username: ${DB_USER}"
    echo "  Password: ${DB_PASSWORD}"
    echo "  Host: localhost"
    echo "  Port: 3306"
    echo ""
    echo "Next steps:"
    echo "  1. Set environment variables (or create .env file):"
    echo "     export DB_USERNAME=${DB_USER}"
    echo "     export DB_PASSWORD=${DB_PASSWORD}"
    echo "     export APP_DEFAULT_CURRENCY=LKR"
    echo ""
    echo "  2. Build and run the application:"
    echo "     mvn clean install"
    echo "     mvn spring-boot:run"
    echo ""
else
    echo -e "${RED}✗ Database setup failed${NC}"
    exit 1
fi
