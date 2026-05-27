# UrbanMicro-CAD Backend

Spring Boot backend for UrbanMicro-CAD.

## Environment variables

```bash
SERVER_PORT=8080
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=<db-user>
SPRING_DATASOURCE_PASSWORD=<db-password>
JWT_SECRET=<at-least-32-characters-secret>
JWT_EXPIRES_MINUTES=1440
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173
```

Do not commit real Supabase credentials or JWT secrets.

## Run

```bash
mvn test
mvn spring-boot:run
```

OpenAPI UI: `http://localhost:8080/swagger-ui.html`.
