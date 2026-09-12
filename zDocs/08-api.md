# 08 — API Design

## API Principles

REST APIs should:

* Use meaningful resources.
* Use HTTP methods correctly.
* Return consistent responses.
* Validate requests.
* Return appropriate HTTP status codes.
* Protect authenticated resources.

## Authentication

```http
POST /api/v1/auth/login
POST /api/v1/auth/register
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
```

## Students

```http
GET    /api/v1/students/{id}
PUT    /api/v1/students/{id}
GET    /api/v1/students/{id}/attendance
GET    /api/v1/students/{id}/results
GET    /api/v1/students/{id}/certificates
GET    /api/v1/students/{id}/achievements
```

## Attendance

```http
POST /api/v1/attendance
GET  /api/v1/attendance
PUT  /api/v1/attendance/{id}
```

## Certificates

```http
POST   /api/v1/certificates
GET    /api/v1/certificates/{id}
GET    /api/v1/students/{id}/certificates
PUT    /api/v1/certificates/{id}
DELETE /api/v1/certificates/{id}
```

## Results

```http
POST /api/v1/results
GET  /api/v1/students/{id}/results
PUT  /api/v1/results/{id}
```

## API Documentation

The API should additionally be documented using:

```text
Swagger / OpenAPI
```

and tested through:

```text
Postman
```

## API Versioning

Use:

```text
/api/v1/
```

so future breaking changes can be introduced through:

```text
/api/v2/
```
