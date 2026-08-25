# LabFlow

LabFlow is a web app for tracking laboratory samples (hematology and microbiology): register samples, print labels, enter results, and print reports.

The code lives in this repository:

- `code/labflow-backend` — Spring Boot API (port **8080**), three SQLite files under `databases/` (`auth.db`, `hematology.db`, `microbiology.db`)
- `code/labflow-frontend` — SvelteKit UI (dev server port **5173**)

---

## Contact

- Nathan Neve — [nathanneve@hotmail.com](mailto:nathanneve@hotmail.com)
- César Van Leuffelen — [cesar_vl@icloud.com](mailto:cesar_vl@icloud.com)

---

## Development environment

**Need:** Java 21, Maven, Node.js ≥ 20.19, npm.

### Backend

```bash
cd code/labflow-backend
cp .env.example .env
mkdir -p databases
```

Set non-empty values in `.env`:

- `USER_ADMIN_PASSWORD`
- `USER_NATHAN_PASSWORD`
- `USER_CESAR_PASSWORD`

Then:

```bash
mvn spring-boot:run
```

API: `http://localhost:8080`  
Health: `http://localhost:8080/api/microbiology/health`

With `labflow.jpa.ddl-auto=create` (current default), each start recreates the schema and re-seeds data.

Seed logins (passwords = the `.env` values above):

| Email | Role |
| --- | --- |
| `adminlabflow@digitalinnovation.be` | admin |
| `nathanneve@test.be` | admin |
| `césarvanleuffelen@test.be` | student |

### Frontend

```bash
cd code/labflow-frontend
echo 'VITE_BACKEND_PATH=http://localhost:8080' > .env
npm install
npm run dev
```

UI: `http://localhost:5173`  
Start the backend first. CORS already allows `http://localhost:5173`.

---

## Production environment

This is what to change in the project so it can run as production, not a full server install.

**1. Do not wipe data on startup**  
In `code/labflow-backend/src/main/resources/application.properties` set:

```properties
labflow.jpa.ddl-auto=update
```

(`create` drops all tables every start.)

**2. Secrets**  
On the host, `code/labflow-backend/.env` (never commit it):

```env
USER_ADMIN_PASSWORD=...
USER_NATHAN_PASSWORD=...
USER_CESAR_PASSWORD=...
JWT_SIGNING_SECRET=at-least-32-characters
```

`JWT_SIGNING_SECRET` must stay the same across restarts or everyone has to log in again. Seed passwords are only used when `auth.db` is empty.

**3. CORS**  
In `SecurityConfig.java`, add the public website origin to `setAllowedOriginPatterns` (no trailing slash), e.g. `https://labflow.example.com`. Rebuild the backend after this change.

**4. Frontend API URL**  
`VITE_BACKEND_PATH` is baked in at **build** time. Create `code/labflow-frontend/.env`:

```env
VITE_BACKEND_PATH=https://api.labflow.example.com
```

No trailing slash. Then `npm install` and `npm run build`.  
If the site is not at the domain root, also set `BASE_PATH` (see `svelte.config.js`), e.g. `BASE_PATH=/LabFlow npm run build`.

**5. Persistent database volumes**  
Store `auth.db`, `hematology.db`, and `microbiology.db` on a **persistent volume**, not inside the container image. The backend `Dockerfile` uses `/app/databases` (`VOLUME ["/app/databases"]`). Mount a host directory (or a named Docker volume) there so rebuilds, restarts, and deploys do not lose data.

**6. How to run**  
- Backend: `mvn spring-boot:run`, or `docker build` / `docker run` from `code/labflow-backend` (port 8080).  
- Frontend: serve the static files from `code/labflow-frontend/build`.

---

## Automatic deployment

After production works, add automatic deploys so pushes to the branch you ship (typically **`Labflow-v2`**) rebuild and restart the backend and frontend. Do not run tests in that pipeline. Keep host `.env` files outside git, and keep the SQLite files on the same persistent volume so deploys do not overwrite secrets or wipe data.
