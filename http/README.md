# HTTP requests

This folder contains IntelliJ HTTP Client request files (`*.http`) for manually
exercising the `store-service` REST API, plus the environment files that back
their variables.

## Files

- `nearest-stores.http` — happy-path requests: default parameters, `limit`,
  `open`, and coordinate boundary values (±90 latitude, ±180 longitude).
- `nearest-stores-validation.http` — negative scenarios: missing/invalid
  coordinates, out-of-range `limit`, wrong HTTP method, unknown routes.
- `http-client.env.json` — **committed** shared/public environment variables
  (e.g. `baseUrl`, sample `latitude`/`longitude`). Safe to check into git.
- `http-client.private.env.json` — **gitignored** per-developer overrides for
  the same environment names. Currently empty because the service only runs
  on `localhost` and has no secrets to configure; add local-only overrides
  here if that ever changes.

## Usage

1. Open any `.http` file in IntelliJ IDEA (Ultimate) or another JetBrains IDE.
2. Select the `local` environment from the environment picker in the top-right
   of the editor gutter (or via the run icon dropdown).
3. Start the application (`./gradlew bootRun`), then click the green "run"
   gutter icon next to a request to execute it.

Requests include response handler scripts (`> {% client.test(...) %}`) that
assert status codes and basic response shape, so you can also run an entire
file and see pass/fail results per request.
