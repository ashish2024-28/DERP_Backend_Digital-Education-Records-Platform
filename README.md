# Attendance Management System — Design & Bug Report

## Bugs Fixed

| File | Bug | Fix |
|------|-----|-----|
| `Student.java` | `private AttendanceRecord attendanceRecord` — wrong type for `@OneToMany` | Changed to `List<AttendanceRecord> attendanceRecords` |
| `Student.java` | `private FeesPayment feesPayment` — same wrong type | Changed to `List<FeesPayment> feesPayments` |
| `AttendanceRecord.java` | Had NO reference to `AttendanceSession` — records were orphaned with no parent | Added `@ManyToOne private AttendanceSession session` |
| `AttendanceSession.java` | No `@OneToMany records` list — no way to navigate from session → its records | Added `@OneToMany(mappedBy="session") List<AttendanceRecord> records` |
| `AttendanceRecord.java` | Unique constraint referenced `session_id` column but session relation didn't exist | Fixed — now properly references `session_id` FK |
| `FacultyService.getFacultyByGmail` | Called `frepo.save(faculty)` on a read — unnecessary write on fetch | Should just `return faculty` without saving |
| `BaseUserService.existsUserByEmail` | `universityRepo.existsByEmail` called twice (once at top, once at bottom) | Remove duplicate call |

---

## Architecture

```
Faculty creates AttendanceSession
         │
         ▼
  ┌──────────────────────────────────────────────┐
  │  AttendanceSession                           │
  │  ─────────────────                          │
  │  id, domain, course, branch, batch, section │
  │  subject, sessionDate, locked               │
  │  faculty ──► Faculty                        │
  │  university ──► University                  │
  └──────────────────────────────────────────────┘
         │  OneToMany
         ▼
  ┌──────────────────────────────────────────────┐
  │  AttendanceRecord                            │
  │  ────────────────                            │
  │  id                                          │
  │  session ──► AttendanceSession (ManyToOne)   │
  │  student ──► Student (ManyToOne)             │
  │  status: PRESENT | ABSENT | LEAVE            │
  │  markedAt, updatedAt                         │
  └──────────────────────────────────────────────┘
```

---

## Matching Logic: Faculty → Students

```
Faculty.course         == Session.course
Faculty.teachingBatch  contains  Session.batch

Session.course  == Student.course
Session.branch  == Student.branch
Session.batch   == Student.batch
Student.yearWithSection  contains  Session.section
```

### Example
| Field | Faculty | Session | Student |
|-------|---------|---------|---------|
| course | `B.Tech` | `B.Tech` | `B.Tech` |
| teachingBatch | `2021-2025,2022-2026` | — | — |
| batch | — | `2021-2025` | `2021-2025` |
| branch | — | `CSE` | `CSE` |
| section | — | `A` | yearWithSection = `"3A"` ✓ |

---

## API Reference

### Faculty

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/attendance/session/create?domain=HU` | Create session → returns eligible students |
| `POST` | `/api/attendance/session/mark?domain=HU` | Submit marks (set `lock:true` to finalize) |
| `GET`  | `/api/attendance/session/{id}?domain=HU` | Fetch session with current marks |
| `GET`  | `/api/attendance/session/history?domain=HU` | All sessions by this faculty |

### Student

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET`  | `/api/attendance/student/dashboard?domain=HU` | Full dashboard: overall %, per-subject %, recent records with faculty name |

---

## Faculty Request Examples

### Create Session
```json
POST /api/attendance/session/create?domain=HU
{
  "course": "B.Tech",
  "branch": "CSE",
  "batch": "2021-2025",
  "section": "A",
  "subject": "Data Structures",
  "sessionDate": "2026-03-19"
}
```

### Mark Attendance
```json
POST /api/attendance/session/mark?domain=HU
{
  "sessionId": 42,
  "lock": true,
  "entries": [
    { "studentId": 101, "status": "PRESENT" },
    { "studentId": 102, "status": "ABSENT"  },
    { "studentId": 103, "status": "LEAVE"   }
  ]
}
```

---

## Student Dashboard Response

```json
{
  "studentName": "Rahul Sharma",
  "rollNumber": "21CSE001",
  "overallPercentage": 78.5,
  "subjectSummaries": [
    {
      "subject": "Data Structures",
      "totalClasses": 40,
      "presentCount": 35,
      "absentCount": 4,
      "leaveCount": 1,
      "attendancePercentage": 87.5,
      "status": "SAFE"
    },
    {
      "subject": "DBMS",
      "totalClasses": 30,
      "presentCount": 18,
      "absentCount": 12,
      "leaveCount": 0,
      "attendancePercentage": 60.0,
      "status": "WARNING"
    }
  ],
  "recentRecords": [
    {
      "sessionDate": "2026-03-19",
      "subject": "Data Structures",
      "status": "PRESENT",
      "facultyName": "Dr. Anjali Singh",
      "facultyId": "FAC-CSE-001",
      "facultyEmail": "anjali.singh@hu.ac.in"
    }
  ]
}
```

---

## Notes

- **Locking**: Once `lock: true` is sent, no further edits are allowed for that session. Faculty must be sure before locking.
- **Idempotency**: Creating a session that already exists returns the existing one — safe to retry.
- **Security**: Every service method cross-checks `domain` and `email` from JWT against the database record. A faculty from University A cannot touch University B's sessions.
- **yearWithSection format**: Store as `"3A"` meaning Year 3, Section A. The LIKE query `%A%` matches it. Keep section values short (single letter) to avoid false matches.
