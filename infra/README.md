### ✅ Final Deployment Instructions (All-in-One)

#### 1. **Set up environment**

Make sure your `.env` file is in the correct place:

```bash
cp infra/env/.env.example infra/env/.env
```

> 🔁 The `.env` file is now located at `infra/env/.env`

---

#### 2. **Start backend, DB, Redis, Grafana stack**

```bash
docker compose --profile local up -d
```

This launches:

* Spring Boot backend (builds from `backend/Dockerfile`)
* PostgreSQL
* Redis
* Fluent Bit → Loki → Grafana logging stack

---

#### 3. **(Optional) Run NGINX in front of backend**

```bash
docker compose --profile nginx up -d
```

This uses:

* `infra/nginx/Dockerfile`
* Reverse proxies requests to the backend
* Exposes port **80**

---

#### 4. **(Optional) Start Watchtower for auto-updates**

```bash
docker compose --profile watchtower up -d
```

Make sure:

* You have `${DISCORD_STREAKER_WEBHOOK}` set in `infra/env/.env`
* You are logged in to GHCR (GitHub Container Registry):

```bash
echo $CR_PAT | docker login ghcr.io -u your-username --password-stdin
```

---

#### 5. **Access Services**

| Service | URL                                            |
| ------- | ---------------------------------------------- |
| Backend | [http://localhost:8080](http://localhost:8080) |
| Grafana | [http://localhost:3000](http://localhost:3000) |
| Loki    | [http://localhost:3100](http://localhost:3100) |
| NGINX   | [http://localhost](http://localhost)           |

---

#### 6. **Logging**

* Backend logs are written to the `shared-logs` volume.
* Fluent Bit reads logs from that volume and forwards them to Loki.
* Grafana dashboards (configured in `infra/logging/grafana/provisioning/`) visualize logs in real time.

---

#### 7. **Deploy to VPS**

On your VPS:

1. Copy the following directories:

   ```
   infra/
   backend/
   frontend/
   docker-compose.yml
   ```

2. Then run:

```bash
docker compose --profile local up -d
docker compose --profile nginx up -d
docker compose --profile watchtower up -d
```