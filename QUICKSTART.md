# Quick Start Guide

## 🚀 Get Running in 5 Minutes

### 1. Install MySQL (if not already installed)
```bash
sudo apt update
sudo apt install mysql-server -y
sudo systemctl start mysql
```

### 2. Run Database Setup Script
```bash
./setup-database.sh
```

### 3. Configure Environment
```bash
# Copy example environment file
cp .env.example .env

# Edit if you want custom settings (optional)
nano .env
```

### 4. Run the Application
```bash
./run-app.sh
```

That's it! The application will be available at `http://localhost:8080`

## 📝 What Happens on First Run?

1. **Flyway migrations** automatically create all database tables
2. **Account fields** are set up including:
   - ✓ Name, color, currency
   - ✓ Account types (General, Credit, Overdraft)
   - ✓ Type-specific fields (limits, due dates, balance types)
   - ✓ Statistics exclusion flag
3. **API endpoints** become available immediately

## 🧪 Test the Setup

Create your first account:
```bash
# First, you'll need to create a user via User API
# Then create an account:
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Savings Account",
    "initialAmount": 10000.00,
    "userId": 1,
    "color": "#2e7d32",
    "accountType": "GENERAL",
    "currency": "LKR",
    "excludeFromStatistics": false
  }'
```

## 🔍 Verify Everything Works

```bash
# Check MySQL connection
mysql -u fintracker_user -p
# Password: fintracker_password
# Then: SHOW DATABASES;

# Check application logs
# Look for: "Started FinTrackerBackendApplication"

# Test API
curl http://localhost:8080/api/accounts/user/1
```

## ❓ Having Issues?

See [SETUP.md](SETUP.md) for detailed troubleshooting steps.

## 📚 Account Types Supported

### General Account
Basic account with balance tracking

### Credit Card Account
- Credit limit
- Due day of month
- Balance type (amount owed vs available credit)

### Overdraft Account
- Overdraft limit
- Due day of month
- Balance type (actual vs available with overdraft)

All account types support:
- Custom colors (hex codes)
- Multiple currencies
- Statistics inclusion/exclusion
- Balance tracking

## 🎯 Next Steps

1. Explore the [full API documentation](SETUP.md#api-endpoints)
2. Create different account types
3. Set up categories and labels
4. Create financial records
5. Build recurring templates
