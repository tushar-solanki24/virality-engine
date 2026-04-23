# Virality Engine - Spring Boot Microservice

## Tech Stack
- Java 17
- Spring Boot 3.x
- PostgreSQL
- Redis (Spring Data Redis)

## How to Run

### Step 1 - Start Docker containers
docker-compose up -d

### Step 2 - Run Spring Boot app
./mvnw spring-boot:run

App runs on: http://localhost:8081

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/users | Create a user |
| POST | /api/bots | Create a bot |
| POST | /api/posts | Create a new post |
| POST | /api/posts/{postId}/comments | Add a comment |
| POST | /api/posts/{postId}/like | Like a post |

## Phase 2 - Redis Virality Engine & Atomic Locks

### Virality Score
Every interaction updates a real-time score in Redis:
- Bot reply → +1 point
- Human like → +20 points  
- Human comment → +50 points

Redis key: `post:{id}:virality_score`

### Atomic Locks (Concurrency Protection)
Three guardrails implemented using Redis atomic operations:

**1. Horizontal Cap**
- Maximum 100 bot replies per post
- Redis key: `post:{id}:bot_count`
- Uses Redis INCR — if count exceeds 100, returns 429 Too Many Requests
- Proven with race condition test: 200 concurrent requests → exactly 100 accepted

**2. Vertical Cap**
- Comment thread cannot exceed 20 depth levels
- Validated before saving to database
- Returns 400 Bad Request if depth > 20

**3. Cooldown Cap**
- A bot cannot interact with same human more than once per 10 minutes
- Redis key: `cooldown:bot_{id}:human_{id}` with 10 minute TTL
- Returns 429 Too Many Requests if cooldown active

## Phase 3 - Notification Engine (Smart Batching)

### Redis Throttler
- First bot interaction → logs "Push Notification Sent to User" + sets 15 min cooldown
- Subsequent interactions within 15 mins → pushed to Redis List (pending)
- Redis key: `user:{id}:pending_notifs`

### CRON Sweeper
- Runs every 5 minutes via Spring @Scheduled
- Scans all users for pending notifications
- Logs summarized message: "Summarized Push Notification: Bot X and N others..."
- Clears Redis list after processing

## Thread Safety Approach

All atomic operations use Redis INCR command which is
single-threaded by nature. This guarantees that even
200 concurrent bot requests will never exceed the
100 reply cap. Redis processes each INCR one at a time,
making race conditions impossible without any Java locks
or synchronized blocks.

## Redis Key Design

| Key | Purpose | TTL |
|-----|---------|-----|
| post:{id}:virality_score | Running virality score | None |
| post:{id}:bot_count | Bot reply counter per post | None |
| cooldown:bot_{id}:human_{id} | Bot-human interaction lock | 10 mins |
| user:{id}:pending_notifs | Batched notification queue | None |
| notif:cooldown:user_{id} | Notification throttle lock | 15 mins |

## Race Condition Test
A Python script `test_race.py` is included to verify the
horizontal cap under load. It fires 200 concurrent bot
requests simultaneously and confirms exactly 100 are
accepted and 100 are rejected.