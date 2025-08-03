# 🧠 Git Branching Strategy

We follow a **Trunk-Based Development** strategy with a few lightweight conventions:

* ✅ `main` is the production branch.
* ✅ `staging` is used to auto-deploy for testing — it must always match `main`.
* ✅ Short-lived `feature/` branches are used for all development.
* ✅ `hotfix/` branches are used for urgent production fixes.

---

## 🌲 Main Branches

| Branch    | Purpose                                  | Deployment | Rules                                              |
| --------- | ---------------------------------------- | ---------- | -------------------------------------------------- |
| `main`    | Production-ready code                    | Manual     | - Protected<br>- Only updated via PR               |
| `staging` | Always mirrors `main` for testing deploy | Automatic  | - Must match `main` exactly<br>- No direct commits |

---

## 🌱 Feature Branches

* Created from `main`
* Named like: `feature/add-login`, `feature/refactor-api`
* Merged via PR (squash + delete branch after merge)

```bash
git checkout -b feature/your-feature-name main
```

---

## 🛠️ Hotfix Branches

Used for quick emergency fixes to `main`:

1. Branch from `main`
2. Push fix
3. Open PR back to `main`
4. After merge, also re-sync `staging`

```bash
git checkout -b hotfix/fix-critical-bug main
```

---

## 🔁 Workflow Summary

```mermaid
graph TD
    A[feature/*] --> B[Pull Request]
    B --> C[main]
    C -->|Tag & Deploy to Production| D[Prod Server]
    C --> E[staging (sync)]
    E -->|Auto Deploy| F[Test Server]
    G[hotfix/*] --> C
```

---

## 📜 Branch Naming Conventions

| Type    | Prefix     | Example                |
| ------- | ---------- | ---------------------- |
| Feature | `feature/` | `feature/add-api-auth` |
| Hotfix  | `hotfix/`  | `hotfix/fix-crash-500` |

---

## 🔐 Branch Protection Rules

* **`main`**

  * PRs required
  * CI must pass
  * Squash merges only

* **`staging`**

  * Only updated via GitHub Actions sync from `main`
  * No direct commits or PRs