# 11 — Deployment

## Environment

The system should separate environments.

```text
Development
     ↓
Testing
     ↓
Staging
     ↓
Production
```

## Frontend

Production frontend can be deployed using a suitable cloud hosting provider.

## Backend

Spring Boot application can be packaged as:

```text
JAR
```

or deployed as a Docker container.

## Docker

Example architecture:

```text
Docker
│
├── Frontend
├── Backend
└── Supporting Services
```

## Database

Production database should be hosted separately from application containers.

## Configuration

Secrets must not be committed to Git.

Bad:

```text
application.properties

password=my-secret-password
```

Better:

```text
Environment Variables
```

Example:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
SMTP_USERNAME
SMTP_PASSWORD
```

## CI/CD

Future pipeline:

```text
Git Push
   ↓
Build
   ↓
Unit Tests
   ↓
Integration Tests
   ↓
Docker Build
   ↓
Deploy
   ↓
Health Check
```

## Rollback

Production deployment should have a rollback strategy.

```text
New Version
    ↓
Health Check
    ↓
Failure
    ↓
Rollback
```
