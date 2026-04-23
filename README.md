# Virality Engine - Spring Boot Microservice

## Tech Stack
- Java 17
- Spring Boot 3.x
- PostgreSQL
- Redis

## How to Run

### Step 1 - Start Docker containers
docker-compose up -d

### Step 2 - Run Spring Boot app
./mvnw spring-boot:run

App runs on: http://localhost:8081

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/posts | Create a new post |
| POST | /api/posts/{postId}/comments | Add a comment |
| POST | /api/posts/{postId}/like | Like a post |

## Thread Safety Approach

All atomic operations use Redis INCR command which is
single-threaded by nature. This guarantees that even
200 concurrent bot requests will never exceed the
100 reply cap. Redis processes each INCR one at a time,
making race conditions impossible without any Java locks.

## Redis Key Design

| Key | Purpose |
|-----|---------|
| post:{id}:virality_score | Running virality score |
| post:{id}:bot_count | Bot reply counter per post |
| cooldown:bot_{id}:human_{id} | 10 min cooldown TTL |
| user:{id}:pending_notifs | Batched notification queue |
| notif:cooldown:user_{id} | 15 min notification TTL