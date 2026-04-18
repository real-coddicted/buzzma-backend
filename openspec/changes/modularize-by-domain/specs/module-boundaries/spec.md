## ADDED Requirements

### Requirement: Domain module partitioning

The codebase SHALL be partitioned into exactly the following top-level Java packages under `com.mobo`, each representing a single domain module: `brands`, `agency`, `mediator`, `buyers`, `catalog`, `orders`, `wallet`, `support`, `identity`, `admin`, `notifications`, and `shared`. Every production `.java` file under `com.mobo` (other than `JavaBackendApplication.java`) MUST reside inside exactly one of these module packages.

#### Scenario: Every production class belongs to a module

- **WHEN** an engineer lists all classes under `src/main/java/com/mobo/` (excluding `JavaBackendApplication.java`)
- **THEN** every class's package SHALL start with one of `com.mobo.brands`, `com.mobo.agency`, `com.mobo.mediator`, `com.mobo.buyers`, `com.mobo.catalog`, `com.mobo.orders`, `com.mobo.wallet`, `com.mobo.support`, `com.mobo.identity`, `com.mobo.admin`, `com.mobo.notifications`, or `com.mobo.shared`

#### Scenario: No class lives in the legacy layer packages

- **WHEN** the build is run after this change is applied
- **THEN** the packages `com.mobo.controller`, `com.mobo.service`, `com.mobo.service.impl`, `com.mobo.repository`, `com.mobo.entity`, `com.mobo.dto`, `com.mobo.mapper`, `com.mobo.common`, `com.mobo.security`, and `com.mobo.exception` SHALL NOT exist (they MUST be empty or absent)

### Requirement: Module internal layout

Every domain module under `com.mobo.<module>` (except `shared`) SHALL organize its classes into sub-packages named `api`, `web`, `service`, `service.impl`, `persistence`, and `mapper`, with the following contents:

- `api/` — public interfaces consumed by other modules, plus the request/response DTOs returned by those interfaces and controllers.
- `web/` — `@RestController` classes.
- `service/` — service interfaces local to the module (implementations in `service.impl/`).
- `persistence/` — `@Entity` classes and Spring Data repositories.
- `mapper/` — MapStruct mappers.

A class MUST live in the sub-package that matches its role.

#### Scenario: Controller placement

- **WHEN** a class is annotated with `@RestController`
- **THEN** it SHALL reside in `com.mobo.<module>.web`

#### Scenario: Entity and repository placement

- **WHEN** a class is annotated with `@Entity` or extends `org.springframework.data.jpa.repository.JpaRepository` (or another Spring Data repository interface)
- **THEN** it SHALL reside in `com.mobo.<module>.persistence`

#### Scenario: Mapper placement

- **WHEN** a class or interface is annotated with `@Mapper` (MapStruct)
- **THEN** it SHALL reside in `com.mobo.<module>.mapper`

#### Scenario: Service placement

- **WHEN** a class is annotated with `@Service`
- **THEN** it SHALL reside in `com.mobo.<module>.service.impl`, and its interface (if present) SHALL reside in `com.mobo.<module>.service` or `com.mobo.<module>.api`

### Requirement: Cross-module dependency rule

A class in module `A` (package `com.mobo.A.*`) MAY import from:
- Its own module: `com.mobo.A.**`
- The `shared` module: `com.mobo.shared.**`
- Another module's public API: `com.mobo.B.api.**` for any other module `B`

A class in module `A` MUST NOT import from another module `B`'s `persistence`, `service`, `service.impl`, `web`, or `mapper` sub-packages. The only permitted cross-module coupling is via `api` packages or via `shared`.

#### Scenario: Legal cross-module call via api

- **WHEN** `impl.service.brands.com.coddicted.buzzma.BrandDomainServiceImpl` needs data owned by the `orders` module
- **THEN** it SHALL depend on an interface in `com.mobo.orders.api` (e.g. `OrderQueryPort`), and SHALL NOT import `com.mobo.orders.persistence.*` or `com.mobo.orders.service.*`

#### Scenario: Illegal cross-module reach-in is rejected

