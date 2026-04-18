## Why

The codebase is currently organized by technical layer (`controller/`, `service/`, `repository/`, `entity/`, `dto/`, `mapper/`), so every feature is smeared across six siblings and cross-domain coupling is invisible. This makes the app hard to reason about as it grows and impossible to carve out as microservices later without untangling every package first. We want to move to a domain-first layout now — while the monolith is young — so boundaries are enforced before more features land.

## What Changes

- **BREAKING** (internal package paths only): Reorganize `src/main/java/com/mobo/**` from layer-based packages into domain modules, each containing its own `api/`, `web/`, `service/`, `persistence/`, and `mapper/` sub-packages.
- Introduce module packages: `brands`, `agency`, `mediator`, `buyers`, `orders`, `wallet`, `catalog` (campaigns + deals), `support` (tickets), `identity` (users, auth, invites, security questions, pending connections), `admin` (ops, suspensions, system configs, audit logs), `notifications` (push subscriptions), and a `shared` module for truly cross-cutting infrastructure (security, common utilities, exceptions, base classes).
- Each module exposes an `api` sub-package (public interfaces + DTOs consumed by other modules) and keeps entities, repositories, and service impls internal. Other modules MUST depend only on another module's `api` — not on its repositories or entities.
- Controllers are moved into each module's `web/` sub-package; REST URL paths and payload shapes are unchanged.
- Flyway migrations, `application.yaml`, `SecurityConfig` URL patterns, and all business logic (including the `*DomainService`, `*WorkflowService`, `*BusinessService` classes) are kept byte-for-byte equivalent — only their package locations change.
- Add an ArchUnit test (or equivalent) that fails the build if a module reaches into another module's internals.
- Update `build.gradle` / `checkstyle` / `spotless` targets if any globs depended on old paths.

Non-goals: splitting the Gradle build into sub-projects, extracting any module into a separate service, changing database schema, changing APIs, changing authentication, renaming entities/tables, or altering business behavior.

## Capabilities

### New Capabilities
- `module-boundaries`: Defines the set of domain modules, which entities/services/controllers belong to each, the rules for inter-module communication (only via `api` packages), and the enforcement mechanism (ArchUnit rule or equivalent).

### Modified Capabilities
<!-- None — there are no existing specs in openspec/specs/, and this change does not alter requirement-level behavior of any feature. -->

## Impact

- **Code**: Every file under `src/main/java/com/mobo/**` except `MoboApplication.java` moves to a new package. Java `package` declarations and `import` statements update accordingly; class bodies are untouched.
- **Tests**: `src/test/java/com/mobo/service/**` move alongside their production counterparts; test logic unchanged. One new ArchUnit test added under `shared` (or a top-level `architecture` test package).
- **APIs**: No change — REST paths, request/response shapes, HTTP status codes, and error payloads are identical.
- **Database**: No change — table names, columns, Flyway migrations, and JPA mappings remain the same (entities keep their `@Table` names).
- **Build / tooling**: `build.gradle` Spotless/Checkstyle globs already use `src/*/java/**/*.java` and continue to work. Any IDE run configurations referencing old package paths need to be updated by developers.
- **Dependencies**: Adds ArchUnit as a test dependency (`com.tngtech.archunit:archunit-junit5`).
- **Future microservices**: Each module's `api` package becomes the natural candidate for a shared-contract library; each module's `persistence` package owns a disjoint set of tables, so extraction to a separate service becomes a matter of swapping the in-process `api` implementation for a remote client.
