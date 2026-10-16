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

## Fees Accounts

All endpoints require authentication and a domain-scoped role. Fees Admin write operations are restricted to the signed-in Fees Admin. Domain Admin oversight endpoints are read-only.

```http
GET  /{domain}/fees-admin/all-students
GET  /{domain}/fees-admin/fees-course-wise

GET  /{domain}/fees-admin/payment-methods
POST /{domain}/fees-admin/payment-method/bank
POST /{domain}/fees-admin/payment-method/online (multipart: `paymentOnlineDTO` JSON part, optional `qrImage` PNG/JPEG file up to 5 MB)
PUT  /{domain}/fees-admin/payment-method/{id}/active?active={boolean}

GET  /{domain}/fees-admin/payments/pending
GET  /{domain}/fees-admin/payments/recorded/count
POST /{domain}/fees-admin/payment/cash
POST /{domain}/fees-admin/online-payment/verify-success
POST /{domain}/fees-admin/online-payment/verify-fail

GET  /{domain}/fees-admin/admin/payment-methods
GET  /{domain}/fees-admin/admin/payments
GET  /{domain}/fees-admin/activity-logs
```

Successful Fees Admin changes to payment methods and receipts/reviews are recorded in the shared `ERPActionAudit` table. The activity endpoint returns all Fees Admin audit actions within the caller's domain.

## Fees Admin Profile

Profile and account operations use the authenticated Fees Admin identity; a client-supplied email is not used to choose the account.

```http
GET    /{domain}/feesAdmin
PUT    /{domain}/feesAdmin/update_profile
PUT    /{domain}/feesAdmin/forgot_update_password
DELETE /{domain}/feesAdmin/delete_account
```

The profile update body accepts `name` and `mobileNumber`. The password update body is JSON: `{"newPassword":"..."}`.

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
