#!/bin/bash

# Fin Tracker Application Runner
# This script sets up environment and runs the application

set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=========================================="
echo "Fin Tracker Backend"
echo "=========================================="
echo ""

# Check if .env file exists
if [ -f .env ]; then
    echo -e "${GREEN}Loading environment variables from .env${NC}"
    export $(cat .env | grep -v '^#' | xargs)
else
    echo -e "${YELLOW}Warning: .env file not found${NC}"
    echo "Using default environment variables or system-wide settings"
    echo "Create .env file from .env.example for custom configuration"
    echo ""
fi

# Check if database credentials are set
if [ -z "$DB_USERNAME" ] || [ -z "$DB_PASSWORD" ]; then
    echo -e "${RED}Error: Database credentials not set${NC}"
    echo "Please set the following environment variables:"
    echo "  DB_USERNAME"
    echo "  DB_PASSWORD"
    echo ""
    echo "You can create a .env file from .env.example"
    exit 1
fi

echo "Environment variables loaded:"
echo "  DB_USERNAME: $DB_USERNAME"
echo "  DB_PASSWORD: ********"
echo "  APP_DEFAULT_CURRENCY: ${APP_DEFAULT_CURRENCY:-LKR (default)}"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven is not installed${NC}"
    echo "Please install Maven first:"
    echo "  sudo apt install maven"
    exit 1
fi

# Check if MySQL is running
if ! sudo systemctl is-active --quiet mysql 2>/dev/null; then
    echo -e "${YELLOW}Warning: MySQL service may not be running${NC}"
    echo "Make sure MySQL is running before starting the application"
    echo ""
fi

# Build the application if target directory doesn't exist
if [ ! -d "target" ]; then
    echo -e "${YELLOW}Building application...${NC}"
    mvn clean install
    echo ""
fi

echo -e "${GREEN}Starting Fin Tracker Backend...${NC}"
echo "Application will be available at: http://localhost:8080"
echo "Press Ctrl+C to stop"
echo ""

# Run the application
mvn spring-boot:run
