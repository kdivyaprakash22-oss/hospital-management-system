# Hospital Management System (JSP + Servlets + PostgreSQL)

A repaired, deployable starter based on the original legacy JSP project. It provides a dashboard and database-backed record forms for patients, departments, doctors, nurses, rooms, medicines, admissions, bills and attendance.

> Educational demonstration only. Do not use real patient information or deploy as a clinical production system. Before real use, add robust role-based access control, individual accounts, secure password handling, audit logging, backups, monitoring, rate limiting and a professional security review. The demo now includes CSRF tokens, HTML output escaping and baseline browser security headers, but it is still not approved for real patient data.

## Stack
- Java 17
- JSP + Java Servlet API 4 (`javax.servlet`)
- Apache Tomcat 9
- PostgreSQL + JDBC
- Maven WAR build
- Docker deployment and GitHub Actions CI

## 1. Requirements
Install JDK 17, Maven 3.9+, Git, and PostgreSQL (or create a hosted PostgreSQL database).

## 2. Create database
Create a database named `hospital`, then run `database/schema.sql`. Optional demo data: run `database/sample-data.sql`. All records are fictional.

## 3. Configure local environment (PowerShell)
Set the variables in the terminal you will use to run Docker/Tomcat:
```powershell
$env:DB_URL='jdbc:postgresql://localhost:5432/hospital'
$env:DB_USERNAME='hospital_user'
$env:DB_PASSWORD='YOUR_DATABASE_PASSWORD'
$env:APP_USERNAME='admin'
$env:APP_PASSWORD='Use-A-Long-Unique-Password-Here'
```
The `APP_PASSWORD` value is a demo gate stored in the hosting environment, not a production-grade identity system. Do not commit secrets.

## 4. Build
```powershell
mvn clean verify
```
Expected artifact: `target/ROOT.war`.

## 5. Run with Docker
Build image:
```powershell
docker build -t hospital-management-system .
```
Run it (map host port 8080):
```powershell
docker run --rm -p 8080:8080 -e DB_URL="$env:DB_URL" -e DB_USERNAME="$env:DB_USERNAME" -e DB_PASSWORD="$env:DB_PASSWORD" -e APP_USERNAME="$env:APP_USERNAME" -e APP_PASSWORD="$env:APP_PASSWORD" hospital-management-system
```
Open http://localhost:8080 and sign in with the configured app credentials.

## 6. Deploy free-tier demo
1. Push this repository to GitHub.
2. Create a PostgreSQL database with a free-tier provider such as Neon. Run the SQL files in order.
3. Create a Render Web Service from the GitHub repo, choose Docker, and select the free instance type if available.
4. In Render environment settings set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `APP_USERNAME`, `APP_PASSWORD`. Convert a provider URL to JDBC format if needed (`jdbc:postgresql://host/database?...`). Follow the database provider's SSL instructions.
5. Deploy, inspect logs, then visit the assigned HTTPS URL.
6. GitHub Actions must show a green build before merging changes.

Free-tier availability, quotas, sleep behaviour and policies can change. This is a student/demo deployment, not a guaranteed always-on production service.

## Tests and security checks

The GitHub Actions workflow runs `mvn --batch-mode clean verify`, including the unit tests. The Docker build also runs Maven verification before packaging the image. Browser/database smoke tests still need to be performed against the deployed service after each meaningful change.

## Modules and limitations
The generic module screens support listing and adding records. Admissions support a discharge action; bills support marking paid. The application intentionally avoids hard delete buttons to preserve hospital history. It is a minimal repaired foundation, not a full clinical information system. Before submission, expand integration tests and validate every workflow against your requirements.

## Troubleshooting
- `APP_PASSWORD is not configured`: add the environment variable to the hosting service and redeploy.
- `DB_URL ... not configured`: configure the JDBC URL in environment variables.
- Database connection refused: check host, database name, username, password, SSL requirements and provider allow-list.
- `relation ... does not exist`: run `database/schema.sql` in the correct database.
- Maven fails: run `mvn clean verify` and read the first compiler error.
- Login succeeds but database page fails: inspect server logs; do not expose credentials in public issue reports.
