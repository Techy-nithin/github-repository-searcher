# GitHub Repository Searcher

A Java Spring Boot backend application that integrates with the GitHub REST API to search repositories, store repository information in PostgreSQL, and retrieve saved repositories using filters and sorting.

## Live Deployment

- **Live API:** https://github-repository-searcher-1qqg.onrender.com
- **Swagger Documentation:** https://github-repository-searcher-1qqg.onrender.com/swagger-ui/index.html
- **OpenAPI Specification:** https://github-repository-searcher-1qqg.onrender.com/v3/api-docs

## Features

- Search GitHub repositories by name or keyword.
- Filter GitHub search results by programming language.
- Sort search results by stars, forks, or last updated date.
- Store repository information in PostgreSQL.
- Update existing repositories instead of creating duplicates.
- Retrieve saved repositories.
- Filter saved repositories by language and minimum stars.
- Handle invalid requests, GitHub API errors, and empty results.
- Automated unit tests using JUnit 5 and Mockito.
- API documentation using Swagger and OpenAPI.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot 4.1.2 | Backend framework |
| Spring Web MVC | REST API development |
| Spring Data JPA | Database operations |
| Hibernate | ORM |
| PostgreSQL 18 | Database |
| Maven | Dependency management |
| JUnit 5 | Unit testing |
| Mockito | Mocking dependencies |
| Springdoc OpenAPI | API documentation |
| GitHub REST API | External repository search |

## Project Architecture

The application follows a layered architecture:

- **Controller:** Handles incoming HTTP requests and API responses.
- **Service:** Implements business logic, repository mapping, filtering, sorting, and upsert operations.
- **Client:** Communicates with the GitHub REST API.
- **Repository:** Handles PostgreSQL database operations using Spring Data JPA.
- **Entity:** Represents repository records in the database.
- **DTO:** Transfers request and response data.
- **Exception:** Handles application and API errors.

## Database Design

**Database:** `github_searcher_db`

**Table:** `github_repositories`

| Column | Description |
|---|---|
| id | GitHub repository ID (Primary Key) |
| name | Repository name |
| description | Repository description |
| owner | Repository owner |
| language | Programming language |
| stars | Number of stars |
| forks | Number of forks |
| last_updated | Last updated timestamp |

The GitHub repository ID is used as the primary key to prevent duplicate records.

## REST API Endpoints

### 1. Search GitHub Repositories

**POST** `/api/github/search`

Searches GitHub repositories and saves or updates the results in PostgreSQL.

**Request Body:**

```json
{
  "query": "spring boot",
  "language": "Java",
  "sort": "stars"
}
```

Supported sorting values:

- `stars`
- `forks`
- `updated`

### 2. Retrieve Saved Repositories

**GET** `/api/github/repositories`

Retrieves saved repositories with optional filters.

**Query Parameters:**

| Parameter | Description |
|---|---|
| language | Filter by programming language |
| minStars | Minimum number of stars |
| sort | Sort by stars, forks, or updated date |

**Example:**

```http
GET /api/github/repositories?language=Java&minStars=100&sort=stars
```

Default sorting: stars in descending order.

## API Documentation

The project uses Swagger UI and OpenAPI for API documentation.

Once the application is running, access:

- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

Swagger UI allows users to explore and test the REST API endpoints.

## Setup and Installation

### Prerequisites

- Java 21 or later
- Maven
- PostgreSQL
- Git

### 1. Clone the Repository

```bash
git clone YOUR_GITHUB_REPOSITORY_URL
cd github-searcher
```

### 2. Create the PostgreSQL Database

```sql
CREATE DATABASE github_searcher_db;
```

### 3. Configure Database Connection

Set the following environment variables:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Configure `src/main/resources/application.properties`:

```properties
spring.application.name=github-searcher

spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/github_searcher_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Do not commit actual database credentials to GitHub.

### 4. Run the Application

```bash
mvn spring-boot:run
```

The application runs at:

`http://localhost:8080`

## Running Tests

Run all automated tests using:

```bash
mvn test
```

The project includes **20 automated service and controller tests** using JUnit 5 and Mockito.

These tests cover repository saving and updating, filtering, sorting, request validation, and controller behaviour.

## Error Handling

The application handles:

- Invalid search requests.
- Invalid sorting parameters.
- GitHub API errors.
- GitHub API rate limits and forbidden requests.
- Connection failures.
- Unexpected application errors.
- Empty search results.

## Future Improvements

- Automated integration testing.
- Deployment with a hosted PostgreSQL database.
- Pagination for retrieving saved repositories.
- Additional search and filtering capabilities.

## Author

**G Nithin**

