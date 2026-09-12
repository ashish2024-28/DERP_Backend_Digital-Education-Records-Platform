# 13 — Architecture Decisions

This document records important engineering decisions and their reasoning.

## ADR-001 — Start With Modular Monolith

### Decision

Use a modular monolithic Spring Boot backend initially.

### Why?

DERP is still evolving and does not yet require independent microservice deployment.

Advantages:

* Lower complexity
* Easier debugging
* Easier local development
* Easier transactions
* Faster development

### Future

Move selected modules into services only when there is a measurable reason.

---

## ADR-002 — React for Frontend

### Decision

Use React.js.

### Reason

The project requires:

* Dynamic dashboards
* Role-based interfaces
* Reusable components
* API integration
* Responsive UI

---

## ADR-003 — Spring Boot for Backend

### Decision

Use Java + Spring Boot.

### Reason

The project requires:

* REST APIs
* Authentication
* Authorization
* Database integration
* Scalable backend architecture

It also aligns with the project's Java backend learning and career direction.

---

## ADR-004 — MySQL

### Decision

Use MySQL as the primary relational database.

### Reason

DERP contains strongly related structured data:

```text
Students
Faculty
Courses
Subjects
Attendance
Results
Fees
Certificates
```

A relational database provides useful relationships and transactional guarantees.

---

## ADR-005 — API Versioning

### Decision

Use:

```text
/api/v1/
```

### Reason

Allows future API evolution without immediately breaking existing clients.

---

## ADR-006 — Do Not Introduce Microservices Prematurely

### Decision

Do not use microservices simply for resume value.

### Reason

Distributed systems introduce:

* Network failures
* Service discovery
* Deployment complexity
* Distributed tracing
* Data consistency challenges

Architecture should be driven by requirements.

---

## ADR-007 — Documentation as Part of Engineering

### Decision

Engineering documentation lives inside the repository.

### Reason

The documentation should evolve together with the code.

```text
Requirement changes
       ↓
Documentation changes
       ↓
Design changes
       ↓
Implementation changes
```

This prevents the documentation from becoming disconnected from the actual system.
