# 06 — High-Level Design

## 1. Initial Architecture

DERP initially follows a modular monolithic architecture.

```text
                React Frontend
                      │
                      │ HTTPS
                      ▼
              Spring Boot Backend
                      │
        ┌─────────────┼─────────────┐
        │             │             │
   Controller      Service      Security
        │             │             │
        └─────────────┼─────────────┘
                      │
                Repository
                      │
                      ▼
                    MySQL
```

## 2. Why Modular Monolith?

A modular monolith is appropriate initially because:

* Easier development
* Easier deployment
* Easier debugging
* Lower infrastructure complexity
* Clear module boundaries
* Easier transaction management

The project can evolve toward microservices only when there is a real reason.

## 3. Backend Modules

```text
backend/
└── src/
    └── main/
        └── java/
            └── derp/
                ├── auth/
                ├── student/
                ├── faculty/
                ├── attendance/
                ├── academic/
                ├── certificate/
                ├── achievement/
                ├── internship/
                ├── workshop/
                ├── fees/
                └── admin/
```

## 4. Future Architecture

If scale requires it:

```text
                  Load Balancer
                       │
                  API Gateway
                       │
       ┌───────────────┼────────────────┐
       ▼               ▼                ▼
 Authentication    Academic         Attendance
 Service           Service          Service
       │               │                │
       └───────────────┼────────────────┘
                       │
                 Message Broker
                       │
             ┌─────────┴─────────┐
             ▼                   ▼
          Notification       Analytics
```

This is a future direction, not an MVP requirement.

## 5. Important Design Principle

Do not introduce microservices only because they look impressive on a resume.

Architecture should follow actual requirements.
