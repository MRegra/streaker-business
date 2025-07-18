# Streaker – Gamified Task Tracker with Full DevOps Environment

**Streaker** is a self-hosted, real-world productivity tracker where each task earns you XP. It’s designed not just as a personal tool—but as a showcase of a full-stack DevOps production-grade environment, built from the ground up.

> ✨ Think of it as a portfolio project disguised as a daily life optimizer.

---

## 📦 Tech Stack

### 👨‍💻 Frontend
- Angular
- Tailwind CSS

### 🔧 Backend
- Java (Spring Boot)

### 🧠 Database
- PostgreSQL

### ☁️ DevOps & Infra
- GitLab CI/CD
- Docker + Docker Compose
- NGINX + Let’s Encrypt (HTTPS)
- Prometheus + Grafana (Monitoring)
- Loki + Promtail (Logging)
- Error alerting (Telegram bot or Sentry)
- Reverse proxy & health checks
- Deployment on personal server (Linux-based)

---

## 🎯 Core Features

- ✅ Create, categorize, and score tasks
- 📅 Daily view with streak & progress tracking
- 📊 Analytics dashboard (weekly score, completion rate)
- 🔐 User authentication
- 📈 Live metrics, logs, and alerts for full observability
- 🧰 Real CI/CD with rollback + infra as code

---

## 🗂️ Project Structure (MVP Plan)

### 🔧 Infrastructure Setup
- [X] Setup GitLab repository
- [ ] Setup Linux server with Docker
- [ ] NGINX reverse proxy + HTTPS (Let’s Encrypt)
- [ ] PostgreSQL container
- [ ] Grafana + Prometheus setup
- [ ] Loki + Promtail logging
- [ ] CI/CD pipelines for FE and BE
- [ ] Alerts for errors & outages

### 🧠 Backend (Java Spring Boot or Python FastAPI)
- [ ] Project skeleton setup
- [ ] Task model: title, desc, points, date, category, done
- [ ] Auth (token-based or session)
- [ ] Task API endpoints (CRUD + analytics)
- [ ] API validation + error handling
- [ ] Dockerfile + health check
- [ ] Unit & integration tests
- [ ] API docs (Swagger/OpenAPI)

### 💻 Frontend (Angular + Tailwind)
- [ ] Angular project setup
- [ ] Auth flow (login/register)
- [ ] Task dashboard (list, filter, daily check)
- [ ] Task creation form
- [ ] Analytics/stats UI
- [ ] Responsive layout
- [ ] Dockerfile

### 🔐 DevSecOps & Observability
- [ ] Secrets management (.env or GitLab)
- [ ] HTTPS enabled
- [ ] Rate limiting & secure headers
- [ ] CVE scanner (e.g. Trivy or Grype)
- [ ] Prometheus alerts
- [ ] Central logging with Loki
- [ ] Telegram/Sentry alerts

### 📚 Documentation & Content
- [ ] Full README with setup & architecture
- [ ] Swagger API docs
- [ ] System architecture diagram (PNG + source)
- [ ] Medium/LinkedIn writeup
- [ ] YouTube video (10–20 min walkthrough)
- [ ] 3–5 shorts (pipelines, logging, errors, etc.)

---

## 📆 MVP Timeline (8 Weeks)

| Week | Focus Area |
|------|------------|
| 1 | Infra setup + backend skeleton |
| 2 | API + DB + testing |
| 3 | Angular base + login + task UI |
| 4 | FE/BE integration + CI/CD |
| 5 | Logging + monitoring + alerts |
| 6 | Security + docs |
| 7 | Content creation (video, posts) |
| 8 | Polish, share, reflect |

---

## 📈 Gamified Progress (XP System)

Every task grants XP (easy: 10 XP, medium: 25 XP, hard: 50 XP).
You’ll track:
- 🧠 Weekly XP total
- 🔥 Daily streaks
- 💣 Burnout risk score
- 🏆 Unlockable badges: “Pipeline Wizard”, “Incident Commander”, etc.

_Not tracked here — see the [Notion Tracker](#) for full progress._

---

## 🧪 Local Dev Setup (Coming Soon)

```bash
git clone https://gitlab.com/your-user/streaker.git
cd streaker
docker-compose up -d
# FE → localhost:4200 | BE → localhost:8080 | DB → localhost:5432
```

---

🤝 Contributing
This project is personal, but built with open-source quality and habits:

Use feature branches

Commit with clear messages

Follow code standards and write tests

---

🛡️ License
MIT (or your preferred license)

---

👋 Author
Marcelo (TBD)

Building tools, workflows, and systems that compound.

