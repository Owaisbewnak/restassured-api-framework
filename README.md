# RestAssured API Automation Framework — Restful Booker

[![API Tests](https://github.com/Owaisbewnak/restassured-api-framework/actions/workflows/ci.yml/badge.svg)](https://github.com/Owaisbewnak/restassured-api-framework/actions/workflows/ci.yml)
![Java 17](https://img.shields.io/badge/Java-17-blue)
![RestAssured](https://img.shields.io/badge/RestAssured-5.5.0-green)
![TestNG](https://img.shields.io/badge/TestNG-7.10.2-orange)

A Java API automation framework built with **RestAssured + TestNG + Maven**, covering
auth, booking CRUD, negative cases, data-driven tests, JSON schema validation and
response-time checks against the public demo API
[Restful Booker](https://restful-booker.herokuapp.com).

**45 tests** across auth, CRUD, negative cases, data-driven creation, JSON schema
validation and response-time checks.

## Why this project exists

My UI work (Selenium + Java + TestNG) was already on GitHub, but my API testing
was only Postman collections. SDET roles expect API automation **in code** — so
this framework proves exactly that: POJO-based requests, reusable clients,
TestNG DataProviders, schema validation and CI.

## Tech stack

| Area | Tool |
|---|---|
| Language | Java 17 |
| API client | RestAssured 5.5.0 |
| Test runner | TestNG 7.10.2 |
| Build | Maven 3.9 |
| Serialisation | Jackson 2.17 (POJOs) |
| Schema validation | RestAssured JSON Schema Validator |
| Reporting | Allure 2.29 + TestNG |
| Logging | Log4j2 |
| CI | GitHub Actions (JDK 17, `mvn clean test`) |

## Architecture

```
src/main/java/com/owais/restassured/
├── config/        Config.java            # config.properties + -D overrides
├── models/        Booking, BookingDates, AuthRequest, AuthResponse, BookingResponse
├── clients/       AuthClient, BookingClient   # one method per endpoint, returns Response
└── utils/         TestDataFactory, JsonUtils  # payload builders, JSON test-data reader

src/test/java/com/owais/restassured/
├── base/          BaseTest               # suite setup, shared token, time assertions
├── dataproviders/ BookingDataProvider    # JSON-file + inline DataProviders
└── tests/
    ├── AuthTests.java                          # 8 tests
    ├── BookingCrudTests.java                   # 11 tests
    ├── BookingNegativeTests.java               # 10 tests
    ├── BookingDataDrivenTests.java             # 9 tests
    └── BookingSchemaAndPerformanceTests.java   # 7 tests

src/test/resources/
├── config.properties     # base.url, auth credentials, response-time limit
├── testng.xml            # suite definition (5 test groups)
├── schemas/              # booking-schema, bookings-list-schema, auth-schema
└── testdata/bookings.json # 5 data-driven booking datasets
```

Design choices:

- **Clients return the raw `Response`** — tests own their assertions; the client
  only knows how to build and send a request.
- **POJOs, not raw JSON strings**, for every happy-path request (Jackson serialises
  them). Raw strings are used only in negative tests, deliberately.
- **One token per suite run** — fetched in `@BeforeSuite`, reused via a
  `Cookie: token=...` header, which is Restful Booker's documented auth mechanism.
- **Every write test creates its own booking** — the demo API is public and shared,
  so no test depends on a fixed pre-existing id.
- **Data-driven tests clean up after themselves** (DELETE the booking they created).

## Endpoints covered

| Endpoint | Method | Tests |
|---|---|---|
| `/auth` | POST | valid token, schema, 5 invalid-credential cases, empty body |
| `/booking` | GET | list, schema, response time, content-type, filter by firstname / lastname / checkin |
| `/booking/{id}` | GET | created booking, non-existent id (404), non-numeric id (404) |
| `/booking` | POST | create + echo, minimal booking, 5 JSON datasets, 4 price datasets |
| `/booking/{id}` | PUT | full update, no-auth (403), bad token (403), non-existent id (405) |
| `/booking/{id}` | PATCH | partial update (firstname only) |
| `/booking/{id}` | DELETE | delete + verify 404, no-auth (403), bad token (403), non-existent id (405) |

Plus one end-to-end chaining test: **create → get → update → get → delete → get 404**,
reusing the booking id returned at each step.

## How to run

Prerequisites: **JDK 17** and **Maven 3.9+**.

```bash
# Run the full suite (45 tests)
mvn clean test

# Run a single class
mvn -Dtest=AuthTests test

# View the Allure report (after a run)
mvn allure:serve
```

TestNG results are in `target/surefire-reports/` and Allure raw results in
`target/allure-results/`. Override the base URL or time limit without editing files:

```bash
mvn clean test -Dbase.url=https://restful-booker.herokuapp.com -Dmax.response.time.ms=10000
```

**Behind a proxy?** If `https_proxy` (or `http_proxy`) is set in your environment —
common on corporate networks — the framework picks it up automatically,
including proxy auth from the URL. With no proxy variable set, nothing changes.

## Test count

| Class | Tests |
|---|---|
| AuthTests | 8 |
| BookingCrudTests | 11 |
| BookingNegativeTests | 10 |
| BookingDataDrivenTests | 9 |
| BookingSchemaAndPerformanceTests | 7 |
| **Total** | **45** |

## Known API quirks (verified, not assumed)

These were probed against the live API before the tests were written, and the
tests assert the real behaviour:

1. **Bad credentials return HTTP 200** with `{"reason": "Bad credentials"}` and
   no token — not a 403.
2. **Write operations on a non-existent id return 405**, not 404
   (GET on the same id correctly returns 404).
3. **Creating a booking with a missing required field returns 500**, not 400.
4. **DELETE returns 201** on success.
5. The API is lenient about types — e.g. `totalprice: "abc"` is stored as `null`
   with a 200, and a `DD-MM-YYYY` date is silently mangled. The suite does not
   assert on those inputs.
6. Response times vary because the demo runs on a free Heroku dyno — the first
   call after idle can take a few seconds, so the asserted limit is 10,000 ms
   (`max.response.time.ms` in `config.properties`).

## CI

Every push / PR to `main` runs `mvn -B clean test` on GitHub Actions
(see `.github/workflows/ci.yml`). Allure results and Surefire reports are
uploaded as artifacts on every run.

## Author

**Owais Shakil Bewnak** — QA Automation Engineer / SDET (Selenium, Java, TestNG,
RestAssured). GitHub: [@Owaisbewnak](https://github.com/Owaisbewnak)
