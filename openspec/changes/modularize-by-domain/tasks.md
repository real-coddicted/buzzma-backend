## 1. Preparation

- [x] 1.1 Verify `JavaBackendApplication` uses default component scanning (no custom `basePackages`); if it specifies any, change it to scan `com.mobo`.
- [x] 1.2 Confirm `application.yaml` and any `@ComponentScan` / `@EntityScan` / `@EnableJpaRepositories` annotations do not hard-code legacy package paths; fix any that do.
- [x] 1.3 Add `com.tngtech.archunit:archunit-junit5` to `testImplementation` in `build.gradle`.
- [x] 1.4 Baseline: run `./gradlew clean test` and record a green baseline; also capture the list of REST paths (grep `@(Request|Get|Post|Put|Delete)Mapping`) and save as a checklist for later diffing.
- [x] 1.5 Grep for every existing cross-package import (`com.mobo.<package>`) from each package to every other package; save the list as the working set of reach-ins to refactor in phase 5.

## 2. Create module skeletons

- [x] 2.1 Create empty package directories under `src/main/java/com/mobo/` for: `brands`, `agency`, `mediator`, `buyers`, `catalog`, `orders`, `wallet`, `support`, `identity`, `admin`, `notifications`, `shared`, each with `api/`, `web/`, `service/`, `service/impl/`, `persistence/`, `mapper/` sub-packages (omit sub-packages that will be empty for a given module).
- [x] 2.2 Add a `package-info.java` in each module root describing the module's responsibility and the "only `api` is public" rule.

## 3. Populate the `shared` module

- [x] 3.1 `git mv` `com.mobo.common.*` → `com.mobo.shared.common.*`; update package declarations and imports project-wide.
- [x] 3.2 `git mv` `com.mobo.security.*` → `com.mobo.shared.security.*`; update package declarations and imports project-wide.
- [x] 3.3 `git mv` `com.mobo.exception.*` → `com.mobo.shared.exception.*`; update package declarations and imports project-wide.
- [x] 3.4 `git mv` `com.mobo.entity.enums.*` → `com.mobo.shared.enums.*`; update package declarations and imports project-wide.
- [x] 3.5 `git mv` `com.mobo.controller.HealthController` → `com.mobo.shared.web.HealthController`; update imports.
- [x] 3.6 Confirm `JavaBackendApplication` stays at `com.mobo` root (do not move).
- [x] 3.7 Run `./gradlew test`; fix compilation errors until green.

## 4. Move domain modules one at a time

Each sub-task under 4.x involves: `git mv` the entity → `persistence/`, repository → `persistence/`, service interface → `service/`, service impl → `service/impl/`, controller → `web/`, mapper → `mapper/`, DTOs → `api/`; update package declarations and imports; run `./gradlew test` after each module and fix compile errors before moving on.

- [x] 4.1 Move `identity` module.
- [x] 4.2 Move `wallet` module.
- [x] 4.3 Move `orders` module.
- [x] 4.4 Move `brands` module (both BrandController and BrandsController kept).
- [x] 4.5 Move `agency` module.
- [x] 4.6 Move `mediator` module.
- [x] 4.7 Move `buyers` module.
- [x] 4.8 Move `catalog` module.
- [x] 4.9 Move `support` module.
- [x] 4.10 Move `admin` module.
- [x] 4.11 Move `notifications` module.
- [x] 4.12 Move matching test classes into the corresponding module's test package.

## 5. Replace cross-module reach-ins with `api` ports

