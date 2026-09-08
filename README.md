# MiniTwitter 🐦

A lightweight social feed platform with a Spring Boot backend and an Angular frontend: user registration, JWT-based login, and a public post feed.

## 🚀 Features

- **User Management**: Registration, login, JWT-based authentication, profile view/edit
- **Posts**: Create posts and browse a public feed (no likes/comments yet — see Roadmap)
- **Monitoring**: Prometheus metrics and Grafana dashboards (infra provisioned, not yet wired into app code)

## 🛠️ Tech Stack

- **Backend**: Java 25, Spring Boot 4.1.1, Spring Security, Spring Data JPA
- **Frontend**: Angular 20 (standalone components)
- **Database**: PostgreSQL 16
- **Cache / Streaming**: Redis 7, Apache Kafka (provisioned via Docker Compose; not yet used by application code)
- **Security**: JWT authentication (`jjwt`), BCrypt password hashing
- **Monitoring**: Prometheus, Grafana, Spring Boot Actuator
- **Containerization**: Docker / Podman, Docker Compose

## 📋 Prerequisites

- Java 25 or higher
- Node.js 20.19+ or 22+ and npm (for frontend development)
- Docker or Podman + Compose (for Postgres and the optional infra stack)
- Maven (or use the included Maven Wrapper, `./mvnw`)

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/naosh1ma/MiniTwitter.git
cd MiniTwitter
```

### 2. Start Postgres (and optional infra)
```bash
docker-compose up -d postgres
```
This starts just the database on port 5433, matching `application.properties`. The compose file also defines `backend`, `frontend`, and a full monitoring/logging stack (Redis, Kafka, Prometheus, Grafana, ELK) — see [Docker Services](#-docker-services) if you want to run everything containerized instead of the local dev workflow below.

### 3. Run the Backend
```bash
./mvnw spring-boot:run
```
Starts on `http://localhost:8080`. On first run, Hibernate creates the `users` and `posts` tables automatically (`spring.jpa.hibernate.ddl-auto=update`).

### 4. Run the Frontend
```bash
cd minitwitter-frontend
npm install
npm start
```
Starts on `http://localhost:4200` and proxies API calls to `http://localhost:8080/api`.

## 📚 API Endpoints

### Auth
- `POST /api/auth/login` — Log in, returns a JWT + user info
- `POST /api/auth/validate` — Validate a `Bearer` token

### Users
- `POST /api/users/register` — Register a new user
- `GET /api/users/{username}` — Get a user's public info
- `GET /api/users/profile` — Get the logged-in user's profile *(requires auth)*
- `PUT /api/users/profile` — Update the logged-in user's bio *(requires auth)*

### Posts
- `POST /api/posts` — Create a post *(requires auth)*
- `GET /api/posts/feed?page=0&size=20` — Paginated public feed *(no auth required)*

### Example Usage

**Register a user:**
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "email": "test@example.com", "password": "password123"}'
```

**Log in:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}'
```

**Create a post (with the token from login):**
```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"content": "Hello MiniTwitter!"}'
```

## 🏗️ Project Structure

```
.
├── src/main/java/org/art/mt/
│   ├── config/          # Security, CORS, JWT filter
│   ├── controller/      # REST controllers
│   ├── dto/              # Request/response DTOs
│   ├── entity/           # JPA entities
│   ├── exception/        # Exception handling
│   ├── repository/       # Data repositories
│   ├── service/          # Business logic
│   └── MTApplication.java
├── src/main/resources/application.properties
├── minitwitter-frontend/  # Angular app (standalone components)
│   └── src/app/
│       ├── components/    # auth, feed, create-post, header, profile
│       ├── services/       # API client
│       ├── interceptors/   # JWT auth interceptor
│       └── models/
├── Dockerfile             # Backend image
├── minitwitter-frontend/Dockerfile  # Frontend image
├── docker-compose.yml     # Full stack: app + infra + monitoring
└── pom.xml
```

## 🔧 Configuration

`src/main/resources/application.properties` holds dev-only, hardcoded values — fine for local development, but replace them before deploying anywhere reachable:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/MiniTwitter
spring.datasource.username=postgres
spring.datasource.password=devpass123

jwt.secret=minitwitter-dev-secret-change-me
jwt.expiration=86400000
```

## 🐳 Docker Services

`docker-compose.yml` defines the full stack — application and infrastructure:

| Service | Port | Description |
|---------|------|-------------|
| frontend | 4200 | Angular app (Nginx) |
| backend | 8080 | Spring Boot API |
| postgres | 5433 | Main database |
| redis | 6379 | Caching layer (provisioned, not yet used by the app) |
| kafka | 9092 | Message streaming (provisioned, not yet used by the app) |
| prometheus | 9090 | Metrics collection |
| grafana | 3000 | Monitoring dashboard |
| elasticsearch | 9200 | Log storage and search |
| logstash | 5044 | Log processing |
| kibana | 5601 | Log visualization |

Run `docker-compose up -d` to start everything, or target specific services (e.g. `docker-compose up -d postgres`) for the local dev workflow above.

## 🗺️ Roadmap

Known gaps, not yet implemented:
- Likes and comments on posts
- Avatar upload (`POST /api/users/profile/avatar` — frontend calls it, backend doesn't implement it yet)
- Redis caching and Kafka event streaming are provisioned in Docker Compose but not yet wired into the application

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Arthur** - [@naosh1ma](https://github.com/naosh1ma)
