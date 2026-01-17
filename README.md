# Financial Tracker Backend

A comprehensive personal finance management REST API built with Spring Boot. This backend provides robust support for managing multiple account types, financial transactions, categorization, labeling, and reusable templates for efficient financial tracking.

## Features

### Core Functionality
- **Multi-User Support** - Complete user management system
- **Account Management** - Support for multiple account types:
  - General accounts
  - Credit card accounts (with credit limits and billing cycles)
  - Overdraft accounts (with overdraft limits and balance tracking)
- **Financial Records** - Track income, expenses, and transfers with:
  - Multiple payment types (Cash, Card, Bank Transfer, UPI, Cheque, etc.)
  - Payment status tracking (Cleared, Pending, Reconciled, Failed, etc.)
  - Inter-account transfers
- **Category System** - Organize transactions with:
  - System-default categories
  - User-created custom categories
  - Support for expense, income, or dual-purpose categories
- **Labeling System** - Tag transactions with color-coded labels for better organization
- **Templates** - Create reusable transaction templates for recurring expenses/income
- **Advanced Filtering** - Query financial records by date range, account, category, and type

### Account Types
1. **General Account** - Standard bank accounts or cash accounts
2. **Credit Account** - Credit cards with credit limits, due dates, and balance type tracking
3. **Overdraft Account** - Accounts with overdraft facilities and limit management

## Technologies

- **Framework:** Spring Boot 3.2.0
- **Language:** Java 21
- **Build Tool:** Maven with Maven Wrapper
- **Database:**
  - Production: MySQL
  - Testing: H2 (in-memory, MySQL compatible mode)
- **ORM:** Spring Data JPA with Hibernate
- **Database Migrations:** Flyway
- **Validation:** Jakarta Bean Validation
- **Testing:** JUnit 5 + Mockito

## Prerequisites

- Java 21 or higher
- Maven 3.6+ (or use included Maven Wrapper)
- MySQL 8.0+ (for production)
- Git

## Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd fin-tracker-backend
```

2. **Configure database connection**

Create a MySQL database:
```sql
CREATE DATABASE fin_tracker;
```

Set environment variables:
```bash
export DB_USERNAME=your_mysql_username
export DB_PASSWORD=your_mysql_password
export APP_DEFAULT_CURRENCY=LKR  # Optional, defaults to LKR
```

Alternatively, update `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fin_tracker
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
```

3. **Build the project**
```bash
./mvnw clean install
```

## Running the Application

### Using Maven Wrapper
```bash
./mvnw spring-boot:run
```

### Using packaged JAR
```bash
./mvnw package
java -jar target/fin-tracker-backend-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

## Database Migrations

The application uses Flyway for database version control. Migrations run automatically on application startup:

- **V1**: Creates core user and account tables
- **V2**: Adds account type enhancements (credit, overdraft)
- **V3**: Creates financial records system (categories, labels, templates, records)

Migration files are located in `src/main/resources/db/migration/`

## API Endpoints

### Users API
**Base Path:** `/api/v1/users`

- `GET /api/v1/users` - List all users
- `GET /api/v1/users/{id}` - Get user by ID
- `POST /api/v1/users` - Create new user
- `PUT /api/v1/users/{userId}` - Update user
- `DELETE /api/v1/users/{userId}` - Delete user

### Accounts API
**Base Path:** `/api/accounts`

- `GET /api/accounts/user/{userId}` - Get user's accounts
- `GET /api/accounts/{id}` - Get account by ID
- `POST /api/accounts` - Create account
- `PUT /api/accounts/{id}` - Update account
- `DELETE /api/accounts/{id}` - Delete account

### Financial Records API
**Base Path:** `/api/records`

- `POST /api/records` - Create financial record
- `GET /api/records` - Get records (supports filtering by userId, accountId, categoryId, recordType, date range)
- `GET /api/records/{id}` - Get record by ID
- `PUT /api/records/{id}` - Update record
- `DELETE /api/records/{id}` - Delete record
- `GET /api/records/user/{userId}/account/{accountId}` - Get records by user and account
- `GET /api/records/user/{userId}/range` - Get records by date range

### Categories API
**Base Path:** `/api/categories`

- `POST /api/categories` - Create category
- `GET /api/categories/user/{userId}` - Get accessible categories
- `GET /api/categories/{id}` - Get category by ID
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category
- `GET /api/categories/user/{userId}/type/{categoryType}` - Get by type
- `GET /api/categories/user/{userId}/expense` - Get expense categories
- `GET /api/categories/user/{userId}/income` - Get income categories
- `GET /api/categories/user/{userId}/custom` - Get user-created categories
- `GET /api/categories/system-defaults` - Get system defaults
- `GET /api/categories/user/{userId}/search` - Search categories

