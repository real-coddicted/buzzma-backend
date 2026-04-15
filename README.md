# MOBO Java Backend (Spring Boot)

This Spring Boot service exposes migration-driven REST controllers for PostgreSQL tables defined in:

- `backend/prisma/migrations/0_baseline/migration.sql`
- follow-up `ALTER TABLE` / `DROP` migrations in `backend/prisma/migrations/*`

## What was generated

- Per-table CRUD modules under `src/main/java/com/mobo/javabackend/generated/`:
  - `controller/*Controller.java` (one REST controller per table)
  - `service/*Service.java` (CRUD orchestration)
  - `repository/*Repository.java` (`JpaRepository` per table)
  - `entity/*Entity.java` (JPA entities mapped from migrations)
  - `dto/*RequestDto.java` and `dto/*ResponseDto.java`
  - `mapper/*Mapper.java` (MapStruct mappers)
  - `rowmapper/*RowMapper.java` (JDBC row-to-entity mapping for read queries)

Resources exposed (examples):

- `/api/users`
- `/api/orders`
- `/api/campaigns`
- `/api/tickets`
- `/api/security_questions`
- `/api/ticket_comments`
- one controller is generated for each table from migrations.

## API shape (per table)

For each generated table endpoint (for example `/api/users`, `/api/orders`, `/api/tickets`):

- `GET /api/<table>?limit=50&offset=0`
- `GET /api/<table>/{id}`
- `POST /api/<table>`
- `PATCH /api/<table>/{id}`
- `DELETE /api/<table>/{id}`

Notes:

- For tables with `is_deleted`, delete is implemented as soft delete.
- If `updated_at` exists, patch updates it automatically.
- MapStruct handles request/entity/response conversions.
- RowMappers are used for read paths (`list` and `getById`) to map rows into entities.

## Flyway migrations

- Prisma SQL migrations are mirrored into Flyway files under `src/main/resources/db/migration`.
- Version naming used:
  - `V0001__baseline.sql`
  - `V0002__<description>.sql`, `V0003__<description>.sql`, ... for subsequent migrations
- Flyway is enabled via Spring configuration and runs at app startup.

## Run

Set DB variables if needed:

- `DB_URL` (default: `jdbc:postgresql://localhost:5432/mobo`)
- `DB_USERNAME` (default: `postgres`)
- `DB_PASSWORD` (default: `postgres`)
- `PORT` (default: `8081`)

Start app:

```bash
./gradlew bootRun
```

Run tests:

```bash
./gradlew test
```

