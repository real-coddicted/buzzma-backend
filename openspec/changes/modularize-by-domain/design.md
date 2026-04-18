## Context

The Spring Boot 3.3 / Java 21 backend currently lives under `com.mobo` and is sliced by technical layer:

```
com.mobo
├── controller/   (26 REST controllers)
├── service/      (33 service interfaces)
│   └── impl/     (33 service implementations)
├── repository/   (21 Spring Data JPA repositories)
├── entity/       (22 @Entity classes)
│   └── enums/    (21 enums)
├── dto/          (request/response DTOs + auth/)
├── mapper/       (20 MapStruct mappers)
├── security/     (JWT, filters, SecurityConfig)
├── common/       (base CRUD, audit, codegen, password)
└── exception/    (ApiException, GlobalExceptionHandler)
```

Every feature touches all six top-level folders. Nothing in the package system prevents `BrandServiceImpl` from calling `ShopperProfilesRepository` directly, and a few services already reach into repositories they do not "own" (e.g. `BrandDomainServiceImpl` uses `OrdersRepository`, `TransactionsRepository`, `WalletsRepository`, `UsersRepository`). If we attempt to extract any of these areas into a separate service later, we have to trace these reach-ins one import at a time.

Constraints for this change:
- **No business-logic changes** — every method body stays identical.
- **No API / DB / migration changes** — REST paths, JSON shapes, table names, and Flyway files are frozen.
- **Monolith deploy stays** — we are not splitting the Gradle build, not introducing inter-process calls, and not adopting Spring Modulith tooling yet.
- **Future microservices must be cheap** — module boundaries should be the natural seam along which a future extraction happens.

Stakeholders: the backend engineers who will work in these packages day-to-day, plus anyone who later owns a service extraction.

## Goals / Non-Goals

**Goals:**
- Group every `.java` file under `com.mobo` into a single domain module by semantic ownership (Brands, Agency, Mediator, Buyers, Orders, Wallet, Catalog, Support, Identity, Admin, Notifications, Shared).
- Within each module, keep a predictable internal layout: `api/`, `web/`, `service/`, `persistence/`, `mapper/`.
- Make inter-module dependencies explicit: one module may only depend on another module's `api` sub-package.
- Enforce the boundary at build time with an ArchUnit test so violations fail CI.
- Leave a migration path: a module's `api` is what would become a shared contract artifact, and its `persistence/` owns a disjoint slice of tables.

**Non-Goals:**
- Splitting `build.gradle` into multiple sub-projects.
- Adopting Spring Modulith, hexagonal architecture, CQRS, or event-driven messaging.
- Renaming any class, method, REST path, DB table, or column.
- Changing transaction boundaries or `@Transactional` placement (other than following the class to its new package).
- Deleting or merging duplicate-looking classes (e.g. `BrandController` vs `BrandsController`) — those are pre-existing and tracked separately.
- Introducing a shared-kernel library, DDD aggregates, or value objects.

## Decisions

### 1. Module list and ownership

One Java package per module under `com.mobo.<module>`. Mapping of current classes → module:

| Module | Entities (tables) | Services | Controllers |
|---|---|---|---|
| `brands` | `BrandsEntity` | `BrandService`, `BrandDomainService` | `BrandController`, `BrandsController` |
| `agency` | `AgenciesEntity` | `AgencyService` | `AgenciesController` |
| `mediator` | `MediatorProfilesEntity` | `MediatorProfileService`, `LineageService`, `PendingConnectionService` | `MediatorProfilesController`, `PendingConnectionsController` |
| `buyers` | `ShopperProfilesEntity` | `ShopperProfileService` | `ShopperProfilesController` |
| `catalog` | `CampaignsEntity`, `DealsEntity` | `CampaignService`, `DealService` | `CampaignsController`, `DealsController` |
| `orders` | `OrdersEntity`, `OrderItemsEntity` | `OrderService`, `OrderItemService`, `OrderDomainService`, `OrderWorkflowService`, `CoolingPeriodSettlerService` | `OrdersController`, `OrderItemsController` |
| `wallet` | `WalletsEntity`, `TransactionsEntity`, `PayoutsEntity` | `WalletService`, `WalletBusinessService`, `TransactionService`, `PayoutService` | `WalletsController`, `TransactionsController`, `PayoutsController` |
| `support` | `TicketsEntity`, `TicketCommentsEntity` | `TicketService`, `TicketCommentService`, `TicketDomainService` | `TicketsController`, `TicketCommentsController` |
| `identity` | `UsersEntity`, `InvitesEntity`, `SecurityQuestionsEntity` | `UserService`, `AuthService`, `InviteService`, `InviteBusinessService`, `SecurityQuestionService` | `UsersController`, `AuthController`, `InvitesController`, `SecurityQuestionsController` |
| `admin` | `SuspensionsEntity`, `SystemConfigsEntity`, `AuditLogsEntity` | `AdminDomainService`, `OpsService`, `SuspensionService`, `SystemConfigService`, `AuditLogService` | `AdminController`, `OpsController`, `SuspensionsController`, `SystemConfigsController`, `AuditLogsController` |
| `notifications` | `PushSubscriptionsEntity` | `PushSubscriptionService` | `PushSubscriptionsController` |
| `shared` | *(none)* | *(none — holds `common/`, `security/`, `exception/`, `HealthController`, enums, `JavaBackendApplication`)* | `HealthController` |

**Rationale:** starts from the user's four named modules (Brands, Agency, Mediator, Buyers) and groups every remaining entity by its clearest natural owner. `catalog` and `admin` grouping keeps modules at a manageable granularity (5–10 per module) without forcing a 1:1:1 entity-to-module explosion.

**Alternatives considered:**
- **One module per entity (22 modules)**: too fine-grained; most would be three classes.
- **Fold Campaigns/Deals into `brands`**: Campaigns and Deals belong to brands but are also the core of the catalog shoppers browse; a separate `catalog` module lets a future extraction split "brand admin" from "deal discovery" without another reorg.
- **Fold Wallet into `identity` (per-user wallet)**: wallets, transactions, and payouts are a distinct financial subdomain with audit/settlement concerns; keeping them separate matches how they would be extracted later.

### 2. Internal module layout

Each module follows the same shape:

```
com.mobo.<module>
├── api/          # interfaces + DTOs exposed to other modules
├── web/          # @RestController classes
├── service/
│   └── impl/     # @Service classes
├── persistence/  # @Entity + Spring Data repositories
└── mapper/       # MapStruct mappers
```

**Rationale:** the `api/` package is the ONLY thing other modules may import. `persistence/` (entities + repositories) stays private to the module, which is the rule that makes microservice extraction tractable. Keeping `service/impl` mirrors the current `service/` + `service/impl/` split so Spring configuration and existing `@Transactional` semantics do not change.

**Alternatives considered:**
- **Flat module layout (no sub-packages)**: simpler, but loses the `api/` vs internal distinction and makes the ArchUnit rule harder to express.
- **Rename `persistence/` to `internal/`**: would require ArchUnit to whitelist by suffix; `persistence/` is a more honest name for what it contains.

### 3. Cross-module dependency rule

Rule: a class in `com.mobo.A.*` may import from `com.mobo.A.**`, from `com.mobo.shared.**`, and from `com.mobo.B.api.**` for any module `B` — but never from `com.mobo.B.persistence.**`, `com.mobo.B.service.**`, `com.mobo.B.web.**`, or `com.mobo.B.mapper.**`.

Today's reach-ins that violate this rule (discovered from grepping imports) need dedicated `api` interfaces:

| Current reach-in | New `api` interface in owner module |
|---|---|
| `BrandDomainServiceImpl` → `OrdersRepository`, `TransactionsRepository` | `orders.api.OrderQueryPort`, `wallet.api.TransactionQueryPort` |
| `BrandDomainServiceImpl` → `UsersRepository`, `WalletsRepository` | `identity.api.UserLookupPort`, `wallet.api.WalletLookupPort` |
| `AdminDomainServiceImpl` → `UsersRepository`, `SuspensionsRepository`, `OrderWorkflowService` | `identity.api.UserLookupPort`, (stays in `admin`), `orders.api.OrderWorkflowPort` |
| `AuthServiceImpl` → `WalletsEntity`, `InviteBusinessService`, `WalletBusinessService` | `wallet.api.WalletProvisioningPort`, `identity.api.InviteConsumptionPort` (inter-identity) |
| Mappers referenced across modules (e.g. `BrandDomainService` uses `OrdersMapper`, `TransactionsMapper`) | Mapper stays internal; owning module exposes DTO-returning methods on its `api` port |