- **WHEN** any production class imports a type from another module's `persistence`, `service`, `service.impl`, `web`, or `mapper` sub-package
- **THEN** the ArchUnit test `ModuleBoundaryTest` SHALL fail the build

#### Scenario: Shared module is globally importable

- **WHEN** a class in any module imports from `com.mobo.shared.**`
- **THEN** the ArchUnit test SHALL NOT flag it as a violation

### Requirement: Shared module scope

The `com.mobo.shared` module SHALL contain only cross-cutting infrastructure: common framework utilities (`shared.common`), security and authentication plumbing (`shared.security`), global exception handling (`shared.exception`), globally-used enums (`shared.enums`), and the liveness `HealthController` (`shared.web`). The `shared` module MUST NOT contain any `@Entity`, `@Repository`, business-logic `@Service`, or domain DTO.

#### Scenario: Shared contains no entities

- **WHEN** the `com.mobo.shared` package tree is scanned for `@Entity` annotations
- **THEN** zero classes SHALL be found

#### Scenario: Shared contains no business services

- **WHEN** a `@Service` class references a business concept (brand, order, wallet, ticket, etc.)
- **THEN** it SHALL NOT reside in `com.mobo.shared.**`

### Requirement: Boundary enforcement test

The test suite SHALL include an ArchUnit (or equivalent architecture-test library) test class that asserts the module partitioning, internal-layout, and cross-module dependency rules above. The test MUST run as part of `./gradlew test` and MUST fail the build on any violation.

#### Scenario: ArchUnit test runs in CI

- **WHEN** `./gradlew test` is executed
- **THEN** the test class `architecture.com.coddicted.buzzma.ModuleBoundaryTest` (or equivalent) SHALL be executed and all its `@ArchTest` rules SHALL pass

#### Scenario: Introducing a reach-in fails CI

- **WHEN** a developer adds an import of `persistence.orders.com.coddicted.buzzma.OrdersRepository` inside a class in `com.mobo.brands.**`
- **THEN** `./gradlew test` SHALL fail with a violation message naming the illegal dependency

### Requirement: Behavioral preservation

Refactoring code into modules MUST NOT change any observable behavior. Specifically, across the refactor:

- Every REST endpoint SHALL retain its HTTP method, path, request payload shape, response payload shape, and HTTP status codes.
- Every Flyway migration file in `src/main/resources/db/migration/` SHALL remain unchanged.
- Every JPA entity SHALL map to the same database table with the same columns and constraints.
- Every existing service method SHALL retain the same signature (name, parameters, return type, thrown exceptions) and the same method body, apart from `import` statements.
- No class, interface, enum, method, field, or REST path SHALL be renamed as part of this change.

#### Scenario: REST surface is identical

- **WHEN** the list of all `@RequestMapping` / `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` paths is compared before and after the change
- **THEN** the two lists SHALL be identical

#### Scenario: Database schema is unchanged

- **WHEN** a fresh database is built from Flyway migrations before and after the change
- **THEN** the resulting schema (tables, columns, constraints, indexes) SHALL be identical

#### Scenario: Existing tests pass unchanged

- **WHEN** `./gradlew test` is run after the refactor
- **THEN** every pre-existing test in `src/test/java/com/mobo/**` SHALL pass without changes to its assertions (only `package` and `import` lines may be updated)

### Requirement: Microservice extraction readiness

Each module SHALL be structured so that extracting it into a separate service requires no change to calling modules' source code outside of the adapter that implements the module's `api` port. Specifically, a module's `persistence` package MUST own a disjoint set of database tables (no other module's entity maps to those tables), and every cross-module call MUST go through an interface in the owning module's `api` package.

#### Scenario: Disjoint table ownership

- **WHEN** the set of `@Table` names declared by `@Entity` classes in each module's `persistence` package is computed
- **THEN** the sets for any two distinct modules SHALL be disjoint (no table is mapped by entities in more than one module)

#### Scenario: Cross-module calls are all port-mediated

- **WHEN** an engineer greps for cross-module imports in the codebase
- **THEN** every cross-module import SHALL resolve to either `com.mobo.<module>.api.**` or `com.mobo.shared.**`
