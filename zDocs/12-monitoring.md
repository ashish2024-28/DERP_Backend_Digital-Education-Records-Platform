# 12 — Monitoring

A production system should not stop at deployment.

We need to know:

> Is the system working?

## Important Metrics

### Application

* Request count
* Response time
* Error rate
* HTTP status distribution

### Database

* Connection count
* Query performance
* Slow queries
* CPU/memory usage

### Infrastructure

* CPU
* RAM
* Disk
* Network

## Logs

Important events should be logged.

Examples:

```text
User login
Attendance submission
Authentication failure
Database failure
API exception
Certificate operation
```

Logs should not expose sensitive information.

## Health Checks

Example:

```http
GET /actuator/health
```

Possible result:

```json
{
  "status": "UP"
}
```

## Alerts

Potential alerts:

```text
Error rate high
        ↓
Alert

Database unavailable
        ↓
Alert

Response time high
        ↓
Alert
```

## Future Observability

Possible tools:

* Spring Boot Actuator
* Metrics
* Centralized logging
* Prometheus
* Grafana
* Error tracking

These should be introduced based on actual project needs.
