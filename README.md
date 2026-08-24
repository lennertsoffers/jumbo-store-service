# store-service

A small Spring Boot service that returns the **five closest Jumbo stores** for a given
latitude/longitude. Store data is loaded once at startup from the bundled seed file
(`src/main/resources/stores.json`) and served through a REST API.

## Requirements

- **JDK 25** (the Gradle build declares a Java 25 toolchain).
- No other tooling needed, the Gradle wrapper (`./gradlew`) is included.
- The service is stateless and reads the seed file from the classpath, so no database or
  external service is required.

## Build & run

```bash
# Start the application (listens on http://localhost:8080)
./gradlew bootRun

# Build and run the tests
./gradlew build
```

### Manual requests

The `http/` folder contains HTTP Client files (`*.http`) to explore the API, including negative scenarios and response assertions. See [http/README.md](./http/README.md).

## API

### `GET /api/v1/stores`

Returns the nearest stores to a position, ordered by ascending distance.

| Query param | Type    | Required | Default | Description                                                        |
|-------------|---------|----------|---------|--------------------------------------------------------------------|
| `latitude`  | double  | yes      | –       | Latitude of the origin, `-90..90`.                                 |
| `longitude` | double  | yes      | –       | Longitude of the origin, `-180..180`.                              |
| `limit`     | int     | no       | `5`     | Maximum number of stores to return, `1..50`.                       |
| `open`      | boolean | no       | `false` | When `true`, only return stores currently open (see limitations). |

#### Example

```bash
curl "http://localhost:8080/api/v1/stores?latitude=52.3791&longitude=4.9003&limit=2"
```

```json
[
  {
    "id": "R74KYx4XucoAAAFIqY8YwKxK",
    "name": "Jumbo Amsterdam Westerstraat",
    "city": "Amsterdam",
    "postalCode": "1015 MN",
    "street": "Westerstraat",
    "houseNumber": "98-102",
    "latitude": 52.37867,
    "longitude": 4.883832,
    "todayOpen": "08:00",
    "todayClose": "21:00",
    "distanceInKm": 1.119
  }
]
```

#### Health

An actuator health endpoint is exposed
```bash
curl "http://localhost:8080/actuator/health"
```

## Configuration

See `src/main/resources/application.yaml`:

- `store-service.store.dataset-location` — classpath location of the seed file.
- `store-service.store.timezone` — timezone used to interpret store opening hours.


## Architecture

Hexagonal / DDD-flavoured layering under `be.lennertsoffers`:

- `application` — Spring Boot entry point and wiring.
- `domain` — model (`Store`, `Coordinates`, `OpeningHours`, …), the `StoreService` use case,
  and the `StoreRepository` port. Business rules and invariants live here; the domain does
  not depend on infrastructure.
- `infrastructure` — adapters: the REST controller (`infrastructure.rest`) and the
  in-memory store repository backed by the JSON loader (`infrastructure.repository`).

Search is expressed with a `StoreSearchCriteria` value object rather than ad-hoc flags.

Distance is computed with the Haversine formula.

### Data loading

Stores are loaded once at startup. A record is **only** skipped if it lacks the data
required to be searchable (a valid coordinate pair). Optional enrichment like opening hours never causes an otherwise valid store to be dropped; missing or unparseable
values are simply reported as absent.

## Known limitations / trade-offs

- **Opening hours are a daily snapshot.** The seed only contains today's `todayOpen`/
  `todayClose` for the day it was captured, so the `open` filter is only meaningful relative
  to that snapshot. A production feed would carry a full weekly schedule.
- **Linear scan.** For 587 stores a full scan + sort per request is more than fast enough
  and keeps the code simple.
- **In-memory data.** Data is immutable and loaded at startup; there is no refresh endpoint.

## Time spent

Approximately 6-7 hours in total, including understanding the data, designing the
architecture, implementation, tests, AI tooling setup, and documentation.

## Testing

```bash
./gradlew test
```

Fast unit tests cover the domain logic (parameterized where useful); a Spring MockMvc
integration test (`StoreControllerIntegrationTest`) exercises the REST layer end-to-end
against a small fixture dataset with a fixed `Clock`.
