# TaskManager — Real-Time Collaborative Task Management System

A Jira/Trello-style backend built with Java + Spring Boot, showcasing JWT auth,
role-based access control, real-time updates, caching, and async messaging.

## Phase 1 (this scaffold) — Core REST API ✅
- Spring Boot 3.3, Java 17
- JWT-based authentication (register/login) with Spring Security
- Role-based access (ADMIN / MANAGER / MEMBER)
- Projects: create, list, add members
- Tasks: create, list (paginated + filterable by status), update status, delete
- PostgreSQL + Spring Data JPA
- Global exception handling with clean JSON error responses
- Swagger/OpenAPI docs at `/swagger-ui.html`

## How to run locally

### 1. Prerequisites
- JDK 17+
- Maven (or use the wrapper once you generate one — see note below)
- Docker Desktop (for Postgres/Redis/Mailhog)

### 2. Start supporting services
```bash
docker compose up -d
```
This starts Postgres (5432), Redis (6379), and MailHog (SMTP on 1025, web UI on 8025).

### 3. Run the app
Open the folder in VS Code with the **Extension Pack for Java** and **Spring Boot Extension Pack** installed, then:
```bash
mvn spring-boot:run
```
Or just click "Run" above the `main` method in `TaskmanagerApplication.java`.

The API will be live at `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui.html`

### 4. Try it out
```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Ada Lovelace","email":"ada@example.com","password":"password123"}'

# Login (grab the accessToken from the response)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ada@example.com","password":"password123"}'

# Create a project (use the accessToken)
curl -X POST http://localhost:8080/api/projects \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"name":"My First Project","description":"Testing it out"}'
```

## Pushing to GitHub (via GitHub Desktop)
1. Open GitHub Desktop → **File > Add Local Repository** → select this `taskmanager` folder.
2. It'll detect it's not yet a git repo and offer to **create a repository** — click that.
3. Write a commit message like "Phase 1: auth, projects, tasks REST API" → **Commit to main**.
4. Click **Publish repository** (top bar) → choose public/private → Publish.

---

## Roadmap (build this over the next few weeks)

### Phase 2 — Real-time updates (WebSockets)
- Add a `WebSocketConfig` using STOMP over SockJS at `/ws`
- Broadcast task-status-change events to a topic like `/topic/project/{id}`
- Frontend subscribes and updates the Kanban board instantly when others move cards
- Send an email (via MailHog locally) when a task is assigned to someone

### Phase 3 — Caching & scaling
- Cache `GET /api/projects/{id}` and task lists in Redis (`@Cacheable`)
- Evict cache on task/project updates (`@CacheEvict`)
- Use Redis pub/sub so WebSocket broadcasts work across multiple app instances

### Phase 4 — Async messaging (this is a big resume differentiator)
- Add RabbitMQ (or Kafka if you want that specific keyword)
- Instead of sending emails synchronously, publish a `TaskAssignedEvent` to a queue
- A separate `@RabbitListener` consumer sends the email — decouples the request from the side effect

### Phase 5 — Testing & CI/CD
- JUnit + Mockito unit tests for services
- `@SpringBootTest` + Testcontainers (or H2) integration tests for controllers
- GitHub Actions workflow: run `mvn test` on every push, build a Docker image on merge to main

### Phase 6 — Deploy it
- Deploy to Render, Railway, or AWS Elastic Beanstalk (free tiers available)
- Use a managed Postgres (Supabase/Neon free tier) instead of local Docker for production
- Add the live URL + a short demo GIF to your resume/GitHub README

### Optional stretch goals
- File attachments on tasks (upload to S3 or local disk)
- Activity log / audit trail per task
- Full microservices split (Auth service, Task service, Notification service) if you want that keyword specifically — only worth it once Phases 1–5 are solid

---

## Project structure
```
src/main/java/com/taskmanager/
├── config/          # Security config, CORS
├── controller/       # REST endpoints
├── dto/              # Request/response objects
├── entity/            # JPA entities
├── exception/         # Custom exceptions + global handler
├── repository/         # Spring Data JPA repositories
├── security/           # JWT filter, JWT service, UserDetailsService
└── service/            # Business logic
```

## Resume bullet (update as you complete phases)
> Built a real-time collaborative task management platform using Java, Spring Boot,
> and WebSockets, supporting JWT-based role authentication, Redis caching, and
> async email notifications via RabbitMQ; deployed on AWS with CI/CD via GitHub Actions.
