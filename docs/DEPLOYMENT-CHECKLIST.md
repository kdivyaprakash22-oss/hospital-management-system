# Deployment checklist

- [ ] Use only fictional demo data.
- [ ] Set DB_URL, DB_USERNAME, DB_PASSWORD, APP_USERNAME and APP_PASSWORD in the hosting dashboard.
- [ ] Run database/schema.sql and database/sample-data.sql.
- [ ] `mvn clean verify` succeeds locally.
- [ ] GitHub Actions build is green.
- [ ] Docker image builds and app starts locally.
- [ ] Login is required before any module opens.
- [ ] Add and list one record in each module.
- [ ] Test admission, discharge and bill payment.
- [ ] Verify DB data persists across app redeploys.
- [ ] Review logs for errors and secrets.
- [ ] Do not use real patient records.
- [ ] Do not claim production/clinical readiness.
