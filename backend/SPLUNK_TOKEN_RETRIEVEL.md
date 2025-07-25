Great question — here’s how you can get the **Splunk HTTP Event Collector (HEC) Token**, step by step:

---

## 🧭 How to Get a Splunk HEC Token (Local Instance)

> 💡 This assumes you have Splunk running locally (via Docker or a VM) and accessible at `http://localhost:8000`.

### 🔐 Step-by-Step to Get Your Splunk Token:

1. **Login to the Splunk Web UI**

    * URL: [http://localhost:8000](http://localhost:8000)
    * Default login:

        * **Username:** `admin`
        * **Password:** The one you set when starting the container or VM (e.g., `changeme123`)

2. **Enable HEC (if not already enabled)**

    * Go to: `Settings` > `Data Inputs`
    * Click on **"HTTP Event Collector"**
    * If it's **disabled**, click **"Global Settings"** (top right)

        * Set **All Tokens Enabled**: ✅ Enabled
        * Set **Enable SSL**: Optional (✅ Enabled or off for local dev)
        * Click **Save**

3. **Create a New Token**

    * In the **HTTP Event Collector** page, click **"New Token"**
    * Step-by-step:

        * **Name:** `spring-boot-logs`
        * **Source type:** Leave default (`_json` is fine)
        * **Index:** Use `main` or create a new one (e.g., `spring`)
        * Optionally set app name/user
    * Click **Next**, then **Submit**

4. **Copy the Token**

    * After submitting, Splunk will display:

      ```
      Token Value:
      DF0FDC3D-B68C-4C89-AD1F-5BB4E7798659
      ```
    * ⚠️ **Copy and save it** — you won't see it again.

5. **Verify URL and Port**

    * The HEC endpoint is typically:

      ```
      http://localhost:8088/services/collector
      ```

      or

      ```
      https://localhost:8088/services/collector
      ```

---

## 🧪 Test That the Token Works

Optional: use `curl` to test your token:

```bash
curl -k https://localhost:8088/services/collector \
  -H "Authorization: Splunk DF0FDC3D-B68C-4C89-AD1F-5BB4E7798659" \
  -d '{"event": "Hello from curl!"}'
```

If working, Splunk returns:

```json
{"text":"Success","code":0}
```

---

Let me know once you have the token, and I’ll help you:

* ✅ Add it to your `logback-spring.xml`
* 🔧 Set up a secure integration
* 📊 Create a basic search or dashboard in Splunk

Want me to generate the updated XML config with that token now?