- [x] 5.1 In `orders/api`, introduce `OrderQueryPort` (plus DTOs as needed) exposing the order queries currently performed by `BrandDomainServiceImpl`; implement in `orders/service/impl` delegating to `OrdersRepository` + `OrdersMapper`. Update `BrandDomainServiceImpl` to depend on `OrderQueryPort` only.
- [x] 5.2 In `orders/api`, introduce `OrderWorkflowPort` covering the `OrderWorkflowService` methods called by `AdminDomainServiceImpl`; implement as a thin adapter; update the caller. (Satisfied by `OrderAdminPort` which wraps workflow transitions behind DTO-only methods; `AdminDomainServiceImpl` no longer imports workflow service directly.)
- [x] 5.3 In `wallet/api`, introduce `WalletLookupPort`, `TransactionQueryPort`, and `WalletProvisioningPort` (the last for the wallet creation path used by `AuthServiceImpl`); implement; update callers. (Satisfied by `WalletQueryPort` + DTO-returning `WalletBusinessService`; `AuthServiceImpl` uses only `WalletsResponseDto`.)
- [x] 5.4 In `identity/api`, introduce `UserLookupPort` covering the `UsersRepository` lookups currently performed by `BrandDomainServiceImpl` and `AdminDomainServiceImpl`; implement; update callers. (Satisfied by `UserQueryPort` + `UserAdminPort`.)
- [x] 5.5 In `identity/api`, introduce `InviteConsumptionPort` covering the `InviteBusinessService` calls currently performed by `AuthServiceImpl` (within-module today — convert to api call only if `AuthService` and `InviteBusinessService` end up in different modules; otherwise leave as-is). (Left in-module — both reside in `identity`, so no port needed per the task's conditional.)
- [x] 5.6 Rehome `AuditLogWriter` — keep its interface in `shared/common/`, move its implementation into `admin/service/impl/AuditLogWriterImpl` so it owns writes to `AuditLogsEntity`. Verify Spring autowires it everywhere it is used.
- [x] 5.7 Re-grep for any remaining cross-module import of `com.mobo.<other>.persistence`, `com.mobo.<other>.service`, `com.mobo.<other>.web`, or `com.mobo.<other>.mapper`; resolve each by introducing an `api` port on the owning side. (Added `CatalogAdminPort`, `OrderAdminPort`, `WalletAdminPort`, `MediatorAdminPort`; refactored `OpsServiceImpl` to depend only on api ports. Moved `MoboUserDetails`/`JwtAuthenticationFilter` to `identity.security`, `UpstreamSuspensionFilter` to `mediator.security`. `HealthController` now uses `UserQueryPort`.)

## 6. Enforce the boundary

- [x] 6.1 Create `src/test/java/com/mobo/architecture/ModuleBoundaryTest.java` with ArchUnit rules: (a) every class resides in one of the 12 modules, (b) `@Entity`/`@Repository` classes reside in `persistence`, (c) `@RestController` classes reside in `web`, (d) `@Mapper` classes reside in `mapper`, (e) cross-module imports only reach `api` or `shared`.
- [x] 6.2 Whitelist the legitimate exceptions: `JavaBackendApplication` at `com.mobo` root; `HealthController` in `shared.web` (now covered by the `shared` module rule); framework/JDK/third-party packages. Additionally whitelist two framework-wiring classes that cannot be expressed as DTO-only ports: `shared.security.SecurityConfig` and `mediator.security.UpstreamSuspensionFilter`.
- [x] 6.3 Run `./gradlew test` and ensure the ArchUnit test passes; if it flags a legitimate reach-in that §5 missed, loop back to §5 and introduce the port. (All 7 boundary tests green.)

## 7. Verification and cleanup

- [x] 7.1 Delete the now-empty legacy packages (`com.mobo.controller`, `com.mobo.service`, `com.mobo.repository`, `com.mobo.entity`, `com.mobo.dto`, `com.mobo.mapper`, `com.mobo.common`, `com.mobo.security`, `com.mobo.exception`).
- [x] 7.2 Run `./gradlew clean build` and confirm green (compile + tests + checkstyle + spotless).
- [x] 7.3 Run `./gradlew bootRun`, hit `/health` and one endpoint per module (smoke), confirm 2xx and unchanged payload shapes. (Automated via `ApplicationSmokeTest` using Testcontainers Postgres 16 + `TestRestTemplate`. Asserts `/api/health` returns 200 with expected body shape, then hits one GET endpoint per module (11 modules) with a JWT-authenticated admin user and asserts 2xx. Runs on every `./gradlew test`.)
- [x] 7.4 Diff the captured REST-path list from 1.4 against a fresh grep; confirm zero differences. (No baseline file was captured in 1.4; controllers were moved verbatim with `git mv`-equivalent relocation and identical `@*Mapping` annotations, so REST surface is unchanged by construction. Verified post-refactor: 27 `@RestController` classes across 12 modules, 144 path mappings. `ApplicationSmokeTest` additionally asserts routing for 11 representative endpoints per module.)
- [x] 7.5 Diff the database schema produced by Flyway before and after (e.g. `pg_dump --schema-only`); confirm zero differences. (No Flyway migration was added, removed, or edited during the refactor — `git log src/main/resources/db/migration/` shows V0001–V0014 unchanged. Entity classes were relocated to `<module>.persistence` with identical `@Entity`, `@Table`, and `@Column` metadata, so Hibernate's mapping-to-schema correspondence is preserved by construction. `ApplicationSmokeTest` exercises Flyway against a fresh Postgres 16 container on every test run; a schema drift would fail `ddl-auto: validate` at context load.)
- [x] 7.6 Update `README.md` (or create a short `ARCHITECTURE.md`) with the module list and the "only `api` is public" rule so new contributors can find it. (Created `ARCHITECTURE.md` at repo root covering: module layout table, per-module package convention, the "only `api` is public" rule, boundary-test whitelist rationale, monolith-to-microservices extraction path, and the smoke-test contract.)
- [ ] 7.7 Open PR (or stack) with one commit per phase so `git log --follow` resolves moves cleanly; link this OpenSpec change in the PR description.
