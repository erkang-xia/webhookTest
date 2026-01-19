# Webhook Test App

Spring Boot webhook helper that responds with whatever HTTP status code you ask for. Useful for testing integrations that need to see specific response codes or bodies.

## Prerequisites
- Java 21+
- Maven Wrapper is included; no global Maven install required.

## Run the app
```bash
./mvnw spring-boot:run
```

## API
- `POST /webhook/test` — returns the requested status code and echoes a message.
- `GET /webhook/health` — basic readiness check.

### Request body
```json
{
  "statusCode": 201,
  "message": "Created by webhook"
}
```
- `statusCode` (optional): integer between 100 and 599. Defaults to `200` if omitted.
- `message` (optional): string to echo back; falls back to `Webhook processed with status <code>`.

### Examples
Successful response with a custom status:
```bash
curl -i -X POST http://localhost:8080/webhook/test \
  -H "Content-Type: application/json" \
  -d '{"statusCode":202,"message":"Queued for processing"}'

# also works if you send JSON as text/plain
curl -i -X POST http://localhost:8080/webhook/test \
  -H "Content-Type: text/plain" \
  -d '{"statusCode":202,"message":"Queued for processing"}'
```

Invalid status code is rejected with `400`:
```bash
curl -i -X POST http://localhost:8080/webhook/test \
  -H "Content-Type: application/json" \
  -d '{"statusCode":42}'
```

### Response shape
Both successful and error responses include a timestamp.
- Success body: `{"statusCode":202,"message":"Queued for processing","receivedAt":"2025-02-18T12:34:56.789Z"}`
- Error body: `{"error":"Status code must be between 100 and 599.","timestamp":"2025-02-18T12:34:56.789Z"}`

## Project structure (MVC)
- `src/main/java/com/erkang/webhooktest/controller` — REST controllers and exception advice.
- `src/main/java/com/erkang/webhooktest/service` — status-code processing logic.
- `src/main/java/com/erkang/webhooktest/model` — request/response payload models.

## Tests
Run the test suite:
```bash
./mvnw test
```

## Deploy to Railway
The app reads its port from `PORT` (default 8080), which Railway provides automatically.

1) Install and log in to the CLI:
```bash
npm install -g @railway/cli
railway login
```
2) From the project root, create/link a Railway project:
```bash
railway init
```
3) Deploy (Railway will auto-detect Java/Maven via Nixpacks):
```bash
railway up
```
4) Once deployed, find the service URL with:
```bash
railway status
```
The web service listens on `PORT` and responds to `/webhook/test` and `/webhook/health`.