**Rationale:** each `*Port` interface is a thin seam that wraps the existing query or mutation. The *implementation* of the port lives in the owning module, delegates to the existing repository/service, and does not change behavior. When we later extract a module to a separate process, the port implementation swaps for a remote client; callers do not change.

**Alternatives considered:**
- **Allow repository access across modules, warn in review**: no teeth; the current reach-ins prove review alone does not hold the line.
- **Spring Modulith `@ApplicationModule`**: heavier buy-in than we want right now; we can adopt later if the ArchUnit rule proves insufficient. Spring Modulith is additive to this layout, not incompatible with it.
- **Shared entities / cross-module JPA joins**: would block any future extraction and is exactly what we are buying our way out of.

### 4. Shared module contents

`com.mobo.shared` holds:
- `shared.common` — `BaseCrudService`, `OffsetBasedPageRequest`, `Auditable`, `AuditEntityListener`, `AuditLogWriter`, `CodeGenerator`, `PasswordService`.
- `shared.security` — `JwtService`, `JwtProperties`, `JwtAuthenticationFilter`, `SecurityConfig`, `MoboUserDetails`, `CurrentUserId`, `UpstreamSuspensionFilter`.
- `shared.exception` — `ApiException`, `GlobalExceptionHandler`.
- `shared.enums` — all 21 enums (they are value types referenced cross-module).
- `shared.web` — `HealthController` (pure liveness endpoint, no domain).
- `JavaBackendApplication` stays at the root `com.mobo` package so Spring component-scan picks up every sub-package with no configuration.

**Rationale:** these are framework / infrastructure concerns, not business logic. Enums are placed in `shared.enums` (not per-module) because several enums are referenced from multiple modules (e.g. `UserStatus` from `identity`, `admin`, and `auth` flows); splitting them would force cross-module imports of non-api types.

**Alternatives considered:**
- **Per-module enums**: would require every module that reads another module's status to depend on that module's internals. Reject.
- **Move `AuditLogWriter` into `admin`**: it writes to `AuditLogsEntity` which `admin` owns, but every module calls it. Resolution: keep the writer interface in `shared.common`; its implementation can live in `admin` and be injected by Spring. (Implementation detail deferred to tasks.)

### 5. Enforcement

Add ArchUnit test `src/test/java/com/mobo/architecture/ModuleBoundaryTest.java`:

```java
@AnalyzeClasses(packages = "com.mobo", importOptions = ImportOption.DoNotIncludeTests.class)
class ModuleBoundaryTest {
  @ArchTest
  static final ArchRule modules_only_depend_on_api_of_other_modules =
      classes().that().resideInAPackage("com.mobo.(*)..")
               .should().onlyDependOnClassesThat(
                   resideInAnyPackage(
                       "com.mobo.(*).api..",
                       "com.mobo.shared..",
                       "java..", "javax..", "jakarta..",
                       "org.springframework..", "org.slf4j..",
                       "com.fasterxml..", "lombok..", "org.mapstruct..",
                       "io.jsonwebtoken.."))
               .orShould().resideInTheSamePackage("..");
  // Plus: HealthController allowed in shared.web; JavaBackendApplication allowed at com.mobo root.
}
```

(Exact predicate composed in the implementation; the shape above conveys intent.)

**Rationale:** cheap, in-build, familiar to Java teams. Fails the next developer's PR if they reach past an `api` boundary.

### 6. Controller URL & configuration stability

