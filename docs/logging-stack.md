# Logging Stack

This document explains the logging setup used in this project. It describes each tool, what it is responsible for, and why it was chosen. The goal is to make the system easy to understand for future developers, and to justify why three tools — Fluent Bit, Loki, and Grafana — are used together.

---

## Overview

Logging is a critical part of any production system. It helps us understand what is happening inside our services, debug problems, monitor performance, and detect security issues.

In this project, we use the following stack:

- **Fluent Bit** — collects logs from the application and forwards them to Loki.
- **Loki** — stores and indexes logs for querying and analysis.
- **Grafana** — provides a web interface to search, filter, and visualize logs.

Each tool has one specific job, and they are designed to work together.

---

## How the Logging Pipeline Works

1. The application writes logs to standard output (or to files).
2. **Fluent Bit** reads those logs from the file system or container output.
3. Fluent Bit forwards the logs to **Loki**.
4. **Loki** receives and stores the logs, indexing them using labels.
5. **Grafana** connects to Loki and lets us view, filter, and search logs in a browser.

This pipeline looks like this:

```bash

[App Logs]
↓
[Fluent Bit]
↓
[Loki]
↓
[Grafana]

```

---

## Why Each Tool Was Chosen

### ✅ Fluent Bit (Log Collector)

- **Purpose**: Reads logs from files or containers, adds metadata, and sends logs to Loki.
- **Reason for Use**: It is lightweight (uses very little memory), fast, and officially supported by Grafana Loki.
- **Alternatives Considered**:
  - Filebeat: good, but better integrated with Elasticsearch.
  - Vector.dev: very promising, but more complex and still maturing.
- **Conclusion**: Fluent Bit is a stable and simple choice that integrates well with Loki.

---

### ✅ Loki (Log Aggregator)

- **Purpose**: Stores logs and makes them searchable using labels and time ranges.
- **Reason for Use**: Loki is optimized for logs from microservices and containers. It is more efficient than Elasticsearch because it does not index the full log content — only the labels.
- **Alternatives Considered**:
  - Elasticsearch: powerful, but heavier, requires more memory and storage.
  - OpenSearch: similar to Elasticsearch, but more operational complexity.
- **Conclusion**: Loki is lighter and easier to maintain, which is better for projects like this.

---

### ✅ Grafana (Log Viewer)

- **Purpose**: Provides a user interface to search, filter, and explore logs stored in Loki.
- **Reason for Use**: Grafana is already a standard for dashboards and monitoring. It integrates natively with Loki, making it very easy to use.
- **Alternatives Considered**:
  - Kibana (for Elasticsearch): not compatible with Loki.
- **Conclusion**: Grafana is the best choice when using Loki.

---

## Why Three Tools?

It might seem like three tools is too much, but each tool has a very specific role:

| Tool        | Role                  |
|-------------|-----------------------|
| Fluent Bit  | Collects and ships logs |
| Loki        | Stores and indexes logs |
| Grafana     | Displays logs in UI     |

They are not doing the same job — they are each doing one part of a full logging system. This separation of concerns keeps the system modular and easier to debug or extend in the future.

---

## Local vs Production Use

- In **local development**, this stack runs using Docker Compose.
- In **production**, the same tools can be scaled and deployed using Docker, Kubernetes, or system services.
- Fluent Bit supports outputting to multiple backends if needed (e.g. Loki for monitoring and Splunk for compliance/auditing).

---

## Final Thoughts

This logging stack was chosen to:

- Be lightweight and efficient.
- Work well in both development and production environments.
- Be easy to extend or integrate with other tools in the future.
- Provide good observability with low operational cost.

All components are open-source and backed by large communities.

If the project grows and requires more features (e.g., log retention policies, alerting, or compliance storage), the setup can be extended without throwing away the current system.
