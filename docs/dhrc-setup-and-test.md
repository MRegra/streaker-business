## 🔁 PART 1: Tagging and Pushing Images to GHCR (Locally)

### ✅ 1. Authenticate with GitHub from Docker (first time only)

Run this once:

```bash
echo $GITHUB_TOKEN | docker login ghcr.io -u mregra --password-stdin
```

Use a [GitHub PAT (Personal Access Token)](https://github.com/settings/tokens) with **`write:packages`** scope.

---

### ✅ 2. Build and push an image

```bash
VERSION=0.1.0

docker build -t ghcr.io/mregra/streaker-backend:$VERSION .
docker push ghcr.io/mregra/streaker-backend:$VERSION
```

Also push `:latest` if you want Watchtower to track the `latest` tag:

```bash
docker tag ghcr.io/mregra/streaker-backend:$VERSION ghcr.io/mregra/streaker-backend:latest
docker push ghcr.io/mregra/streaker-backend:latest
```

---

## 🔎 PART 2: List All Tags on GHCR Image

> GHCR **does not** currently support listing tags via Docker CLI directly.
> But you can use the GitHub API or GitHub UI.

### 🔧 Option 1: GitHub UI

Visit:
👉 [https://github.com/mregra/streaker/pkgs/container/streaker-backend](https://github.com/mregra/streaker/pkgs/container/streaker-backend)

You’ll see all image tags, last updated, etc.

---

### 🔧 Option 2: GitHub API

Run this:

```bash
curl -s -H "Authorization: Bearer YOUR_PAT" \
  https://ghcr.io/v2/mregra/streaker-backend/tags/list
```

Replace `YOUR_PAT` with your GitHub Personal Access Token (same one you used to push).

---

## 🔄 PART 3: Setup Watchtower for Auto-Redeploy

Add this to your `docker-compose.yml` on the VPS:

```yaml
watchtower:
  image: containrrr/watchtower
  container_name: watchtower
  restart: unless-stopped
  volumes:
    - /var/run/docker.sock:/var/run/docker.sock
  command: --interval 30
  environment:
    - WATCHTOWER_CLEANUP=true
    - WATCHTOWER_POLL_INTERVAL=30
    - WATCHTOWER_INCLUDE_RESTARTING=true
```

Make sure your backend service **uses the `:latest` tag** (or a pinned version):

```yaml
backend:
  image: ghcr.io/mregra/streaker-backend:latest
  restart: unless-stopped
  etc: ...
```

---

## 🧪 PART 4: Test Deployment Flow

### 🧪 Local Test Flow

1. Build new version:

   ```bash
   VERSION=0.1.1
   docker build -t ghcr.io/mregra/streaker-backend:$VERSION .
   docker push ghcr.io/mregra/streaker-backend:$VERSION

   docker tag ghcr.io/mregra/streaker-backend:$VERSION ghcr.io/mregra/streaker-backend:latest
   docker push ghcr.io/mregra/streaker-backend:latest
   ```

2. Wait 30–60 seconds (Watchtower interval)

3. Check Watchtower logs:

   ```bash
   docker logs -f watchtower
   ```

4. Check backend version:

   ```bash
   docker ps | grep streaker-backend
   ```

5. Call `/actuator/info` or `/actuator/health` to verify the new backend is working.
