# 🧠 Streaker Backend

Streaker is a habit-tracking backend designed for real-world use. Built with Java 21 and Spring Boot 3, it supports JWT authentication, Docker-based deployments, and structured logging — making it suitable for production-grade environments and DevOps experimentation.

---

## 📦 Tech Stack

- **Java 21**
- **Spring Boot 3**
- **JWT Authentication**
- **PostgreSQL**
- **Hibernate**, **Lombok**, **MapStruct**
- **Docker & Docker Compose**
- **Splunk (logging)**
- **JUnit & Mockito**
- **GitHub Actions (CI)**
- **PMD & JaCoCo**

---

## 🧱 Architecture

```text
[Frontend SPA]
     |
     |  HTTPS
     v
[ NGINX Reverse Proxy (SSL Termination) ]
     |
     v
[ Streaker Backend (Spring Boot) ]
     |
     | -- REST API (secured via JWT)
     |
     | -- Docker Logging (Splunk)
     |
     | -- Actuator (/health, /metrics)
     v
[ PostgreSQL DB ]
````

---

## 🚀 Getting Started (Local)

### ✅ Prerequisites

* Docker + Docker Compose
* Java 21 (for manual development builds)

### 🔧 Run with Docker

```bash
git clone https://github.com/MRegra/streaker-business.git
cd streaker-business/backend

# Copy environment file
cp .env.template .env

# Start backend + database + logging
docker compose up --build
```

Access:

* API: `http://localhost:8080`
* Splunk UI: `http://localhost:8000`
* Health: `http://localhost:8080/actuator/health`

---

## 🔐 Authentication

Streaker uses stateless JWT-based authentication.

### Login Example

```http
POST /api/v1/user/login
Content-Type: application/json

{
  "username": "admin",
  "password": "your-password"
}
```

**Response:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR..."
}
```

Include this token in all secured endpoints:

```
Authorization: Bearer <JWT>
```

---

## 📄 API Overview

> Full Swagger API documentation is planned.

| Endpoint                | Method | Auth | Description           |
| ----------------------- | ------ | ---- | --------------------- |
| `/api/v1/user/register` | POST   | ❌    | Register new user     |
| `/api/v1/user/login`    | POST   | ❌    | Login & get JWT token |
| `/api/v1/habit`         | CRUD   | ✅    | Manage habits         |
| `/api/v1/category`      | CRUD   | ✅    | Habit categories      |
| `/api/v1/streak`        | CRUD   | ✅    | Track habit streaks   |
| `/api/v1/log`           | CRUD   | ✅    | Habit activity log    |
| `/api/v1/reward`        | CRUD   | ✅    | Reward tracking       |

---

## 🧪 Testing

Run tests:

```bash
./mvnw test
```

Check coverage:

```bash
open target/site/jacoco/index.html
```

PMD and other quality checks are configured in the build pipeline.

---

## ⚙️ Environment Variables

Create an `.env` file with these keys:

| Key                          | Description                |
| ---------------------------- | -------------------------- |
| `SPRING_DATASOURCE_URL`      | JDBC connection string     |
| `SPRING_DATASOURCE_USERNAME` | DB user                    |
| `SPRING_DATASOURCE_PASSWORD` | DB password                |
| `JWT_SECRET`                 | Signing key for JWT tokens |
| `BOOTSTRAP_ADMIN_USERNAME`   | Default admin username     |
| `BOOTSTRAP_ADMIN_PASSWORD`   | Default admin password     |
| `BOOTSTRAP_ADMIN_EMAIL`      | Admin email                |

---

## 📊 Monitoring & Observability

* Actuator endpoints enabled:

  * `/actuator/health`
  * `/actuator/metrics`
  * `/actuator/info`
* Logs sent to **Splunk**
* Prometheus + Grafana integration (in progress)
* Structured logging planned (JSON w/ request tracing)

We use **Fluent Bit + Loki + Grafana** for application logging.
→ See [docs/logging-stack.md](docs/logging-stack.md) for details and justification.

---

## 🚀 Deployment (Contabo VPS)

Steps to deploy:

1. Set up VPS with Docker
2. Harden SSH & firewall (only allow ports 22, 80, 443)
3. Clone this repository
4. Copy `.env` with production secrets
5. Run:

```bash
docker compose -f docker-compose.prod.yml up -d
```

6. Use NGINX reverse proxy + Let's Encrypt for SSL termination

---

## ✅ CI/CD Pipeline

* GitHub Actions test pipeline
* Runs:

  * Unit + integration tests
  * PMD checks
  * JaCoCo reports
* Deployment integration to VPS planned

---

## 📌 Roadmap

* [x] Docker + Compose setup
* [x] Secure backend (JWT)
* [x] GitHub Actions for tests
* [x] Logging to Splunk
* [ ] Swagger/OpenAPI docs
* [ ] Auth rate limiting
* [ ] Refresh token strategy
* [ ] Prometheus + Grafana
* [ ] Production deploy via GitHub SSH

---

## ⚠️ License & Use

This project is open for personal use and inspiration.
However, **I do not offer support, contributions, or issue tracking.**

Use it, fork it, break it, learn from it — that's the goal.

---

MIT © [MRegra](https://github.com/MRegra)

Full docker clean-up:

    docker compose down -v --remove-orphans && docker system prune -af --volumes && docker compose build --no-cache && docker compose up
    docker compose down -v --remove-orphans; docker system prune -af --volumes; docker compose build --no-cache; docker compose up
    docker compose down -v --remove-orphans; docker system prune -af --volumes; docker compose build --no-cache; docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
    

