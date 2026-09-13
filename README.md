# TaskManager — Real-Time Collaborative Task Management System

A Jira/Trello-style backend built with Java + Spring Boot, with a lightweight React
frontend to demonstrate it visually. Showcases JWT auth, role-based access control,
real-time updates over WebSockets, Redis caching, and event-driven messaging via RabbitMQ.

**Frontend repo:** see `taskmanager-frontend` (separate project, run alongside this one)

## What's built

- **Auth** — JWT-based register/login with Spring Security, roles (ADMIN / MANAGER / MEMBER)
- **Projects & Tasks** — full CRUD, paginated + filterable task lists, project membership
- **Real-time updates** — WebSocket (STOMP) broadcasts on every task create/status-change/delete,
  so every connected client sees changes instantly with no refresh
- **Async messaging** — task assignment publishes an event to RabbitMQ; a separate consumer
  sends the notification email, decoupling the request from that side effect
- **Caching** — Redis-backed caching on project lookups via Spring's `@Cacheable`/`@CacheEvict`
- **Testing & CI** — JUnit + Mockito unit tests for the service layer, running automatically
  on every push via GitHub Actions
- **API docs** — Swagger/OpenAPI UI with JWT bearer auth support built in

## Architecture at a glance

```
Client (React)
   │
   ├── REST API ──────────► Spring Boot ──────► PostgreSQL (users, projects, tasks)
   │                             │
   │                             ├──► Redis (cache: project lookups)
   │                             │
   │                             └──► RabbitMQ ──► Email consumer ──► SMTP
   │
   └── WebSocket (STOMP) ◄── broadcasts task events live
```

## Tech stack
Java 17 · Spring Boot 3 · Spring Security · Spring Data JPA · PostgreSQL · Redis ·
RabbitMQ · WebSockets (STOMP/SockJS) · JUnit/Mockito · GitHub Actions · React (frontend)

## How to run locally

### 1. Prerequisites
- JDK 17+
- Maven (or use VS Code's bundled Maven via the Java extension)
- A PostgreSQL database (local install, or a free cloud instance like [Neon](https://neon.tech))
- A Redis instance (free tier on [Upstash](https://upstash.com) works well)
- A RabbitMQ instance (free tier on [CloudAMQP](https://cloudamqp.com) works well)

### 2. Configure secrets
Copy the template and fill in your real values:
```bash
cp .env.example .env
```
Then edit `.env` with your actual Postgres/Redis/RabbitMQ/JWT values. `.env` is
gitignored and never committed — only `.env.example` (with blank values) is tracked,
so the repo never contains real credentials. **The app will fail to start if any
required variable is missing** — this is intentional, so a misconfigured deployment
fails loudly instead of silently falling back to something insecure.

In VS Code, wire `.env` up via `.vscode/launch.json`'s `envFile` property so `Run and
Debug` picks it up automatically.

### 3. Run the app
Open the project in VS Code with the **Extension Pack for Java** and **Spring Boot
Extension Pack** installed, then use **Run and Debug** (not the inline Run button, so
the `.env` file is actually loaded).

The API is live at `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui.html`

### 4. Run the frontend (optional, but recommended)
See the `taskmanager-frontend` project's own README. Once both are running, open two
browser tabs on the same project board and watch task changes sync live between them.

### 5. Try the API directly
```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Ada Lovelace","email":"ada@example.com","password":"password123"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ada@example.com","password":"password123"}'

# Create a project (use the accessToken from above)
curl -X POST http://localhost:8080/api/projects \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"name":"My First Project","description":"Testing it out"}'
```

## Running tests
```bash
mvn test
```
Tests use Mockito to mock the database, Redis, and RabbitMQ — no live infrastructure
needed to run them. The same command runs automatically in CI on every push (see
`.github/workflows/ci.yml`).

## What's next
- **Deployment** — host on Render/Railway with managed Postgres, and put the live URL here
- **File attachments** — upload to S3 or local disk
- **Activity log** — audit trail per task
- **Microservices split** — separate Auth/Task/Notification services, if pursuing that
  specific architecture pattern

## Project structure
```
src/main/java/com/taskmanager/
├── config/       # Security, CORS, WebSocket, Redis, RabbitMQ, OpenAPI config
├── controller/   # REST endpoints
├── dto/          # Request/response objects, WebSocket + RabbitMQ event payloads
├── entity/       # JPA entities
├── exception/    # Custom exceptions + global handler
├── repository/   # Spring Data JPA repositories
├── security/     # JWT filter, JWT service, UserDetailsService
└── service/      # Business logic, caching, event publishing/consuming
src/test/java/com/taskmanager/
└── service/      # Unit tests (Mockito)
```

## Resume bullet
> Built a real-time collaborative task management platform using Java, Spring Boot,
> and WebSockets, with JWT-based role authentication, Redis caching, and event-driven
> email notifications via RabbitMQ; covered by unit tests running in a GitHub Actions
> CI pipeline, with a React frontend demonstrating live cross-client updates.
