# PGN Parser for Java

The PGN Parser for Java is a tool that tokenizes PGN strings. It recursively traverses nested variation trees (N-ary trees) using a depth-first approach. The parser extracts all unique move sequences and flattens complex variations into individual linear lines. The resulting PGN can be imported into the "Study" feature on Lichess
. This allows users to train the linear lines using the spaced repetition method.
## Architecture

- **Backend:** Java 21, Spring Boot 3.5.5, REST API, OpenAPI/Swagger, classical layer architecture without persistence (no user data persisted)
- **Frontend:** React 19, TypeScript, Vite, Axios
- **API:** OpenAPI 3.0 spec (`backend/openapi/pgn-parser.yaml`), Swagger UI, auto-generated client in frontend.

## Build & Run

### Backend

```sh
cd backend
./mvnw clean package
java -jar target/*.jar
```
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- API base path: `/api`

### Frontend

```sh
cd frontend
npm ci
npm run build
npm run dev
```
- App runs at [http://localhost:5173](http://localhost:5173) (default Vite port)

## Test

### Backend

```sh
cd backend
./mvnw test
```
- Coverage: `target/site/jacoco/jacoco.xml`

### Frontend

```sh
cd frontend
npm test
```
- Coverage: `frontend/coverage/lcov.info`

## CI/CD Pipeline

- GitLab CI/CD: see `.gitlab-ci.yml` and `.gitlab/ci/*.yml`
- Stages: `analyze`, `test`, `build`, `quality`, `package`
- Docker images built in `package` stage (see `.gitlab/ci/package.yml`)

## API Documentation

- OpenAPI spec: `backend/openapi/pgn-parser.yaml`
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Main endpoints:
	- `POST /api/pgn/separated` – Convert PGN to separated format
	- `POST /api/pgn/combined` – Convert PGN to combined format
	- `POST /api/study` – Upload PGN as a new Lichess study (requires Lichess OAuth session)
	- `GET /api/auth/lichess/login` – Start the Lichess OAuth flow (302 to Lichess)
	- `GET /api/auth/lichess/callback` – OAuth redirect URI
	- `GET /api/auth/me` – Returns `{ authenticated, username? }`
	- `POST /api/auth/logout` – Revoke the Lichess token and invalidate the session

## Lichess OAuth

The backend implements OAuth 2.0 Authorization Code + PKCE against Lichess. Lichess is a public
client (no client secret); the configured `client_id` is the application name shown to the user on
the Lichess consent page. The access token is held in the user's `HttpSession` only — there is no
database. The session is identified by the `JSESSIONID` cookie, which the frontend sends thanks to
axios `withCredentials: true`.

Configurable via environment variables (defaults in `application.properties`):

| Variable | Default | Purpose |
|---|---|---|
| `LICHESS_CLIENT_ID` | `pgn-parser-dev` | OAuth client identifier (shown to user) |
| `LICHESS_REDIRECT_URI` | `http://localhost:8080/api/auth/lichess/callback` | OAuth redirect URI |
| `APP_FRONTEND_URL` | `http://localhost:5173` | Where the callback redirects on completion |

Requested scopes: `study:read`, `study:write`.

## Usage

1. Start backend and frontend as above.
2. Open frontend in browser.
3. Paste PGN text.
4. Extract split PGN.
5. Click *Sign in with Lichess* (one-time per session), then *Upload to Lichess*.
6. API can be used directly from Swagger UI or any API client like Postman (see OpenAPI docs for request/response formats).

## Outlook

As this project was created to prove my coding proficiency and systemic understanding, it lacks
features that may be included in future iterations, among which would be:

- Connection of persistence via Spring JPA and PostgreSQL in order to save both user-specific tokens and converted PGN
- Enhancement of UX (especially addition of chessboard, which is a lot of work)
- Deployment of code to provide the functionality to wider chess audience

## Development Notes

- Backend and frontend are decoupled; API client is auto-generated from OpenAPI spec.
- No database required; all PGN processing is in-memory, and the Lichess OAuth token lives in the user's HTTP session.
- Authentication: HTTP Basic Auth (default user: `alpa`, password: `secret123`) gates `/api/**` for non-OAuth endpoints; the Lichess OAuth session is layered on top for `/api/study`.