### Labels API
**Base Path:** `/api/labels`

- `POST /api/labels` - Create label
- `GET /api/labels/user/{userId}` - Get user's labels
- `GET /api/labels/{id}` - Get label by ID
- `PUT /api/labels/{id}` - Update label
- `DELETE /api/labels/{id}` - Delete label
- `GET /api/labels/user/{userId}/used` - Get used labels
- `GET /api/labels/user/{userId}/unused` - Get unused labels
- `GET /api/labels/user/{userId}/search` - Search labels
- `GET /api/labels/user/{userId}/color` - Get labels by color

### Templates API
**Base Path:** `/api/templates`

- `POST /api/templates` - Create template
- `GET /api/templates/user/{userId}` - Get user's templates
- `GET /api/templates/{id}` - Get template by ID
- `PUT /api/templates/{id}` - Update template
- `DELETE /api/templates/{id}` - Delete template
- `GET /api/templates/user/{userId}/type/{recordType}` - Get by record type
- `GET /api/templates/user/{userId}/search` - Search templates
- `GET /api/templates/{id}/usage-count` - Get usage count
- `POST /api/templates/{id}/use` - Create record from template

## Testing

### Run all tests
```bash
./mvnw test
```

### Run specific test class
```bash
./mvnw test -Dtest=UserServiceTest
```

The test suite includes comprehensive unit tests for all service layer classes:
- UserServiceTest
- AccountServiceTest
- CategoryServiceTest
- LabelServiceTest
- FinancialRecordServiceTest
- TemplateServiceTest

Tests use H2 in-memory database for isolation and speed.

## Project Structure

```
src/
├── main/
│   ├── java/com/ceylabs/fintrackerbackend/
│   │   ├── FinTrackerBackendApplication.java    # Main application entry point
│   │   ├── config/                              # Configuration classes
│   │   ├── controller/                          # REST API controllers
│   │   ├── dto/                                 # Data Transfer Objects
│   │   ├── enums/                               # Enumerations
│   │   ├── exception/                           # Exception handling
│   │   ├── model/                               # JPA Entity models
│   │   ├── repository/                          # Data access layer
│   │   └── service/                             # Business logic layer
│   └── resources/
│       ├── application.yml                      # Application configuration
│       └── db/migration/                        # Flyway migration scripts
└── test/
    ├── java/com/ceylabs/fintrackerbackend/
    │   └── service/                             # Service unit tests
    └── resources/
        └── application-test.yml                 # Test configuration
```

## Data Models

### Key Entities

#### User
- Basic user information (name, email, date of birth)
- Calculated age field

#### Account
- Multi-type support (General, Credit, Overdraft)
- Balance tracking with currency support
- Optional exclusion from statistics
- Credit card specific fields (limit, due date, balance type)
- Overdraft specific fields (limit, due date, balance type)

#### Financial Record
- Record types: EXPENSE, INCOME, TRANSFER
- Payment types: Cash, Card, Bank Transfer, UPI, Cheque, Online, Wallet, etc.
- Payment statuses: Cleared, Pending, Reconciled, Failed, Cancelled, Refunded
- Support for categories, labels, and templates
- Inter-account transfer capability

#### Category
- Type: EXPENSE, INCOME, or BOTH
- System defaults vs user-created
- Color and icon support

#### Label
- Custom tags with color coding
- Many-to-many relationship with financial records

#### Template
- Reusable transaction templates
- Usage tracking
- Quick record creation from templates

## Configuration

### Application Properties

Key configuration options in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fin_tracker
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

  flyway:
    enabled: true
    baseline-on-migrate: true

app:
  default-currency: ${APP_DEFAULT_CURRENCY:LKR}
```

## Error Handling

The API uses standardized error responses through `GlobalExceptionHandler`:

- **Validation Errors**: Returns field-specific validation messages with 400 Bad Request
- **Business Logic Errors**: Returns descriptive error messages with 400 Bad Request
- **Not Found Errors**: Returns 404 with appropriate message

## Development

### Code Style
- Clean architecture with separation of concerns
- Controller → Service → Repository pattern
- DTO pattern for API contracts
- Comprehensive validation using Jakarta Bean Validation

### Adding New Entities
1. Create entity class in `model/` package
2. Create repository interface in `repository/` package
3. Create service class in `service/` package
4. Create controller in `controller/` package
5. Add Flyway migration in `db/migration/`
6. Write unit tests in `test/` directory

## License

Copyright © 2026 CEY Labs. All rights reserved.
