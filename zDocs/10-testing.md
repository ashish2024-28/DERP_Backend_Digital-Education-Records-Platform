# 10 — Testing Strategy

Testing should happen at multiple levels.

## 1. Unit Testing

Test individual business components.

Examples:

```text
AttendanceService
CertificateService
FeeService
StudentService
```

## 2. Integration Testing

Test:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

## 3. API Testing

Use Postman and automated API tests.

Test:

* Success
* Validation failure
* Unauthorized request
* Forbidden request
* Not found
* Duplicate request
* Server failure

## 4. Frontend Testing

Test:

* Components
* Forms
* Validation
* API states
* Loading states
* Error states

## 5. Important Attendance Test Cases

### Valid attendance

```text
Faculty authorized
+
Valid subject
+
Valid students
+
Valid date
=
Attendance saved
```

### Duplicate attendance

```text
Existing attendance
+
Same subject
+
Same date
=
Reject / handle duplicate
```

### Unauthorized faculty

```text
Faculty not assigned to subject
=
Forbidden
```

## 6. Testing Pyramid

```text
          E2E
         /   \
      Integration
       /       \
     Unit Tests
```

Most tests should be fast unit tests, followed by integration tests and a smaller number of end-to-end tests.

