# MiniTwitter 🐦

A lightweight social feed platform built with Spring Boot, featuring user management, posts, and real-time feeds.

## 🚀 Features

- **User Management**: Registration, login, JWT-based authentication
- **Posts**: Create, read, like, and comment on posts
- **Feeds**: Home timeline and user-specific timelines
- **Real-time**: Kafka-based event streaming
- **Caching**: Redis for performance optimization
- **Monitoring**: Prometheus metrics and Grafana dashboards

## 🛠️ Tech Stack

- **Backend**: Java 25, Spring Boot 3.5.6
- **Database**: PostgreSQL 16
- **Cache**: Redis 7
- **Message Queue**: Apache Kafka
- **Security**: JWT Authentication
- **Monitoring**: Prometheus, Grafana
- **Containerization**: Docker, Docker Compose

## 📋 Prerequisites

- Java 25 or higher
- Docker and Docker Compose
- Maven (or use included Maven Wrapper)

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/naosh1ma/MiniTwitter.git
cd MiniTwitter
```

### 2. Start Infrastructure Services
```bash
docker-compose up -d
```

This will start:
- PostgreSQL (Port 5433)
- Redis (Port 6379)
- Kafka + Zookeeper (Port 9092)
- Prometheus (Port 9090)
- Grafana (Port 3000)

### 3. Build and Run the Application
```bash
# Using Maven Wrapper
./mvnw clean package -DskipTests
java -jar target/MiniTwitter-0.0.1-SNAPSHOT.jar

# Or using Maven directly
mvn clean package -DskipTests
mvn spring-boot:run
```

### 4. Access the Application
- **API Base URL**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **Prometheus Metrics**: http://localhost:8080/actuator/prometheus
- **Grafana Dashboard**: http://localhost:3000 (admin/admin)

## 📚 API Endpoints

### User Management
- `POST /api/users/register` - Register a new user
- `GET /api/users/{username}` - Get user by username

### Example Usage

**Register a User:**
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Get User:**
```bash
curl http://localhost:8080/api/users/testuser
```

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/org/art/mt/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── entity/          # JPA entities
│   │   ├── repository/      # Data repositories
│   │   ├── service/         # Business logic
│   │   └── MTApplication.java
│   └── resources/
│       └── application.properties
├── docker-compose.yml       # Infrastructure services
├── prometheus.yml          # Prometheus configuration
└── pom.xml                 # Maven dependencies
```

## 🔧 Configuration

### Database Configuration
```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/MiniTwitter
spring.datasource.username=postgres
spring.datasource.password=***REMOVED_DB_PASSWORD***
```

### Redis Configuration
```properties
spring.redis.host=localhost
spring.redis.port=6379
```

### Kafka Configuration
```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=minitwitter-consumer
```

## 🐳 Docker Services

| Service | Port | Description |
|---------|------|-------------|
| PostgreSQL | 5433 | Main database |
| Redis | 6379 | Caching layer |
| Kafka | 9092 | Message streaming |
| Prometheus | 9090 | Metrics collection |
| Grafana | 3000 | Monitoring dashboard |

## 📊 Monitoring

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **Application Metrics**: http://localhost:8080/actuator/prometheus

## 🚧 Development Status

### ✅ Completed
- [x] Project setup and configuration
- [x] Docker infrastructure
- [x] User entity and repository
- [x] User registration and retrieval
- [x] Basic security configuration
- [x] Database integration

### 🔄 In Progress
- [ ] Password hashing (BCrypt)
- [ ] JWT authentication
- [ ] Posts system
- [ ] Feed generation
- [ ] Kafka event streaming

### 📋 Planned
- [ ] Like/Retweet functionality
- [ ] Real-time notifications
- [ ] Search functionality
- [ ] Image uploads
- [ ] Rate limiting
- [ ] Frontend (Angular)

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

---

**Happy Coding! 🚀**
