# 04 — User Flow

## Student Login

```text
Open DERP
   ↓
Login
   ↓
Authentication
   ↓
Role Detection
   ↓
Student Dashboard
```

## Student Dashboard

```text
Dashboard
│
├── Profile
├── Attendance
├── Results
├── Certificates
├── Achievements
├── Internship
├── Workshops
└── Fees
```

## Faculty Attendance Flow

```text
Faculty Login
     ↓
Faculty Dashboard
     ↓
Select Subject
     ↓
Select Class
     ↓
Select Date
     ↓
Load Students
     ↓
Mark Attendance
     ↓
Validate
     ↓
Submit
     ↓
Server Validation
     ↓
Database Transaction
     ↓
Success
```

## Certificate Flow

```text
Student
   ↓
Add Certificate
   ↓
Enter Metadata
   ↓
Upload / Store Document Reference
   ↓
Validation
   ↓
Save
   ↓
Certificate Appears in Profile
```

## Resume Flow — Future

```text
Student Records
      ↓
Certificates
      +
Projects
      +
Internships
      +
Achievements
      +
Results
      ↓
Resume Builder
      ↓
Select Sections
      ↓
Generate Resume
```
