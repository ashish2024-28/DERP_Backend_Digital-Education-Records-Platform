# 02 — Requirements

## 1. Functional Requirements

### Student

A student should be able to:

* Register/login.
* View profile.
* View attendance.
* View attendance subject wise and last 7 days P/A.
* View semester results.
* Add/view certificates where permitted.
* View internships.
* View workshops.
* View achievements.
* View academic history.
* View fee information.
* Access relevant documents.

### Faculty

Faculty should be able to:

* Login securely.
* View assigned subjects.
* View assigned classes.
* View students.
* Mark attendance.
* Update attendance where permitted.
* View attendance history.
* Manage relevant academic information.

### Admin

Admin should be able to:

* Manage students.
* Manage faculty.
* Manage courses.
* Manage departments.
* Manage subjects.
* Manage academic records.
* Manage certificates.
* Manage attendance.
* Manage results.
* Manage fees.

### Domain Admin

Domain Admin can manage information belonging to a specific institution/domain.

### Sub Admin

Sub Admin can perform limited administrative operations based on assigned permissions.

### Fees Admin

Fees Admin manages:

* Fee structures
* Student fee records
* Payment status
* Pending fees
* Payment history


---------------------------------------------------------------------------------


### Functional Requirements
### Student

FR-01 Student can log in securely.

FR-02 Student can view their profile and academic information.

FR-03 Student can view attendance by subject/class and last 7 day P/A.

FR-04 Student can view attendance history by date/session.

FR-05 Student can view semester results.

FR-06 Student can store/view certificates.

FR-07 Student can store/view workshop records.

FR-08 Student can store/view internship records.

FR-09 Student can store/view achievements.

FR-10 Student can view fee information.

FR-11 Student can see their academic records from one centralized dashboard.

FR-12 Student can download/share relevant academic documents where authorized.

### Faculty

FR-13 Faculty can log in securely.

FR-14 Faculty can view assigned of subjects and add/change/delete.

FR-15 Faculty can view students belonging to their assigned class.

FR-16 Faculty can mark attendance for a class/session.

FR-17 Faculty can edit attendance .

FR-18 Faculty can view previously submitted attendance.

FR-19 System can prevent duplicate attendance submissions for the same class/session.

FR-20 Faculty can view relevant student academic information where permitted.

### SubAdmin / Administrator / HOD

FR-21 SubAdmin can manage students.

FR-22 SubAdmin can manage faculty.

FR-23 SubAdmin can manage classes, subjects, and academic sessions.

FR-24 SubAdmin can assign subjects/classes to faculty.

FR-25 SubAdmin can view attendance records.

FR-26 SubAdmin can correct/manage attendance records according to permissions.

FR-27 HOD/SubAdmin can monitor attendance across classes.

FR-28 SubAdmin can manage student academic records.

FR-29 SubAdmin can manage certificate/workshop/internship/achievement records where applicable.

FR-30 SubAdmin can manage .

### Attendance

This deserves its own requirements because it is one of your major real-world problems.

FR-31 System stores attendance against a specific studentId, facultyId, subAdminId, StudentRollNo, subject, section(YearSection->3C), dateTime, period .

FR-32 System identifies whether attendance has already been submitted for a particular session.

FR-33 System prevents unauthorized modification of attendance.

FR-34 System records when attendance was created or modified.

FR-35 System allows authorized users to retrieve attendance history.

### Centralized Academic Records

This is the part that makes DERP more than an attendance CRUD project.

FR-36 System provides a centralized academic record for each student.

FR-37 Student can associate certificates with relevant details such as title, organization, date, and category.

FR-38 Student can maintain workshop records.

FR-39 Student can maintain internship records.

FR-40 Student can maintain achievement records.

FR-41 Student can maintain academic results.

FR-42 System organizes records by category instead of keeping them scattered.




---------------------------------------------------------------------------------------------------------------------------------


### Non-Functional Requirements

Now ask: How well should DERP work?

NFR-01 — Security

Only authenticated and authorized users should access or modify protected data.

NFR-02 — Role-Based Access

Students, faculty, HODs/SubAdmins, FeesAdmin , DomainAdmins, and other roles should have different permissions.

NFR-03 — Performance

Common operations such as opening dashboards, viewing attendance, and retrieving records should respond quickly under normal load.

NFR-04 — Reliability

Submitted attendance and academic records should not be accidentally lost.

NFR-05 — Data Integrity

The system should prevent inconsistent records such as duplicate attendance submissions or invalid student-subject relationships.

NFR-06 — Auditability

Important operations such as attendance modification should be traceable.

NFR-07 — Maintainability

Backend, frontend, database, and business logic should be modular so that new modules can be added without rewriting the entire system.

NFR-08 — Scalability

The system should support multiple:

Universities/domains
Classes
Departments
Students
Faculty
Academic sessions
NFR-09 — Availability

The system should remain accessible during normal college operations without frequent downtime.

NFR-10 — Usability

Students and faculty should be able to complete common tasks without unnecessary steps.



------------------------------------------------------------------------------------------





## 2. Non-Functional Requirements

### Security

The system should provide:

* Authentication
* Authorization
* Role-based access control
* Secure password handling
* JWT-based authentication
* OAuth2 where applicable
* Input validation
* Secure API design

### Performance

The system should:

* Minimize unnecessary API calls.
* Use pagination for large datasets.
* Avoid duplicate database queries.
* Return predictable API responses.
* Support optimized attendance operations.

### Scalability

The architecture should allow future modules to be added without rewriting the entire application.

### Reliability

Important operations should:

* Validate data.
* Handle failures.
* Return useful errors.
* Prevent accidental duplicate operations.

### Maintainability

The codebase should follow:

* Separation of concerns
* Layered architecture
* Consistent naming
* Reusable components
* Documentation
* Automated tests

## 3. User Roles

```text
Student
Faculty
Admin
Domain Admin
Sub Admin
Fees Admin
```

## 4. Core Modules

```text
Authentication
Student
Faculty
Attendance
Academic
Certificates
Achievements
Internship
Workshop
Fees
Administration
Notifications
```
