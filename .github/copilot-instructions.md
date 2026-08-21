# Copilot instructions

## Domain
- The application is about finding the five closest Jumbo stores for a given latitude and longitude.
- Store data comes from the supplied JSON seed file in `resources/stores.json`.
- Each store represents a Jumbo location with address and geographic information such as city, postal code, address name, latitude, and longitude.
- The outcome is the nearest stores for the requested position.
- Enforce coordinate validity in the domain model.

## Architecture
- This is a small Spring Boot and Spring MVC service with a layered package split under `be.lennertsoffers`.
  - `application` contains the Spring Boot entry point and shared bean wiring.
  - `domain` contains the core model (`domain.model`), use cases (`domain.service`), port contracts (`domain.repository`), and domain errors (`domain.exception`).
  - `infrastructure` contains adapters such as the REST controller (`infrastructure.rest`) and the in-memory repository plus data loading (`infrastructure.repository`).
- Follow hexagonal/DDD dependency direction: infrastructure depends on domain/application, and domain must not depend on infrastructure.
  - Keep transport concerns in controllers and persistence/loading concerns in adapters. Keep request/input models separate from the domain model.
  - Keep business rules and domain language in the domain/application layers.
- The domain layer may use Spring `@Component` and `@Service` annotations if needed for pragmatic wiring, but no other Spring dependencies should be introduced there.
- Use Domain Criteria Value Objects instead of adding boolean flags or ad hoc parameters.

## File and package structure
- Follow the existing package layout when adding new classes; place code in the layer package that matches its responsibility rather than introducing new top-level packages.
- Keep transport and persistence DTOs in a `dto` subpackage next to the adapter that owns them (`infrastructure.rest.dto`, `infrastructure.repository.dto`); never expose DTOs from the domain.
- One public type per file, named after the file.

## Code style
- Use records for value objects and DTOs; validate and normalize invariants in a compact constructor. Model entities (identity-based equality) as classes; base equality on the identifier only.
- Validate arguments eagerly: use `Objects.requireNonNull(value, "<field> must not be null")` and throw with descriptive messages that include the offending value.
- Enforce domain invariants by throwing a dedicated exception extending the abstract `DomainException`; keep validation-specific exceptions per invariant.
- Use constructor injection only; do not use field injection or `@Autowired`. Inject collaborators as `final` fields.
- Inject a `Clock` bean for time rather than calling `now()` directly, to keep time-dependent logic deterministic and testable.
- Map between layers with explicit `toDomain`/`fromDomain`/`toCriteria` methods rather than exposing shared mutable state.
- Validate transport input with Jakarta Bean Validation annotations at the controller boundary; return errors as RFC 7807 `ProblemDetail` from a single `@RestControllerAdvice`.
- Log with SLF4J via a `private static final Logger log`; prefer structured `key=value` messages.
- Write Javadoc for public types and methods, documenting `@param`, `@return`, and `@throws`; keep inline comments only where they clarify non-obvious intent.
- Use constants (`private static final`, UPPER_SNAKE_CASE) instead of magic numbers or literals. Use 4-space indentation.

## Conventions
- The project uses Spring Boot/Spring MVC; align new code with that setup and keep additional dependencies minimal.
- Load configuration from `application.yaml` using Spring `@Value`; do not introduce `@ConfigurationProperties` for this project size.

## Testing
- Prefer fast unit tests for domain logic.
- Write integration tests for the infrastructure with Spring.
- Use parameterized tests (`@CsvSource`, `@ValueSource`) where they improve coverage.
- Name tests `method_shouldExpectedBehavior_whenCondition` and add a `@DisplayName` sentence starting with "Should".
- Use AssertJ for assertions, including `assertThatThrownBy` for expected exceptions.
- Use Mockito for mocking but don't use BDDMockito.

## Developer experience
- Keep the repository easy to run from the README.
- Include the Gradle wrapper.
- Keep `.gitignore` up to date.
