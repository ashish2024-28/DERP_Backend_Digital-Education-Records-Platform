# 09 — Low-Level Design

## Backend Layering

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

## Controller

Responsibilities:

* Receive HTTP request.
* Validate request format.
* Call service.
* Return response.

Controller should not contain business logic.

## Service

Responsibilities:

* Business rules
* Transactions
* Validation involving business logic
* Coordination between repositories

Example:

```text
AttendanceService
    |
    ├── validateFacultyAccess()
    ├── validateSubject()
    ├── validateStudents()
    ├── preventDuplicateAttendance()
    └── saveAttendance()
```

## Repository

Responsibilities:

* Database access
* Queries
* Persistence

## DTO

Use DTOs rather than exposing internal database entities directly.

Example:

```text
AttendanceRequest
AttendanceResponse
StudentResponse
CertificateRequest
CertificateResponse
```

## Exception Handling

Centralized exception handling should provide predictable responses.

Example:

```json
{
  "success": false,
  "message": "Attendance already submitted",
  "timestamp": "...",
  "path": "/api/v1/attendance"
}
```

## Security

Security should be handled independently from business logic.

```text
Request
   ↓
Authentication
   ↓
Authorization
   ↓
Controller
   ↓
Service
```

## Transactions

Operations that must succeed or fail together should use transactions.

Example:

```text
Submit Attendance
       ↓
Validate
       ↓
Save Records
       ↓
Update Metadata
       ↓
Commit
```

If a critical step fails:

```text
Rollback
```
