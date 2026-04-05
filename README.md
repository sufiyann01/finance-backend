# Finance Data Processing and Access Control Backend

A Spring Boot REST API for managing financial records with role-based access control, JWT authentication, and dashboard analytics.

---

## Tech Stack

| Layer        | Technology                    |
|-------------|-------------------------------|
| Framework    | Spring Boot 3.2               |
| Language     | Java 17                       |
| Database     | PostgreSQL                    |
| ORM          | Spring Data JPA / Hibernate   |
| Security     | Spring Security + JWT (JJWT)  |
| Validation   | Jakarta Bean Validation       |
| Build Tool   | Maven                         |

---

## Project Structure

```
src/main/java/com/zorvyn/finance/
├── config/
│   └── DataSeeder.java           # Seeds default admin on startup
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── FinancialRecordController.java
│   └── DashboardController.java
├── dto/
│   ├── ApiResponse.java          # Generic response wrapper
│   ├── AuthDto.java
│   ├── UserDto.java
│   ├── FinancialRecordDto.java
│   └── DashboardSummaryDto.java
├── exception/
│   ├── ResourceNotFoundException.java
│   └── GlobalExceptionHandler.java
├── model/
│   ├── Role.java                 # VIEWER, ANALYST, ADMIN
│   ├── TransactionType.java      # INCOME, EXPENSE
│   ├── User.java
│   └── FinancialRecord.java
├── repository/
│   ├── UserRepository.java
│   └── FinancialRecordRepository.java
├── security/
│   ├── JwtUtils.java
│   ├── JwtAuthFilter.java
│   ├── UserDetailsServiceImpl.java
│   └── SecurityConfig.java
└── service/
    ├── AuthService.java
    ├── UserService.java
    ├── FinancialRecordService.java
    └── DashboardService.java
```

---

## Setup & Running

### 1. Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL running locally

### 2. Create Database
```sql
CREATE DATABASE finance_db;
```

### 3. Configure application.properties
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/finance_db
spring.datasource.username=postgres
spring.datasource.password=your_password_here
```

### 4. Run
```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`.  
On first startup, a default **admin** account is created automatically:
- Username: `admin`
- Password: `admin123`

---

## Roles & Access Control

| Endpoint Group          | VIEWER | ANALYST | ADMIN |
|------------------------|--------|---------|-------|
| `POST /api/auth/**`    | ✅     | ✅      | ✅    |
| `GET /api/dashboard/**`| ✅     | ✅      | ✅    |
| `GET /api/records/**`  | ❌     | ✅      | ✅    |
| `POST/PUT/DELETE /api/records/**` | ❌ | ❌ | ✅ |
| `ALL /api/users/**`    | ❌     | ❌      | ✅    |

---

## API Reference

All protected endpoints require:
```
Authorization: Bearer <token>
```

All responses follow this structure:
```json
{
  "success": true,
  "message": "...",
  "data": { ... },
  "timestamp": "2026-04-04T10:00:00"
}
```

---

### Auth

#### POST /api/auth/register
```json
{
  "username": "john",
  "email": "john@example.com",
  "password": "secret123",
  "role": "ANALYST"
}
```
Role defaults to `VIEWER` if not provided.

#### POST /api/auth/login
```json
{
  "username": "admin",
  "password": "admin123"
}
```
Response:
```json
{
  "token": "eyJ...",
  "username": "admin",
  "role": "ADMIN"
}
```

---

### Users (ADMIN only)

| Method | Endpoint        | Description         |
|--------|----------------|---------------------|
| GET    | /api/users      | List all users (paginated) |
| GET    | /api/users/{id} | Get user by ID      |
| POST   | /api/users      | Create user         |
| PUT    | /api/users/{id} | Update role/status  |
| DELETE | /api/users/{id} | Delete user         |

#### POST /api/users body:
```json
{
  "username": "jane",
  "email": "jane@example.com",
  "password": "pass123",
  "role": "VIEWER"
}
```

#### PUT /api/users/{id} body (all fields optional):
```json
{
  "role": "ANALYST",
  "active": false
}
```

---

### Financial Records

| Method | Endpoint          | Role Required   | Description              |
|--------|------------------|-----------------|--------------------------|
| GET    | /api/records      | ANALYST, ADMIN  | List with filters + pagination |
| GET    | /api/records/{id} | ANALYST, ADMIN  | Get record by ID         |
| POST   | /api/records      | ADMIN           | Create record            |
| PUT    | /api/records/{id} | ADMIN           | Update record            |
| DELETE | /api/records/{id} | ADMIN           | Soft delete record       |

#### GET /api/records - Query Parameters:
| Param       | Example         | Description             |
|-------------|----------------|-------------------------|
| `type`      | `INCOME`       | Filter by type          |
| `category`  | `Food`         | Filter by category      |
| `startDate` | `2026-01-01`   | Filter from date        |
| `endDate`   | `2026-03-31`   | Filter to date          |
| `page`      | `0`            | Page number (0-indexed) |
| `size`      | `10`           | Page size               |
| `sort`      | `date,desc`    | Sort field & direction  |

Example: `GET /api/records?type=EXPENSE&category=Food&startDate=2026-01-01&size=5`

#### POST /api/records body:
```json
{
  "amount": 1500.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2026-04-01",
  "notes": "April salary"
}
```

---

### Dashboard

#### GET /api/dashboard/summary
Access: VIEWER, ANALYST, ADMIN

Response:
```json
{
  "totalIncome": 50000.00,
  "totalExpenses": 32000.00,
  "netBalance": 18000.00,
  "categoryTotals": {
    "Salary": 50000.00,
    "Food": 8000.00,
    "Utilities": 4000.00
  },
  "recentActivity": [ ... ],
  "monthlyTrends": [
    { "month": "2025-11", "income": 8000.00, "expenses": 5000.00 },
    { "month": "2025-12", "income": 8500.00, "expenses": 6000.00 }
  ]
}
```

---

## Assumptions & Design Decisions

1. **Roles are user-level** — one role per user, assigned at registration or by admin.
2. **Soft delete** — financial records are never physically removed; they are marked `deleted=true` and hidden from all queries. This preserves audit history.
3. **JWT expiry** — tokens expire after 24 hours (configurable in `application.properties`).
4. **VIEWER access** — viewers can see the dashboard summary but cannot read individual transaction records. This is intentional: they get aggregate data only.
5. **Password storage** — passwords are hashed with BCrypt and never returned in any response.
6. **Pagination defaults** — 10 items per page, sorted by date descending.
7. **Amount precision** — stored as `DECIMAL(15,2)` to handle large financial values accurately.

---

## Optional Enhancements Implemented

- ✅ JWT Authentication
- ✅ Pagination on all list endpoints
- ✅ Soft delete for financial records
- ✅ Filtering by type, category, date range
- ✅ Monthly trend analytics
- ✅ Consistent API response wrapper
- ✅ Global exception handling with proper HTTP status codes
- ✅ Auto-seeded admin account on startup
