# DERP Backend

This repository contains the Spring Boot REST API for **DERP (Digital Education Records Platform)**. It supports university-scoped accounts, role-based access, student and faculty records, attendance, certifications, notes, and fee-related operations.

The React application is in the [DERP frontend repository](https://github.com/ashish2024-28/DERP_Frontend-Digital-Education-Records-Platform-).

## What the API does

- Lists and registers universities with a domain administrator.
- Authenticates users and issues JWTs.
- Manages student, faculty, sub-admin, fees-admin, and domain-admin profiles.
- Provides domain-admin operations for user and fee-course management.
- Lets faculty mark attendance; students and authorized staff can read attendance views.
- Provides student certification upload/list/search/download/delete and user-notepad endpoints.
- Provides fee-course, fee-account, and payment-method/payment-status APIs.

Student and fees-admin fee workflows are exposed in the frontend. Payment submissions remain pending until fees staff verifies the recorded transfer; cash receipts are recorded as verified immediately.

## Backend architecture

```text
HTTP request
    │
Spring Security / JWT / role and domain checks
    │
Controller → Service → Spring Data repository → relational database
```

- **Controllers** map HTTP methods and paths and validate access.
- **Services** implement account, attendance, university, fee, and profile workflows.
- **Repositories and JPA entities** read and persist relational data.
- **DTOs** define request and response shapes for the API.

User roles include `ADMIN`, `DOMAIN_ADMIN`, `SUB_ADMIN`, `FEES_ADMIN`, `FACULTY`, and `STUDENT`. Many university endpoints also check that the requested domain matches the authenticated user.

## Technology

- Java and Spring Boot
- Spring Web MVC, Spring Security, and JWT (JJWT)
- Spring Data JPA / Hibernate
- PostgreSQL JDBC driver is currently declared in Maven
- OpenAPI/Swagger, ModelMapper, Redis/cache dependencies, Apache POI for Excel
- Maven Wrapper

## Requirements and configuration

- A JDK compatible with the Maven compiler configuration
- A configured relational database
- Environment configuration for the database, JWT signing secret, frontend CORS origin, server context path, Swagger UI path, and the platform OTP-provider URL (`DERP_OTP_API_BASE_URL`, which must match the frontend OTP service base URL)

The repository's configuration currently needs alignment before a clean local setup:

- `pom.xml` declares Java 21 but configures compiler source/target 25.
- The PostgreSQL driver is declared while production properties specify a MySQL dialect; select one database and align driver, JDBC URL, and Hibernate dialect.
- Configure secrets locally and never commit populated `.env` files or production credentials. The example file contains credential-like values; do not reuse them.

## Run locally

From this repository, after resolving the JDK/database configuration and setting the required environment variables:

```powershell
.\mvnw.cmd spring-boot:run
```

To package the application:

```powershell
.\mvnw.cmd clean package
```

The server uses the configured port (with 8080 as the default) and may use a servlet context path. Swagger/OpenAPI routes are configurable; check `SWAGGER_URL` and the running application.

## Main API groups

| API group | Example paths | Purpose |
|---|---|---|
| Public onboarding | `/home_page/**` | List, validate, and register a university |
| Login and signup | `/{domain}/login_profile/**`, `/{domain}/signup/**` | Authenticate and create supported account roles |
| Role accounts | `/{domain}/student/**`, `/{domain}/faculty/**`, `/{domain}/subAdmin/**`, `/{domain}/feesAdmin/**`, `/{domain}/domainAdmin/**` | Profile and role-specific operations |
| Attendance | `/{domain}/erp/attendance/**` | Mark attendance, read summaries, manage retention |
| Fees | `/{domain}/domainAdmin/*fees-course-wise`, `/{domain}/fees-admin/**`, `/{domain}/fees-student/**` | Configure course fees, process payment records, read fee accounts |
| Student certifications | `/{domain}/student/certifications/**` | Manage certificates with owner/domain-scoped reads, downloads, and deletes |
| Notepad | `/{domain}/{role}/notepad/**` | Manage a user's notes |
| Global administration | `/admin/**` | Manage universities and domain administrators |

Paths preserve legacy case and spelling (for example `subAdmin`, `feesAdmin`, and `verify-faill`). Refer to controller annotations or the running OpenAPI documentation for exact methods, DTOs, and current access requirements.

## Important implementation notes

- JWT authentication is stateless; passwords are intended to use BCrypt.
- Domain and role authorization must be enforced by the backend; frontend navigation is not a security control.
- Platform-admin university deletion requires a backend-verified OTP, a typed domain confirmation, a reason, and creates a deletion audit entry. Configure `DERP_OTP_API_BASE_URL` to the same trusted OTP service used by `VITE_API_OTP_URL`; deletion remains unavailable when the backend provider is not configured.
- Certificate files are written outside public static uploads and downloaded through an authenticated student/domain-scoped endpoint; legacy certificate paths are also blocked from public static access.
- New student fee submissions have no verifying fees-admin until they are reviewed. On databases created before this workflow, apply the matching [`PostgreSQL`](./src/main/resources/db/manual/allow-pending-fee-payments-postgresql.sql) or [`MySQL`](./src/main/resources/db/manual/allow-pending-fee-payments-mysql.sql) script once before using student payment submission; Hibernate schema update does not reliably relax an existing `NOT NULL` constraint.
- Attendance has role-specific views, audit logging, semester-aware retention data, and scheduled cleanup behavior.
- Some profile/account operations still use legacy DTOs as their `data` payload.
- Automated tests and a complete production deployment guide were not established during the documentation pass; validate locally and add tests before relying on production behavior.

## License

No license has been specified yet. Please contact the project owner before redistribution.
