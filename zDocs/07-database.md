# 07 — Database Design

## Core Entities

```text
User
Student
Faculty
Department
Course
Subject
Enrollment
Attendance
Result
Certificate
Achievement
Internship
Workshop
Fee
Payment
```

## Simplified Relationships

```text
Department
    │
    ├── Course
    │      │
    │      └── Student
    │
    └── Faculty

Student
  │
  ├── Enrollment
  ├── Attendance
  ├── Result
  ├── Certificate
  ├── Achievement
  ├── Internship
  ├── Workshop
  └── Fee

Faculty
   │
   └── Teaching Assignment
          │
          └── Subject
```

## Example Student Entity

```text
Student
----------------
id
user_id
roll_number
name
email
course_id
department_id
semester
admission_year
status
created_at
updated_at
```

## Attendance

```text
Attendance
----------------
id
student_id
subject_id
faculty_id
date
status
created_at
updated_at
```

## Certificate

```text
Certificate
----------------
id
student_id
title
organization
category
issue_date
document_url
verification_url
created_at
updated_at
```

## Database Principles

### Normalization

Avoid unnecessary duplication.

### Foreign Keys

Use relationships to preserve integrity.

### Indexing

Frequently searched columns should be considered for indexes.

Examples:

```text
student_id
roll_number
email
subject_id
faculty_id
date
```

### Auditability

Important records should contain:

```text
created_at
updated_at
created_by
updated_by
```

where appropriate.

## ER Diagram

The final ER diagram should be maintained in:

```text
diagrams/database/
```

using diagrams.net.
