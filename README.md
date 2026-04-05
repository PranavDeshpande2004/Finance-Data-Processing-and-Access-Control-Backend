# Finance Dashboard Backend

A role-based finance management system built with Spring Boot and MySQL.
Users can track income and expenses, view dashboard analytics, and manage financial records — all protected by JWT authentication and role-based access control.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 4.x (Java 17) |
| Security | Spring Security + JWT |
| Database | MySQL + JPA / Hibernate |
| Rate Limiting | Bucket4j |
| API Docs | Swagger UI (SpringDoc OpenAPI) |
| Testing | JUnit 5 + Mockito + MockMvc |

---

## Roles

| Role | What they can do |
|---|---|
| **Viewer** | View dashboard summary only |
| **Analyst** | View transactions + dashboard insights |
| **Admin** | Full access — manage users and transactions |

> Every new user registers as **Viewer** by default. Only an Admin can promote a user to Analyst.

---

## Quick Start

### 1. Prerequisites

- Java 17+
- MySQL 8+
- Maven 3.8+

### 2. Clone and Configure

```bash
git clone https://github.com/your-username/finance-backend.git
cd finance-backend
```

Open `src/main/resources/application.properties` and update:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/finance_db?createDatabaseIfNotExist=true
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

### 3. Run

```bash
mvn spring-boot:run
```

App starts at `http://localhost:8080`

### 4. Create First Admin

Since no one can register as Admin, insert one directly in MySQL:

```sql
USE finance_db;

INSERT INTO users (name, email, password, role, is_active, created_at, updated_at)
VALUES (
  'Super Admin',
  'admin@finance.com',
  '$2a$12$ase5VmExSP51RvO9P3p0UeGEPbHzoMYrDSP/TKaT9CvJlNdDlLGOu',
  'ADMIN', true, NOW(), NOW()
);
-- Password: admin123
```

### 5. Open Swagger UI

```
http://localhost:8080/swagger-ui.html
```

Click **Authorize**, paste your JWT token, and test all endpoints.

---

## API Endpoints

### Auth — Public

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register (auto-assigned Viewer role) |
| POST | `/api/auth/login` | Login and receive JWT token |

### Transactions — Analyst + Admin

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/transactions` | List all (paginated + filterable) |
| GET | `/api/transactions/{id}` | Get one by ID |

### Transactions — Admin Only

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/transactions` | Create a transaction |
| PUT | `/api/transactions/{id}` | Update a transaction |
| DELETE | `/api/transactions/{id}` | Soft delete a transaction |

### Dashboard — All Roles

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/dashboard/summary` | Income, expenses, net balance, trends |
| GET | `/api/dashboard/total?type=INCOME` | Total by type |
| GET | `/api/dashboard/category-breakdown` | Totals per category |
| GET | `/api/dashboard/trends/monthly` | Monthly income vs expense |
| GET | `/api/dashboard/recent` | Last 10 transactions |

### Users — Admin Only

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/users` | List all users |
| GET | `/api/users/{id}` | Get user by ID |
| PUT | `/api/users/{id}` | Update name, role, status |
| PATCH | `/api/users/{id}/toggle-status` | Activate or deactivate user |

---

## Transaction Filters

The `GET /api/transactions` endpoint supports these query parameters:

```
?type=INCOME or EXPENSE
?category=Salary
?startDate=2024-01-01
?endDate=2024-01-31
?search=grocery
?page=0&size=10&sortBy=date&sortDir=desc
```

---

## How Authentication Works

```
Register → JWT issued (role = VIEWER)
Login    → JWT issued

Every request:
  JWT token validated → email extracted
  Role loaded from DB → access granted or denied
```

Role changes take effect on the next request — no re-login needed.

---

## Database Schema

```
users
  id, name, email, password, role, is_active, created_at, updated_at

transactions
  id, user_id (FK), amount, type, category, date,
  notes, is_deleted, deleted_at, created_at, updated_at
```

> Deleted transactions are **soft deleted** — they stay in the database with `is_deleted = true` and are never shown in queries.

---

## Security

| Check | Behavior |
|---|---|
| Missing token | 401 Unauthorized |
| Wrong role | 403 Forbidden |
| Invalid input | 400 Bad Request |
| Rate limit exceeded | 429 Too Many Requests |
| Not found | 404 Not Found |

Rate limit: **20 requests per minute per user**

---

## Running Tests

```bash
mvn test
```

39 tests covering auth, transactions, dashboard, and role-based access.

---

## Project Structure

```
src/main/java/com/finance/
├── controller/      # HTTP endpoints
├── service/         # Business logic
├── repository/      # Database queries
├── entity/          # JPA entities
├── dto/             # Request and response objects
├── security/        # JWT filter and auth
├── config/          # Security, Swagger, rate limit
├── exception/       # Error handling
└── enums/           # Role, TransactionType
```

---

## Assumptions

- Every new user is a Viewer — role assignment is Admin-only to prevent privilege escalation
- Transactions are never hard deleted — soft delete preserves audit history
- JWT tokens carry only the user email; roles are always loaded fresh from the database
- Rate limiting is per authenticated user (falls back to IP for unauthenticated requests)
