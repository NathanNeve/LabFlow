# Splitting the old single `labflow.db` into three files

The application now uses three SQLite files under `databases/` (relative to the process working directory):

- `auth.db` — users and roles
- `hematology.db` — stalen, tests, referentiewaarden, etc.
- `microbiology.db` — microbiology module (placeholder entity only until you add real tables)

## Fresh install (development)

Ensure the `databases/` directory exists. On **every** backend start, `labflow.jpa.ddl-auto=create` drops and recreates all tables in each SQLite file, then `AuthDataLoader` (order 1) and `HematologyDataLoader` (order 2) insert seed data again.

For **production**, switch `labflow.jpa.ddl-auto` to `update` (or another non-destructive mode) so existing data is not wiped on deploy.

## Migrating existing data from `labflow.db`

Hibernate will **not** copy rows from the old file automatically. Options:

1. **Manual / one-off script**: attach databases with SQLite `ATTACH`, `INSERT INTO auth.db` tables `user`/`rol`, then `INSERT INTO hematology.db` for the remaining tables (adjust for renamed paths and the `staal.user_id` column, which now stores only the numeric user id instead of a foreign key row in the same file).

2. **Export/import**: use a SQLite browser or `sqlite3` `.dump` for subsets of tables into the new files.

3. **Accept reset**: for production staging, deploy empty files and re-seed only if that is acceptable.

For Docker/Render, mount a single volume directory so all three `.db` files persist together under `/app/databases`.

<!-- docker run -p 8080:8080 `
  -v "${PWD}\.env:/app/.env" `
  labflow-backend -->
