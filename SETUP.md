# Fin Tracker Backend - Local Setup Guide

## Prerequisites
- Java 21 (already installed ✓)
- MySQL 8.0+
- Maven (for building)

## Step 1: Install MySQL

```bash
# Update package lists
sudo apt update

# Install MySQL Server
sudo apt install mysql-server -y

# Start MySQL service
sudo systemctl start mysql
sudo systemctl enable mysql

# Check MySQL status
sudo systemctl status mysql
```

## Step 2: Configure MySQL Database

Run the setup script or follow these manual steps:

### Option A: Use the setup script (recommended)
```bash
chmod +x setup-database.sh
./setup-database.sh
```

### Option B: Manual setup
```bash
# Login to MySQL as root
sudo mysql -u root

# Run these SQL commands:
CREATE DATABASE fin_tracker;
CREATE USER 'fintracker_user'@'localhost' IDENTIFIED BY 'fintracker_password';
GRANT ALL PRIVILEGES ON fin_tracker.* TO 'fintracker_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

## Step 3: Set Environment Variables

```bash
# Add to your ~/.bashrc or ~/.zshrc
export DB_USERNAME=fintracker_user
export DB_PASSWORD=fintracker_password
export APP_DEFAULT_CURRENCY=LKR

# Or create a .env file (see .env.example)
# Then source it before running:
source .env
```

## Step 4: Build and Run

```bash
# Build the application
mvn clean install

# Run the application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/cashai-backend-0.0.1-SNAPSHOT.jar
```

## Step 5: Verify Setup

The application should start on port 8080. Check the logs for:
- ✓ Flyway migrations completed successfully
- ✓ Application started successfully

Test the endpoints:
```bash
# Health check (if actuator is enabled)
curl http://localhost:8080/actuator/health

# Or test account endpoints (after creating a user)
curl http://localhost:8080/api/accounts/user/1
```

## Database Schema

Flyway will automatically create these tables on first run:
- `user` - User accounts
- `account` - Financial accounts with support for:
  - General accounts
  - Credit card accounts (with limit, due date, balance type)
  - Overdraft accounts (with limit, due date, balance type)
- Additional tables for categories, labels, financial records, and templates

## Troubleshooting

### MySQL Connection Issues
- Check MySQL is running: `sudo systemctl status mysql`
- Verify credentials: `mysql -u fintracker_user -p`
- Check database exists: `SHOW DATABASES;`

### Port Already in Use
If port 8080 is taken, add to application.yml:
```yaml
server:
  port: 8081
```

### Flyway Migration Errors
If migrations fail, you can reset:
```bash
# Login to MySQL
sudo mysql -u root

# Drop and recreate database
DROP DATABASE fin_tracker;
CREATE DATABASE fin_tracker;
EXIT;

# Restart the application
```

## API Endpoints

### Account Management
- `POST /api/accounts` - Create account
- `GET /api/accounts/user/{userId}` - Get all accounts for user
- `GET /api/accounts/{id}` - Get account by ID
- `PUT /api/accounts/{id}` - Update account
- `DELETE /api/accounts/{id}` - Delete account

### Example: Create General Account
```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Savings",
    "initialAmount": 10000.00,
    "userId": 1,
    "color": "#2e7d32",
    "accountType": "GENERAL",
    "currency": "LKR",
    "excludeFromStatistics": false
  }'
```

### Example: Create Credit Card Account
```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Credit Card",
    "initialAmount": 0.00,
    "userId": 1,
    "color": "#d32f2f",
    "accountType": "CREDIT_ACCOUNT",
    "currency": "LKR",
    "excludeFromStatistics": false,
    "creditCardLimit": 50000.00,
    "creditDueDayOfMonth": 15,
    "creditBalanceType": "CREDIT_BALANCE"
  }'
```

## Next Steps

1. Create user accounts first (User API endpoints)
2. Create accounts for testing different types
3. Start creating financial records
4. Explore category and label management
5. Set up recurring transaction templates
