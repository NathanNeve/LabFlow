# Local development servers (LabFlow)

This guide describes how to run the **Spring Boot backend** and **SvelteKit frontend** on your own machine. Paths are relative to the **repository root** (the folder that contains `code/` and `documents/`).

## Prerequisites

- **Java 21** and **Maven** (backend)
- **Node.js** between **20.19 and 22.x** (see `engines` in `code/labflow-frontend/package.json`)
- **npm** (comes with Node)

Check versions:

```bash
java -version
mvn -version
node -version
npm -version
```

## 1. Backend (Spring Boot)

The API listens on **port 8080** by default.

1. Open a terminal and go to the backend module:

   ```bash
   cd code/labflow-backend
   ```

2. **Environment file for seeded users**  
   Copy the example and fill in bcrypt seed passwords (plain text in `.env`; the `AuthDataLoader` hashes them again for storage—same behaviour as before):

   ```bash
   cp .env.example .env
   ```

   Edit `.env` and set non-empty values for:

   - `USER_ADMIN_PASSWORD`
   - `USER_NATHAN_PASSWORD`
   - `USER_CESAR_PASSWORD`

   If these are missing at runtime, the auth seed step will fail when it tries to read them from dotenv.

3. **SQLite databases**  
   Ensure the directory exists (from `code/labflow-backend`):

   ```bash
   mkdir -p databases
   ```

   The app uses three files under `databases/`: `auth.db`, `hematology.db`, and `microbiology.db`. With `labflow.jpa.ddl-auto=create` (see `application.properties`), **each backend start drops all tables and recreates them**, then the data loaders insert seed data again.

4. **Start the server:**

   ```bash
   mvn spring-boot:run
   ```

   Wait until the log shows the application has started. The API base URL is typically:

   ```text
   http://localhost:8080
   ```

5. **Quick check (optional)**  
   A public health endpoint is configured for the microbiology persistence unit:

   ```text
   http://localhost:8080/api/microbiology/health
   ```

## 2. Frontend (SvelteKit + Vite)

The dev server defaults to **port 5173** (`npm run dev`).

1. Open a **second** terminal:

   ```bash
   cd code/labflow-frontend
   ```

2. Install dependencies (first time, or after `package.json` changes):

   ```bash
   npm install
   ```

3. **Point the UI at your local API**  
   Create `code/labflow-frontend/.env` (this file is usually gitignored) with the backend origin your browser will call:

   ```env
   VITE_BACKEND_PATH=http://localhost:8080
   ```

   Use the same scheme/host/port as Spring Boot. No trailing slash is required unless your code expects one consistently.

4. **Start the dev server:**

   ```bash
   npm run dev
   ```

5. Open the app in the browser:

   ```text
   http://localhost:5173
   ```

   On some Linux setups you may use `http://127.0.0.1:5173` instead.

## 3. Running both together

- Start the **backend first** (or ensure it is up before logging in or loading data).
- Keep **two terminals** open: one for `mvn spring-boot:run`, one for `npm run dev`.
- CORS for local development is already allowed for `http://localhost:5173` in the backend security configuration.

## 4. Useful commands (reference)

| Location | Command | Purpose |
|----------|---------|---------|
| `code/labflow-backend` | `mvn spring-boot:run` | Run API |
| `code/labflow-backend` | `mvn test` | Run backend tests |
| `code/labflow-frontend` | `npm run dev` | Run frontend dev server |
| `code/labflow-frontend` | `npm run build` | Production build |
| `code/labflow-frontend` | `npm run preview` | Preview production build locally |

## 5. Troubleshooting

- **Login or API calls fail / CORS errors**  
  Confirm `VITE_BACKEND_PATH` matches where Spring Boot is listening and that the backend is running.

- **Seed users not created**  
  Check `code/labflow-backend/.env` and the three `USER_*_PASSWORD` variables.

- **JWT / 401 after backend restart**  
  Tokens are signed with `labflow.jwt.secret` (or env `JWT_SIGNING_SECRET`), which must stay the same across restarts. If you change the secret, log in again. Invalid `Authorization` headers are ignored so the app should not crash with a 500.

- **Data reset**  
  A normal restart already clears and re-seeds (see `labflow.jpa.ddl-auto=create`). You only need to delete files under `databases/` manually if you want to remove the SQLite files entirely. See `code/labflow-backend/MIGRATION-SQLITE.md` if you are moving from an old single `labflow.db` setup.

For broader installation and deployment notes, see `documents/files/installation.md` (some details there may predate the split-database layout; this file reflects the current `code/` layout).
