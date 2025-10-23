# 📝 Blog Platform (Spring Security + TDD)

A simple blog platform built with **Spring Boot** and **Spring Security**, originally based on a YouTube course project.  
I extended the base project by implementing **unit, integration, and security tests** to strengthen my understanding of **TDD (Test-Driven Development)** and modern backend development practices.

---

## 🚀 Features

- User authentication & authorization with **JWT**
- CRUD operations for **Posts**, **Categories**, and **Tags**
- Role-based access control (secured endpoints)
- Centralized error handling with custom `ApiErrorResponse`
- Test-driven development practice with **JUnit 5**, **Mockito**, and **Spring Boot Test**

---

## 🧱 Project Structure

```
src/
 └── main/
     ├── config/               # Security configuration
     ├── controllers/          # Auth, Category, Post, Tag, Error controllers
     ├── domain/
     │    ├── entities/        # Category, Post, Tag, User
     │    └── dtos/            # Data transfer objects
     ├── mappers/              # MapStruct mappers
     ├── repositories/         # Spring Data JPA repositories
     ├── security/             # JwtAuthenticationFilter, BlogUserDetailsService
     └── services/             # Business logic services
 └── test/
     ├── controllers/          # Small integration tests for endpoints
     ├── integration/          # Integration test for security, full logic
     ├── repositories/         # Unit tests for repositories
     └── services/             # Unit tests for services
```

---

## 🧪 Test Strategy

All tests are written **after** the base project was completed (no tests in the original course).  
This part was added to deepen my TDD practice.

### **Testing Layers**
| Layer | Tool | Example |
|--------|------|----------|
| Service | JUnit 5 + Mockito | `UserServiceTest`, `PostServiceTest` |
| Controller | MockMvc | `AuthControllerTest`, `PostControllerTest` |
| Repository | H2 Database | `UserRepositoryTest`, `PostRepositoryTest` |
| Integration | @SpringBootTest | Full authentication flow test |
| Security | MockMvc + JWT | `JwtAuthenticationFilterTest` |

---

## 🧩 Database Setup

| Environment | DB | Description |
|--------------|----|-------------|
| Development | PostgreSQL (Docker) | Main database |
| Test | H2 Database | In-memory test database |

**Docker setup example:**
```bash
docker run --name blog-postgres -e POSTGRES_PASSWORD=1234 -e POSTGRES_DB=blogdb -p 5432:5432 -d postgres
```

---

## 🌿 Branching Strategy

| Branch | Description |
|---------|-------------|
| `main` | Full implemented project |
| `test/repository` | Unit tests for Repository |
| `test/service` | Unit tests for Service |
| `test/controller` | Integration tests for Controller |
| `test/security` | Security unit tests |
| `test/integration` | Full app integration tests |
| `docs/update-readme` | Documentation and README updates |

Each branch focuses on one testing or refactoring goal.  
All branches are merged into `main` via pull requests.

---

## 🧰 Tech Stack

| Type | Technologies |
|------|---------------|
| **Backend** | Spring Boot, Spring Security, Spring Data JPA |
| **Language** | Java 17 |
| **Database** | PostgreSQL, H2 (for testing) |
| **Build Tool** | Maven |
| **Testing** | JUnit 5, Mockito, MockMvc |
| **Auth** | JWT (JSON Web Tokens) |
| **Containerization** | Docker |

---

## 💡 What I Learned

- Building authentication and authorization with **Spring Security**
- Writing **clean and maintainable tests**
- Applying **TDD principles** to improve code design
- Managing **Git branches and pull requests** like real-world teams
- Working with **Dockerized databases** for local and CI testing

---

## 🧾 Future Improvements

- Add role for User
- Integrate CI/CD pipeline with GitHub Actions
- Add refresh token mechanism
- Expand API documentation with Swagger

---

## 🧑‍💻 How to Run Locally

1. Clone this repository  
   ```bash
   git clone https://github.com/<your-username>/spring-security-blog-platform.git
   ```
2. Navigate into the project  
   ```bash
   cd spring-security-blog-platform
   ```
3. Run PostgreSQL container  
   ```bash
   docker-compose up -d
   ```
4. Run the app  
   ```bash
   mvn spring-boot:run
   ```
5. Run tests  
   ```bash
   mvn test
   ```

---

## 📜 License
This project is for **learning and portfolio purposes only**.  
The base is from YouTube course, but all tests and additional logic are implemented by me.
Source: https://youtu.be/Gd6AQsthXNY?si=WUWNrQ0ARfuUE688
