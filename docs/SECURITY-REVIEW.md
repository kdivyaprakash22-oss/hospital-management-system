# Security Review and Verification Checklist

## Scope

This review covers the JSP/Servlet application code, database schema, build configuration, Dockerfile, and GitHub Actions workflow. It is a student/demo review, not a formal penetration test or approval for clinical use.

## Changes in this branch

- Escapes database values, page titles, and flash messages before writing them into HTML to reduce stored/reflected XSS risk.
- Adds session-bound CSRF tokens to login, record creation, admission discharge, bill payment, and sign-out forms.
- Rotates the HTTP session after successful login to reduce session-fixation risk.
- Makes sign-out a POST action instead of a state-changing GET.
- Checks select-field values on the server rather than trusting browser controls.
- Validates that a bill's patient matches the patient attached to its selected admission.
- Updates patient status when an admission is created or discharged, and wraps discharge-related updates in a database transaction.
- Logs technical database/operation errors server-side while displaying a generic message to the user.

## Important limitations that remain

- Authentication is a single shared username/password read from environment variables. It is not a user account system and has no roles or per-user permissions.
- Passwords are compared as plain text from environment variables; use a proper identity provider or secure password hashing for any real application.
- There is no login rate limiting, account lockout, audit trail, password reset flow, or multi-factor authentication.
- There is no comprehensive automated integration test suite for database workflows.
- Database schema constraints and application validation need further domain review, including attendance staff references, room availability rules, and clinical/business rules.
- Backup/restore, monitoring, dependency scanning, privacy compliance, incident response, and a professional security review are not included.
- Never store real patient data in this demo. Use fictional sample data only.

## Post-deployment smoke test

1. Open the deployed login page and sign in with the environment variables configured in the hosting dashboard.
2. Confirm a direct visit to `/app/` without a session redirects to login.
3. Add a fictional patient and confirm it remains after refresh.
4. Try adding a doctor with an invalid department ID; the app should show a generic failure rather than a server stack trace.
5. Create a room, patient, and admission in that order. Confirm the patient is shown as ACTIVE.
6. Discharge the admission and confirm the admission and patient status are updated correctly.
7. Create a bill linked to that admission using the same patient ID. Try mismatched patient/admission IDs and confirm the save is rejected.
8. Mark an unpaid bill paid and confirm it remains PAID after refresh.
9. Sign out and confirm protected pages require login again.
10. Review Render logs and GitHub Actions. Never paste database URLs with passwords, environment-variable values, or patient data into issues or chat.

## Deployment caution

Do not merge or deploy this branch until the pull request's build check is green and the changed flows have been tested. After deployment, verify the live application manually. A successful Maven build alone does not prove the database workflows or security controls are correct.