- Every `@RequestMapping` / `@GetMapping` path is preserved. The URL contract is set by annotations inside controller classes, so moving the class does not touch the URL.
- `SecurityConfig` is kept in `shared.security`; it references URL patterns (not class names), so it needs no update.
- `application.yaml` — checked for any `basePackage` / `scanBasePackages` setting; Spring Boot's default package scanning from `JavaBackendApplication` covers `com.mobo.**`, so no change needed.
- MapStruct generated sources land under `build/generated/sources/annotationProcessor/java/main/...` — moving mapper source packages updates the generated-class packages, but those are regenerated each build and are not referenced manually anywhere.

## Risks / Trade-offs

- **Risk:** a missed reach-in leaves a circular or illegal dependency the ArchUnit rule catches only after the full move. **Mitigation:** build the ArchUnit rule *first* (with all modules still empty), then move one module at a time, running `./gradlew test` after each; also grep for `import com.mobo.<other-module>.(persistence|service|mapper|web)` before finishing.
- **Risk:** Spring bean wiring breaks because `@ComponentScan` has been narrowed somewhere. **Mitigation:** confirm `JavaBackendApplication` uses default scanning (no `basePackages` arg) before moving anything; if not, update it to scan `com.mobo`.
- **Risk:** Flyway or MapStruct picks up the wrong package. **Mitigation:** Flyway uses classpath resources (`db/migration/*.sql`), not Java packages — unaffected. MapStruct is annotation-driven per class — unaffected as long as the mapper interface is in the compile path.
- **Risk:** git history becomes harder to follow because almost every file moves. **Mitigation:** do the move in a single commit per module using `git mv` so `git log --follow` continues to work; document the mapping in `tasks.md`.
- **Risk:** IDE run configs and bookmarks break for every developer. **Mitigation:** land in one PR with a short migration note; ask developers to re-import the project.
- **Trade-off:** `shared` grows into a catch-all. **Mitigation:** scope it tightly (framework, cross-cutting infrastructure, enums, exceptions, `MoboApplication`, `HealthController`) and add a note in `shared/package-info.java` describing what is and is not allowed.
- **Trade-off:** the `api` ports add a small indirection layer (one interface + one adapter per cross-module call). Worth it for the enforceable boundary.

## Migration Plan

Executed as a single branch, landed as one (or a small stack of) PR(s):

1. Add ArchUnit dependency and a permissive starter rule (pass-only, to prove wiring).
2. Create `com.mobo.shared.*` sub-packages and move `common/`, `security/`, `exception/`, `entity/enums/`, `HealthController` into them. Run tests.
3. Create each domain module's package skeleton (`api/`, `web/`, `service/`, `service/impl/`, `persistence/`, `mapper/`). Empty.
4. For each module in this order — `identity`, `wallet`, `orders`, `brands`, `agency`, `mediator`, `buyers`, `catalog`, `support`, `admin`, `notifications` — `git mv` the relevant entity, repository, service interface, service impl, controller, mapper, and DTOs into the module. Fix imports. Run `./gradlew test`.
5. For each cross-module reach-in identified in §3, introduce the `api` port interface in the owner module, implement it with a `@Service` that wraps the existing internal call, and update the caller to depend on the port. Commit per port.
6. Tighten the ArchUnit rule to forbid `persistence` / `service` / `web` / `mapper` imports across module boundaries. Run tests.
7. Smoke-test the running app (`./gradlew bootRun`) and hit one endpoint per module to confirm bean wiring still works.

**Rollback:** single revert — every change is package moves + interface introductions; no DB, config, or API changes to undo.

## Open Questions

- Should `BrandController` vs `BrandsController` (both exist today) be consolidated as part of this reorg or tracked separately? *Proposal: track separately; this change preserves both to respect the "no business-logic changes" rule.*
- Do we want `api` interfaces to return module-local DTOs or a new neutral DTO type? *Proposal: return existing DTOs from `dto/` as they are re-homed into the owning module's `api` package (since the DTO and the port both become the public contract).*
- Enum home: should enums referenced by only one module live in that module instead of `shared.enums`? *Proposal: yes — during the move, any enum imported by exactly one module goes into that module's `api` package; enums used by 2+ modules stay in `shared.enums`. Concrete placement decided per-enum in tasks.md.*
