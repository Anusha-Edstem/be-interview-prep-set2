# be-interview-prep-set2

Backend interview prep exercises, set 2. Each exercise is built as a Spring Boot feature on its own
branch and merged into `main` through its own pull request.

The repository starts as a minimal Spring Boot skeleton: an application class, configuration and a
context test. The exercises below add the features.

## Versions

| | |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.16 |
| Build | Maven (wrapper committed) |
| Database | H2, in-memory |

## How to run

```bash
./mvnw spring-boot:run
```

The service listens on `http://localhost:8080`.

On Windows `cmd` or PowerShell, use `mvnw.cmd` in place of `./mvnw`.

## How to run tests

```bash
./mvnw test
```

To format sources and run the full build including the format check:

```bash
./mvnw spotless:apply
./mvnw verify
```

## Exercises

| Exercise | Pull request |
|---|---|
| Q1 — Library API | _pending_ |
| Q2 | _pending_ |
| Q3 | _pending_ |
| Q4 | _pending_ |
| Q5 | _pending_ |

## Walkthrough

Final video walkthrough: _pending_
